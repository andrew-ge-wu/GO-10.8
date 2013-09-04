package example.akismet;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Semaphore;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import example.JettyWrapper;

public class AkismetClientTest extends TestCase {

    public static final String VERIFY_KEY_SERVLET_URI = "/"
            + AkismetClientImpl.METHOD_VERIFY_KEY;

    public static final String VERIFY_KEY_SERVLET_MAPPING = VERIFY_KEY_SERVLET_URI
            + "/*";

    public static final String COMMENT_CHECK_SERVLET_URI = "/"
            + AkismetClientImpl.METHOD_COMMENT_CHECK;

    public static final String COMMENT_CHECK_SERVLET_MAPPING = COMMENT_CHECK_SERVLET_URI
            + "/*";

    private JettyWrapper jetty;

    private AkismetVerifyKeyServlet verifyKeyServlet;

    private AkismetCommentCheckServlet commentCheckServlet;

    private AkismetClientImpl akismetClient;

    private Semaphore connectionAvailable;

    private static final int MAX_PERMITS = 1;

    private static final int SOCKET_TIMEOUT = 500;
    
    private static final int CONNECTION_TIMEOUT = 500;

    private static final String VALID_API_KEY = "valid";

    private static final String INVALID_API_KEY = "invalid";

    private static final String HOST_URL = "http://localhost";

    private static final String SITE_URL = HOST_URL + "/blog";

    private static final String NO_SPAM_COMMENT = "Very nice and sweet";

    private static final String SPAM_COMMENT = "Viagra!";

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        jetty = new JettyWrapper();

        verifyKeyServlet = new AkismetVerifyKeyServlet();
        jetty.addServlet(verifyKeyServlet, VERIFY_KEY_SERVLET_MAPPING);

        commentCheckServlet = new AkismetCommentCheckServlet();
        jetty.addServlet(commentCheckServlet, COMMENT_CHECK_SERVLET_MAPPING);

        jetty.start();

        connectionAvailable = new Semaphore(MAX_PERMITS, true);

        akismetClient = new AkismetClientImpl(VALID_API_KEY, SITE_URL, null,
                -1, null, null, SOCKET_TIMEOUT, CONNECTION_TIMEOUT, connectionAvailable);
        akismetClient.setAkismetBaseUrl(getURL("").toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        try {
            if (jetty != null) {
                jetty.stop();
                jetty = null;
            }
        } finally {
            super.tearDown();
        }
    }

    public void testFakeAkismetRestService() throws Exception {

        // Test verify key service (valid key)
        String reqUrl = VERIFY_KEY_SERVLET_URI + "?"
                + AkismetClientImpl.API_PARAMETER_KEY + "=" + VALID_API_KEY
                + "&" + AkismetClientImpl.API_PARAMETER_BLOG + "=" + SITE_URL;
        HttpURLConnection connection = (HttpURLConnection) getURL(reqUrl)
                .openConnection();
        connection.setRequestMethod("POST");
        assertEquals("Expected HTTP status code OK", HttpURLConnection.HTTP_OK,
                connection.getResponseCode());
        String responseBody = getBody(connection.getInputStream());
        assertEquals("Expected key valid",
                AkismetClientImpl.VERIFY_API_KEY_SUCCESS_RESPONSE, responseBody);

        // Test verify key service (invalid key)
        reqUrl = VERIFY_KEY_SERVLET_URI + "?"
                + AkismetClientImpl.API_PARAMETER_KEY + "=" + INVALID_API_KEY
                + "&" + AkismetClientImpl.API_PARAMETER_BLOG + "=" + SITE_URL;
        connection = (HttpURLConnection) getURL(reqUrl).openConnection();
        assertEquals("Expected HTTP status code OK", HttpURLConnection.HTTP_OK,
                connection.getResponseCode());
        responseBody = getBody(connection.getInputStream());
        assertEquals("Expected key invalid",
                AkismetClientImpl.VERIFY_API_KEY_FAILURE_RESPONSE, responseBody);

        // Test check comment service (no spam)
        reqUrl = COMMENT_CHECK_SERVLET_URI + "?"
                + AkismetClientImpl.API_PARAMETER_COMMENT_CONTENT + "="
                + URLEncoder.encode(NO_SPAM_COMMENT, "UTF-8");
        connection = (HttpURLConnection) getURL(reqUrl).openConnection();
        assertEquals("Expected HTTP status code OK", HttpURLConnection.HTTP_OK,
                connection.getResponseCode());
        responseBody = getBody(connection.getInputStream());
        assertEquals("Expected no spam comment", Boolean.FALSE.toString(),
                responseBody);

        // Test check comment service (spam)
        reqUrl = COMMENT_CHECK_SERVLET_URI + "?"
                + AkismetClientImpl.API_PARAMETER_COMMENT_CONTENT + "="
                + URLEncoder.encode(SPAM_COMMENT, "UTF-8");
        connection = (HttpURLConnection) getURL(reqUrl).openConnection();
        assertEquals("Expected HTTP status code OK", HttpURLConnection.HTTP_OK,
                connection.getResponseCode());
        responseBody = getBody(connection.getInputStream());
        assertEquals("Expected spam comment", Boolean.TRUE.toString(),
                responseBody);
    }

    public void testValidKey() throws Exception {

        // Test valid key
        boolean response = akismetClient.verifyAPIKey();
        assertEquals("Expected key valid", true, response);

        // // Test valid key
        verifyKeyServlet.correctKey = INVALID_API_KEY;
        response = akismetClient.verifyAPIKey();
        assertEquals("Expected key invalid", false, response);
    }

    public void testComment() throws Exception {

        HttpServletRequest request = getMockedRequest();

        // Test no spam comment
        boolean response = checkComment(request, NO_SPAM_COMMENT);
        assertEquals("Expected no spam", false, response);

        // Test spam comment
        response = checkComment(request, SPAM_COMMENT);
        assertEquals("Expected no spam", true, response);

        verify(request, atLeastOnce()).getRemoteAddr();
        verify(request, atLeastOnce()).getHeader(AkismetClientImpl.HTTP_REFERER);
        verify(request, atLeastOnce()).getHeader(AkismetClientImpl.USER_AGENT_HEADER);

    }

    public void testHttpFailure() throws Exception {

        // Set REST service to return HTTP failure
        verifyKeyServlet.statusCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
        try {
            akismetClient.verifyAPIKey();
            fail("Expected to get connection failure");
        } catch (AkismetException e) {
            // Success 
        }
    }

    private boolean checkComment(HttpServletRequest request, String comment)
            throws AkismetException {

        return akismetClient.commentCheck(request, "http://localhost/blog",
                AkismetClient.COMMENT_TYPE_COMMENT, "test", "test@test.com",
                "http://test.com/", comment);

    }

    private String getBody(InputStream io) throws IOException {

        BufferedReader in = new BufferedReader(new InputStreamReader(io));
        StringBuffer actual = new StringBuffer();
        String line = null;
        while ((line = in.readLine()) != null) {
            actual.append(line);
        }
        in.close();

        return actual.toString();
    }

    private URL getURL(String servletUri) throws Exception {
        return new URL(HOST_URL + ":" + jetty.getPort() + servletUri);
    }

    private HttpServletRequest getMockedRequest() {
        
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        when(request.getHeader(AkismetClientImpl.HTTP_REFERER)).thenReturn("Akismet Test/1.0");

        when(request.getHeader(AkismetClientImpl.USER_AGENT_HEADER)).thenReturn("http://localhost/blog");

        return request;
    }

    private class AkismetServlet extends HttpServlet {

        private static final long serialVersionUID = -962108380231385961L;

        int statusCode = HttpServletResponse.SC_OK;

        void sendReponse(ServletResponse res, String response)
                throws IOException {
            if (res instanceof HttpServletResponse
                    && statusCode != HttpServletResponse.SC_OK) {
                ((HttpServletResponse) res).sendError(statusCode, "Error");
            } else {
                res.setContentType("text/plain; charset=UTF-8");
                res.getWriter().print(response);
            }
            res.flushBuffer();
        }
    }

    private class AkismetCommentCheckServlet extends AkismetServlet {

        private static final long serialVersionUID = -2006947252766572635L;

        String niceComment = NO_SPAM_COMMENT;

        boolean isInvalidKey = false;

        public void service(ServletRequest req, ServletResponse res)
                throws ServletException, IOException {

            String comment = req
                    .getParameter(AkismetClientImpl.API_PARAMETER_COMMENT_CONTENT);

            String response = Boolean.toString(false);
            if (isInvalidKey) {
                response = AkismetClientImpl.VERIFY_API_KEY_FAILURE_RESPONSE;
            } else if (!niceComment.equals(comment)) {
                response = Boolean.toString(true);
            }

            sendReponse(res, response);
        }
    }

    private class AkismetVerifyKeyServlet extends AkismetServlet {

        private static final long serialVersionUID = 5634778127564211241L;

        String correctKey = VALID_API_KEY;

        public void service(ServletRequest req, ServletResponse res)
                throws ServletException, IOException {

            String response = AkismetClientImpl.VERIFY_API_KEY_FAILURE_RESPONSE;

            String key = req.getParameter(AkismetClientImpl.API_PARAMETER_KEY);
            String blog = req
                    .getParameter(AkismetClientImpl.API_PARAMETER_BLOG);

            if (key != null && correctKey.equals(key.trim()) && blog != null
                    && !"".equals(blog.trim())) {
                response = AkismetClientImpl.VERIFY_API_KEY_SUCCESS_RESPONSE;
            }

            sendReponse(res, response);
        }
    }
}
