package example.blog.command;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.ModelWrite;

import example.blog.BlogContext;
import example.blog.BlogForm;
import example.blog.BlogPolicy;
import example.blog.RenderControllerBlog;
import example.util.Command;
import example.util.Context;

public class PopulateBlogFieldDataCommand
    implements Command
{
    private static final Logger LOG =
            Logger.getLogger(PopulateBlogFieldDataCommand.class.getName());

    public boolean execute(Context context)
    {
        BlogContext blogContext = (BlogContext) context;

        ModelWrite localModel = blogContext.getLocalModel();

        ContentId blogContentId = blogContext.getBlogContentId();
        PolicyCMServer cmServer = blogContext.getPolicyCMServer();

        BlogPolicy blog;

        try {
            blog = (BlogPolicy) cmServer.getPolicy(blogContentId);

            String blogName = blog.getName();
            String blogDescription = blog.getDescription();
            String blogAddress = blog.getPathSegmentString();

            BlogForm blogForm = new BlogForm(blogName, blogDescription, blogAddress, null);
            
            localModel.setAttribute("blogForm", blogForm);

            localModel.setAttribute(RenderControllerBlog.IS_BLOG_EDIT, Boolean.TRUE);

            return true;
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Error while retrieving blog content.", e);
            blogContext.addErrorShowBlogView(RenderControllerBlog.INTERNAL_SERVER_ERROR);
        }
        return false;
    }
}
