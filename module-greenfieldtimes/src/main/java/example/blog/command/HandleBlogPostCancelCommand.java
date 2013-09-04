package example.blog.command;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;

import example.blog.BlogContext;
import example.blog.BlogPostPolicy;
import example.blog.RenderControllerBlog;
import example.util.Context;

public class HandleBlogPostCancelCommand extends BlogPostCommandBase
{
    private static final Logger LOG = Logger.getLogger(HandleBlogPostCancelCommand.class.getName());

    public boolean execute(Context context)
    {
        BlogContext blogContext = (BlogContext) context;
        PolicyCMServer cmServer = blogContext.getPolicyCMServer();
        ContentId blogPostId = blogContext.getBlogPostContentId();
        
        if (null != blogPostId && imageUploadCreatedNewContentVersion(blogPostId)) {
            try {
                BlogPostPolicy blogPost = (BlogPostPolicy) cmServer.getPolicy(blogPostId);
                cmServer.abortContent(blogPost, true);
            } catch (CMException e) {
                LOG.log(Level.WARNING, "Error while cancelling blog post content.", e);
                blogContext.addErrorShowBlogView(RenderControllerBlog.CANCEL_BLOG_POST_UPDATE_ERROR);
                return false;
            }
        }
        
        return true;
    }

}
