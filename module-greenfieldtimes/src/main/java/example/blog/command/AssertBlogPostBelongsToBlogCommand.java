package example.blog.command;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.policy.PolicyCMServer;

import example.blog.BlogContext;
import example.blog.RenderControllerBlog;
import example.util.Context;

public class AssertBlogPostBelongsToBlogCommand
    extends BlogPostCommandBase
{
    private static final Logger LOG = Logger.getLogger(AssertBlogPostBelongsToBlogCommand.class.getName());
    
    public boolean execute(Context context)
    {
        BlogContext blogContext = (BlogContext) context;

        PolicyCMServer cmServer = blogContext.getPolicyCMServer();
        ContentId blogContentId = blogContext.getBlogContentId();
        ContentId blogPostContentId = blogContext.getBlogPostContentId();
        
        try {
            ContentRead blogPost = cmServer.getContent(blogPostContentId);
            ContentId blogPostSecurityParentId = blogPost.getSecurityParentId();
            
            if (blogPostSecurityParentId.equals(blogContentId)) {
                return true;
            }
            blogContext.addErrorShowBlogView(RenderControllerBlog.PERMISSION_DENIED);
            
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Error while retrieving blog post content.", e);
            blogContext.addErrorShowBlogView(RenderControllerBlog.INTERNAL_SERVER_ERROR);
        }
 
        return false;
    }
}
