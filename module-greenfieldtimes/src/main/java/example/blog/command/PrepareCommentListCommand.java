package example.blog.command;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.community.comment.CommentList;
import com.polopoly.community.comment.CommentListFactory;
import com.polopoly.community.list.ContentIdListSlice;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;

import example.blog.CmRenderContext;
import example.util.Command;
import example.util.Context;
import example.util.RequestParameterUtil;

public class PrepareCommentListCommand implements Command
{
    public static final int COMMENTS_PER_PAGE = 20;

    private static final Logger LOG = Logger.getLogger(PrepareCommentListCommand.class.getName());
    private final RequestParameterUtil requestParameterUtil = new RequestParameterUtil();

    public boolean execute(Context context)
    {
        CmRenderContext cmRenderContext = (CmRenderContext) context;

        CommentList list = new CommentListFactory().create(cmRenderContext.getCmClient(),
                                                           cmRenderContext.getCurrentContentId());
        populateModelWithComments(cmRenderContext, list);
        return true;
    }

    protected void populateModelWithComments(CmRenderContext cmRenderContext, CommentList comments)
    {
        ModelWrite localModel = cmRenderContext.getLocalModel();
        PolicyCMServer cmServer = cmRenderContext.getPolicyCMServer();
        RenderRequest request = cmRenderContext.getRenderRequest();

        try {
            int showCommentIndex = getShownCommentIndex(request, comments);
            int index = getIndex(request);
            int shownIsAt = -1;
            boolean useShowCommentIndex = "-1".equals(request.getParameter("comments"));
            if (showCommentIndex != -1 && useShowCommentIndex) {
                index = showCommentIndex - (showCommentIndex % COMMENTS_PER_PAGE);
            }
            shownIsAt = showCommentIndex - index;

            if (index > comments.getNumberOfItemsAddedToList()) {
                index = 0;
                shownIsAt = -1;
            }

            ArrayList<Policy> commentPolicies = new ArrayList<Policy>();

            ContentIdListSlice slice = comments.getSlice(index, COMMENTS_PER_PAGE);
            int currentIndex = 0;
            Policy foundPolicy = null;
            for (ContentId id : slice.getContentIds()) {
                Policy policy = cmServer.getPolicy(id);
                commentPolicies.add(policy);
                if (currentIndex == shownIsAt) {
                    foundPolicy = policy;
                }
                currentIndex = currentIndex + 1;
            }
            if (foundPolicy != null) {
                localModel.setAttribute("showComment", foundPolicy);
            }
            localModel.setAttribute("comments", commentPolicies);
            localModel.setAttribute("commentsIndex", index);
            localModel.setAttribute("commentsLimit", COMMENTS_PER_PAGE);
            if (slice.getNextSliceStartIndex() > 0) {
                localModel.setAttribute("commentsNextIndex", slice.getNextSliceStartIndex());
            }
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Unable to get comments", e);
        }
    }

    private int getShownCommentIndex(RenderRequest request, CommentList comments)
    {
        if (request.getParameter("showCommentId") != null) {
            try {
                ContentId contentId = null;
                try {
                    contentId = ContentIdFactory.createContentId(request.getParameter("showCommentId"));
                } catch (IllegalArgumentException e) {
                    return -1;
                }
                ContentIdListSlice slice = comments.getSlice(0, COMMENTS_PER_PAGE);
                int start = slice.getContentIds().indexOf(contentId);
                int count = 0;
                while (start == -1 && slice.getNextSliceStartIndex() > 0) {
                    count = count + 1;
                    slice = comments.getSlice(slice.getNextSliceStartIndex(), COMMENTS_PER_PAGE);
                    start = slice.getContentIds().indexOf(contentId);
                }
                if (start == -1) {
                    return -1;
                }
                return count * COMMENTS_PER_PAGE + start;
            } catch (CMException e) {
            }
        }
        return -1;
    }

    public int getIndex(RenderRequest request)
    {
        return requestParameterUtil.getInt(request, "comments", 0, 0, Integer.MAX_VALUE);
    }
}
