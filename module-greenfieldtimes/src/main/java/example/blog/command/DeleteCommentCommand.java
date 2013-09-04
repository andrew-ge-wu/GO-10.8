package example.blog.command;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.render.RenderRequest;
import com.polopoly.user.server.Caller;

import example.blog.CmRenderContext;
import example.blog.RenderControllerBlog;
import example.comment.CommentPolicy;
import example.util.Command;
import example.util.Context;

public abstract class DeleteCommentCommand implements Command
{
    private static final Logger LOG = Logger.getLogger(DeleteCommentCommand.class.getName());

    public boolean execute(Context context)
    {
        CmRenderContext cmContext = (CmRenderContext) context;
        RenderRequest renderRequest = cmContext.getRenderRequest();
        PolicyCMServer cmServer = cmContext.getPolicyCMServer();

        Caller currentCaller = cmServer.getCurrentCaller();
        
        try {
            
            String commentIdString = getCommentIdString(renderRequest);
            
            ContentId commentId = ContentIdFactory.createContentId(commentIdString);
            
            VersionedContentId latestCommittedVersionId =
                commentId.getLatestCommittedVersionId();
            CommentPolicy commentPolicy = (CommentPolicy)
                cmServer.createContentVersion(latestCommittedVersionId);
            commentPolicy.delete();
            cmServer.commitContent(commentPolicy);

            return true;
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Error while deleting comment.", e);
            showError(cmContext, RenderControllerBlog.DELETE_COMMENT_ERROR);
        } finally {
            if (currentCaller != null) {
                cmServer.setCurrentCaller(currentCaller);
            }
        }
        return false;
    }
    
    protected abstract void showError(Context context, String errorKey);

    protected abstract boolean isPostingToThisContent(CmRenderContext cmContext);
    
    protected ContentId getCommentsIdPostParameter(Context context)
    {
        return ((CmRenderContext) context).getContentIdFromRequest(RenderControllerBlog.COMMENTS_ID);
    }

    private String getCommentIdString(RenderRequest request)
    {
        return request.getParameter("commentId");
    }
}
