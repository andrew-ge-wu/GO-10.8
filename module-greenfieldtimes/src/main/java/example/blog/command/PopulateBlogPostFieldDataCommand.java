package example.blog.command;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.ModelWrite;

import example.blog.BlogContext;
import example.blog.BlogPostPolicy;
import example.blog.RenderControllerBlog;
import example.content.BodyTranslator;
import example.util.Context;

public class PopulateBlogPostFieldDataCommand
    extends BlogPostCommandBase
{
    private static final Logger LOG = Logger.getLogger(PopulateBlogPostFieldDataCommand.class.getName());

    public boolean execute(Context context)
    {
        BlogContext blogContext = (BlogContext) context;

        ModelWrite localModel = blogContext.getLocalModel();
        PolicyCMServer cmServer = blogContext.getPolicyCMServer();
        
        BlogPostPolicy blogPost;
        
        ContentId blogPostContentId = blogContext.getEditPostId();
        
        if (null == blogPostContentId) {
            blogContext.addErrorShowBlogView(RenderControllerBlog.INTERNAL_SERVER_ERROR);
            return false;
        }
        
        try {
            blogPost = (BlogPostPolicy) cmServer.getPolicy(blogPostContentId);

            String blogPostName = blogPost.getName();
            String blogPostText = blogPost.getText();
            
            BodyTranslator bodyTranslator = new BodyTranslator();
            
            blogPostText = bodyTranslator.translateBody(blogContext.getRenderRequest(),
                                                        blogPostContentId.getContentId(),
                                                        blogPostText, false, true);
            
            localModel.setAttribute(RenderControllerBlog.BLOG_POST_ECHO_NAME, blogPostName);
            localModel.setAttribute(RenderControllerBlog.BLOG_POST_ECHO_TEXT, blogPostText);
            return true;
            
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Error while retrieving blog post content.", e);
            blogContext.addErrorShowBlogView(RenderControllerBlog.INTERNAL_SERVER_ERROR);
        }
        return false;
    }
}
