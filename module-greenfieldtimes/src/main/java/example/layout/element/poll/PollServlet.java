package example.layout.element.poll;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.polopoly.application.servlet.ApplicationServletUtil;
import com.polopoly.cm.client.CMException;
import com.polopoly.poll.FraudDetector;
import com.polopoly.poll.client.PollClient;

/**
 * Servlet handling voting for polls by submitting votes to the Poll Server. Poll
 * votes should be submitted to this servlet, with four parameters:
 * <dl>
 * <dt>pollId</dt>
 * <dd>The ID of the poll, on the format poll_<major>_<minor>, e.g.
 * pollId=poll_7_123</dd>
 * <dt>questionId</dt>
 * <dd>The ID of the question</dd>
 * <dt>answerId</dt>
 * <dd>The ID of the answer</dd>
 * <dt>forward</dt>
 * <dd>The URL for the result page, to which the request will be forwarded after
 * voting</dd>
 * 
 * <p>
 * The servlet supports single question polls. Form posts with 'GET'
 * and 'POST' methods are supported.
 * </p>
 * <p>
 * The servlet tries to make sure a user can vote only once by making sure
 * that cookies can be set and that the user has not voted in this poll before submitting the
 * vote.
 * </p>
 * <p>
 * The user will be forwarded to the result page regardless if the voting was succesful or not.
 * </p>
 */
@SuppressWarnings("serial")
public class PollServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(PollServlet.class.getName());
    
    private PollClient pollClient;

    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        // Get the poll client application component
        pollClient =
                (PollClient) ApplicationServletUtil.getApplication(config.getServletContext())
                        .getApplicationComponent(PollClient.DEFAULT_COMPOUND_NAME);
    }

    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        try {
            handlePollSubmit(request, response);
        } catch (CMException cme) {
            // A CMException is thrown only if a serious error has occurred.
            throw new ServletException(cme);
        }
    }

    private void handlePollSubmit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CMException
    {
        // We need to make sure cookies are supported, which we do by trying to
        // set one and redirecting back to ourself, and then checking that the
        // cookie was set before allowing voting
        boolean isCookieSet = (request.getParameter("cookieSet") != null);
        
        if (!isCookieSet) {
            setCookieAndRedirect(request, response);
        } else {
            String votedOption = voteIfValidCookie(request, response);
            doForward(request, response, votedOption);
        }
    }

    private void doForward(HttpServletRequest request,
                           HttpServletResponse response,
                           String votedOption)
        throws ServletException, IOException
    {
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(getForward(request));

        // If a vote has been performed, add the option id to be able to add this
        // vote manually on the result page to compensate for the poll client cache
        if (votedOption != null) {
            request.setAttribute("poll.votedOptionId", votedOption);
        }
        
        // Avoid redirection to keep request attributes
        request.setAttribute("p.NoRedirect", true);
        dispatcher.forward(request, response);
    }

    private String voteIfValidCookie(HttpServletRequest request,
                                     HttpServletResponse response) 
        throws CMException
    {
        boolean cookieIsValid = checkCookie(request);
        
        if (cookieIsValid) {
            PollAnswer answer = getPollAnswer(request, response);
            return answer.submitAnswer(request, response);
        }
        
        return null;
    }

    private boolean checkCookie(HttpServletRequest request)
        throws CMException
    {
        // If cookie ('fd_cookieCheck') does not exist, try to set cookie and
        // redirect.
        boolean isCookieEnabled = isCookiesEnabled(request);
        if (isCookieEnabled) {
            boolean isTimestampValid = isTimestampValidInCookie(request);
    
            if (isTimestampValid) {
                return true;
            }
        }

        return false;
    }

    private PollAnswer getPollAnswer(HttpServletRequest request,
            HttpServletResponse response)
    {
        String pollId = getPollParamId(request, "pollId");
        String questionId = getPollParamId(request, "questionId");
        String answerId = getPollParamId(request, "answerId");

        if (!isEmpty(pollId) && !isEmpty(questionId) && !isEmpty(answerId)) {
            return new PollAnswer(pollId, questionId, answerId);
        }

        return new InvalidPollAnswer();
    }

    private String getPollParamId(HttpServletRequest request,
            String parameterName)
    {
        String parameterValue = request.getParameter(parameterName);
        
        if (isEmpty(parameterValue)) {
            LOG.log(Level.FINE, "Missing request parameter '" +
                    parameterName + "'");
        }
        return parameterValue;
    }

    private boolean isCookiesEnabled(HttpServletRequest request)
        throws CMException
    {
        if (!FraudDetector.isCookiesEnabled(request)) {
            LOG.log(Level.FINE,
                    "Can't accept submitted vote since coookies aren't enabled.");
            
            return false;
        }

        return true;
    }

    private void setCookieAndRedirect(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {

        FraudDetector.setCookiesEnabledCheckCookie(request, response, (System
                .currentTimeMillis() / 1000L));

        StringBuffer redirectURL = createRedirectURL(request);
        redirectURL.append("&cookieSet=true");

        LOG.log(Level.FINE, "Set timestamp and redirect to URL: "
                + redirectURL.toString());

        response.sendRedirect(redirectURL.toString());
    }

    /*
     * Gets url for forward to. Makes sure it is a relative path
     * (i.e. does not use http://)
     */
    String getForward(HttpServletRequest request)
    {
        String forward = request.getParameter("forward");
        if (isEmpty(forward)) {
            LOG.log(Level.WARNING, "The request parameter 'forward' is missing!");
            forward = "/";
        }

        if (forward.startsWith("http://")) {
            int slashIndex = forward.indexOf('/', "http://".length());
            if (slashIndex != -1) {
                forward = forward.substring(slashIndex);
            } else {
                forward = "/";
            }
        } else if (!forward.startsWith("/")) {
            forward = "/" + forward;
        }

         return forward;
    }

    /*
     * Creates url for use in redirect when cookie is set.
     * Adds post parameters to query string.
     */
    private StringBuffer createRedirectURL(HttpServletRequest request)
    {
        StringBuffer newURL = new StringBuffer(request.getRequestURL().toString());

        if (newURL.indexOf("?") == -1) {
            newURL.append("?");
        }

        Map<?, ?> parameters = request.getParameterMap();
        for (Iterator<?> it = parameters.entrySet().iterator(); it.hasNext();) {
            Entry<?, ?> entry = (Entry<?, ?>) it.next();

            String[] values = (String[]) entry.getValue();
            String name = (String) entry.getKey();

            for (int i = 0; i < values.length; i++) {
                newURL.append(name);
                newURL.append("=");
                newURL.append(values[i]);
                if ((i + 1) < values.length) {
                    newURL.append("&");
                }
            }

            if (it.hasNext()) {
                newURL.append("&");
            }
        }
        
        return newURL;
    }

    /*
     * Checks if timestamp in cookie is valid. A timestamp is valid if
     * it is not too old.
     */
    private boolean isTimestampValidInCookie(HttpServletRequest request)
        throws CMException
    {
        boolean isTimestampValid = false;
        String cookieCheckCookie = FraudDetector.getCookiesEnabledCheckCookie(request);

        if (cookieCheckCookie != null) {
            long currentTime = System.currentTimeMillis() / 1000L;
            long cookieTime = FraudDetector.decodeCookie(cookieCheckCookie);

            if (cookieTime <= currentTime && (cookieTime + 60L) > currentTime) {
                isTimestampValid = true;
            }
        }

        if (!isTimestampValid) {
            LOG.log(Level.FINE, "Invalid(too old) timestamp found in cookie.");
        }
        
        return isTimestampValid;
    }

    private boolean isEmpty(String value)
    {
        return (value == null) || (value.trim().length() == 0);
    }
    
    
    /*
     * Represents an invalid poll answer. Does nothing and returns
     * null when trying to vote with it.
     */
    private class InvalidPollAnswer extends PollAnswer {

        public InvalidPollAnswer() {
            super();
        }

        /*
         * Does nothing.
         */
        protected String submitAnswer(HttpServletRequest request,
                                      HttpServletResponse response)
            throws CMException
        {
            return null;
        }
    }

    /*
     * Handles the voting
     */
    private class PollAnswer {

        private final String pollId;
        private final String questionId;
        private final String optionId;
        
        protected PollAnswer() {
            pollId = null;
            questionId = null;
            optionId = null;
        }

        public PollAnswer(String pollId,
                          String questionId,
                          String optionId)
        {
            this.pollId = pollId;
            this.questionId = questionId;
            this.optionId = optionId;
        }
        
        /*
         * Does actual voting. Returns the option id if successful.
         */
        protected String submitAnswer(HttpServletRequest request,
                                    HttpServletResponse response)
                throws CMException
        {
            if (!FraudDetector.isLocked(request, pollId)) {
                
                // Vote
                boolean voteSuccessfullyRegistered = pollClient
                        .getPollManager().vote(pollId, questionId, optionId);

                if (voteSuccessfullyRegistered) {
                    // Register vote in cookie
                    FraudDetector.setLock(request, response, pollId);
                    return optionId;
                } else {
                    // If the vote couldn't be submitted we log at FINE to avoid
                    // filling the log with thousands of warnings
                    LOG.log(Level.FINE, "Couldn't register vote! Poll id: "
                            + pollId + ", question id: " + questionId
                            + ", option id: " + optionId);
                }
            }
            else {
                request.setAttribute("p.Poll.AlreadyVoted", true);
            }
            
            return null;
        }
    }
}
