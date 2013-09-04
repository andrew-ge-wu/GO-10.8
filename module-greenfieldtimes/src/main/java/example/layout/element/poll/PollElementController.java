package example.layout.element.poll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.DefaultMajorNames;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.path.ContentPathCreator;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.servlet.RequestPreparator;
import com.polopoly.cm.servlet.URLBuilder;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.model.ModelWrite;
import com.polopoly.poll.client.PollClient;
import com.polopoly.poll.client.PollManager;
import com.polopoly.poll.policy.QuestionPolicy;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.util.StringUtil;

import example.content.RenderControllerExtended;

/**
 * Controller for poll elements. Prepares for rendering of the poll form or the result page.
 */
public class PollElementController extends RenderControllerExtended {

    private static final Logger LOG = Logger.getLogger(PollElementController.class.getName());
    private static Map<String, String> params = new HashMap<String, String>(1);

    public PollElementController()
    {
        // The "ot" parameter specifies which template to use for the result page.
        params.put("ot", "example.PopupPageLayout.ot");
    }

    public void populateModelBeforeCacheKey(RenderRequest request, TopModel m,
            ControllerContext context)
    {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            ModelWrite localModel = m.getLocal();
            Model contentModel = context.getContentModel();

            // Model representing an Single Poll
            Model singlePollModel = (Model) contentModel.getAttribute("singlePollField");

            // View is set to 'form-view' as default
            SinglePollElementBean bean = new SinglePollElementBean();
            bean.setRegisterPollId((String) singlePollModel.getAttribute("pollId"));
            
            bean.setTriedToVote(isSubmit(httpRequest));
            Boolean hadAlreadyVoted = (Boolean) request.getAttribute("p.Poll.AlreadyVoted");
            if (hadAlreadyVoted != null) {
                bean.setHadAlreadyVoted(hadAlreadyVoted.booleanValue());
            }
            
            if (isSubmit(httpRequest) || isShowResult(httpRequest)) {
                
                PollClient pollClient = (PollClient)
                    context.getApplication()
                        .getApplicationComponent(PollClient.DEFAULT_COMPOUND_NAME);
                
                prepareForResultView(contentModel, bean,
                                     httpRequest, _policyCMServerProvider.getPolicyCMServer(context),
                                     pollClient);
            
            } else {
                prepareForFormView(context, httpRequest, contentModel, bean);
            }

            localModel.setAttribute(SinglePollElementBean.BEAN_ID, bean);
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Problem showing poll view.", e);
        }
    }
    
    public void populateModelAfterCacheKey(RenderRequest request, TopModel m, CacheInfo cacheInfo,
            ControllerContext context)
    {
        ModelWrite localModel = m.getLocal();
        SinglePollElementBean bean = (SinglePollElementBean)
            localModel.getAttribute(SinglePollElementBean.BEAN_ID);
        
        if (bean.getVotedOptionId() != null) {
            cacheInfo.setPrivate(true);
        }
    }

    private void prepareForFormView(ControllerContext context,
                                    HttpServletRequest httpRequest, Model contentModel,
                                    SinglePollElementBean bean)
        throws CMException
    {
        bean.setUrlToItSelf(getResultUrl(context, httpRequest));
        bean.setAnswerOptionsLayout(getAnswerOptionsLayout(contentModel));
    }

    /**
     * Should answer options be rendered in a  "Horizontal" or "Vertical" manner.
     */
    private String getAnswerOptionsLayout(Model contentModel) {
        String answerOptionsLayout =
                (String) ModelPathUtil.get(contentModel, "answerOptionsLayout/value");

        if (answerOptionsLayout == null) {
            answerOptionsLayout = "Horizontal";
        }
        return answerOptionsLayout;
    }

    /**
     * Get the URL of a page displaying the poll result.
     */
    private String getResultUrl(ControllerContext context,
                                HttpServletRequest httpRequest)
        throws CMException
    {
        URLBuilder urlBuilder = RequestPreparator.getURLBuilder(httpRequest);
        ContentPathCreator pathCreator = RequestPreparator.getPathCreator(httpRequest);
        
        ContentId[] fullPath =
            pathCreator.createPath(context.getContentId(),
                                   _policyCMServerProvider.getPolicyCMServer(context));
        
        // Keep only departments (e.g. for css) and the poll content itself
        List<ContentId> trimmedPath = new ArrayList<ContentId>();
        int departmentMajor =
            _policyCMServerProvider.getPolicyCMServer(context).getMajorByName(DefaultMajorNames.DEPARTMENT);
        for (ContentId cid : fullPath) {
            if (cid.getMajor() == departmentMajor) {
                trimmedPath.add(cid);
            }
        }
        trimmedPath.add(context.getContentId());
                
        //Build URL from Path
        return urlBuilder.createUrl(trimmedPath.toArray(new ContentId[trimmedPath.size()]), params, httpRequest);
    }

    long calculateTotalVoteCount(QuestionPolicy.Option[] options, Map<String, Integer> pollResult, String votedOptionId) {
        long totalVoteCount = 0;
        for (int i = 0; i < options.length; i++) {
            String optionId = options[i].getId();

            long count = getCountAndIncrementIfVoted(pollResult,
                    votedOptionId, optionId);
            long offset = options[i].getOffset();

            if (offset > 0) {
                count += offset;
            }
            
            totalVoteCount += count;
        }

        return totalVoteCount;
    }

    @SuppressWarnings("unchecked")
    private void prepareForResultView(Model contentModel, SinglePollElementBean bean,
            HttpServletRequest httpRequest, PolicyCMServer policyCMServer, PollClient pollClient)
    {
        bean.setView("result-view");
        
        PollManager pollManager = pollClient.getPollManager();

        
        // Model representing an Question Policy
        Model questionModel = (Model) contentModel.getAttribute("questionField");

        // Get total number of submitted votes
        long totalVoteCount = 0L;
        QuestionPolicy.Option[] options = (QuestionPolicy.Option[])
            questionModel.getAttribute("options");
        
        String[] optionIds = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            QuestionPolicy.Option option = options[i];
            optionIds[i] = option.getId();
        }

        Map<String, Integer> pollResult =
                pollManager.getVotes(bean.getRegisterPollId(), "0", optionIds, policyCMServer
                        .getCurrentCaller());

        String votedOptionId = (String) httpRequest.getAttribute("poll.votedOptionId");
        
        // Most of the time we get hasVoted from the cookie set by the voting,
        // but the request that sends the vote doesn't have a cookie since we
        // forward from the servlet, so we have to use this to figure out if the
        // user has voted
        
        if (votedOptionId != null) {
            bean.setTriedToVote(true);
            bean.setVotedOptionId(votedOptionId);
        }

        totalVoteCount = calculateTotalVoteCount(options, pollResult, votedOptionId);
        
        bean.setTotalVoteCount(totalVoteCount);

        QuestionPolicy.Value[] results = new QuestionPolicy.Value[options.length];
        
        for (int i = 0; i < options.length; i++) {
            QuestionPolicy.Option option = options[i];
            
            QuestionPolicy.Value value =
                    new QuestionPolicy.Value(option, pollResult.get(option.getId())
                            .longValue(), totalVoteCount);
            
            results[i] = value;
        }

        bean.setResults(results);
    }

    private int getCountAndIncrementIfVoted(Map<String, Integer> pollResult,
            String votedOptionId, String key)
    {
        int count = pollResult.get(key).intValue();
        
        if (votedOptionId != null && votedOptionId.equals(key)) {
            count++;
            
            pollResult.put(key, new Integer(count));
        }
        
        return count;
    }

    private boolean isShowResult(HttpServletRequest request)
    {
        return !StringUtil.isEmpty(request.getParameter("showResult"));
    }

    private boolean isSubmit(HttpServletRequest request)
    {
        return !StringUtil.isEmpty(request.getParameter("pollId"));
    }

    public static class SinglePollElementBean {

        public static final String BEAN_ID = "singlePollElementBean";

        private String urlToItSelf;
        private String registerPollId;
        // Set if the user tried to vote this request
        private boolean hasVoted;
        // Set if the user tried to vote but had already done so
        private boolean hadAlreadyVoted;
        private String view = "form-view"; // 'form-view', 'result-view', ''
        private long totalVoteCount;
        private QuestionPolicy.Value[] results;
        private String answerOptionsLayout;
        private String votedOptionId;


        public QuestionPolicy.Value[] getResults()
        {
            return results;
        }

        public void setResults(QuestionPolicy.Value[] results)
        {
            this.results = results;
        }

        public long getTotalVoteCount()
        {
            return totalVoteCount;
        }

        public void setTotalVoteCount(long totalVoteCount)
        {
            this.totalVoteCount = totalVoteCount;
        }

        public boolean isTriedToVote()
        {
            return hasVoted;
        }

        public void setTriedToVote(boolean hasVoted)
        {
            this.hasVoted = hasVoted;
        }
        
        public boolean isHadAlreadyVoted() {
            return hadAlreadyVoted;
        }
        
        public void setHadAlreadyVoted(boolean hadAlreadyVoted) {
            this.hadAlreadyVoted = hadAlreadyVoted;
        }

        public String getUrlToItSelf()
        {
            return urlToItSelf;
        }

        public void setUrlToItSelf(String urlToItSelf)
        {
            this.urlToItSelf = urlToItSelf;
        }

        public String getRegisterPollId()
        {
            return registerPollId;
        }

        public void setRegisterPollId(String registerPollId)
        {
            this.registerPollId = registerPollId;
        }

        public String getView()
        {
            return view;
        }

        public void setView(String view)
        {
            this.view = view;
        }

        public String getAnswerOptionsLayout()
        {
            return answerOptionsLayout;
        }

        public void setAnswerOptionsLayout(String answerOptionsLayout)
        {
            this.answerOptionsLayout = answerOptionsLayout;
        }

        public void setVotedOptionId(String votedOptionId)
        {
            this.votedOptionId = votedOptionId;
        }

        public String getVotedOptionId()
        {
            return votedOptionId;
        }
    }
}
