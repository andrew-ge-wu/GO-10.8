package example.blog.command;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.render.RenderRequest;
import com.polopoly.user.server.User;

import example.blog.BlogContext;
import example.blog.BlogPolicy;
import example.blog.RenderControllerBlog;
import example.membership.UserHandler;
import example.util.Context;

public class AssertUserAllowedCommand
    extends BlogPostCommandBase
{
    
    private static final Logger LOG = Logger.getLogger(AssertUserAllowedCommand.class.getName());
    
    public boolean execute(Context context) {
        BlogContext blogContext = (BlogContext) context;

        PolicyCMServer cmServer = blogContext.getPolicyCMServer();
        ContentId blogContentId = blogContext.getBlogContentId();
        RenderRequest renderRequest = blogContext.getRenderRequest();

        try {
            UserHandler userHandler = blogContext.getUserHandler();
            HttpServletRequest servletRequest = (HttpServletRequest) renderRequest;
            
            User user = userHandler.getLoggedInUser(servletRequest, null);
            
            if (isAllowed(blogContext, cmServer, blogContentId, user)) {
                return true;
            }
            blogContext.addErrorShowBlogView(RenderControllerBlog.PERMISSION_DENIED);
        } catch (CMException e) {
            blogContext.addErrorShowBlogView(RenderControllerBlog.INTERNAL_SERVER_ERROR);
            LOG.log(Level.WARNING, "Could not get blog content from server.", e);
        } catch (RemoteException e) {
            blogContext.addErrorShowBlogView(RenderControllerBlog.INTERNAL_SERVER_ERROR);
            LOG.log(Level.SEVERE, "This should never happen.", e);
        }
        return false;
    }

    private boolean isAllowed(BlogContext blogContext, PolicyCMServer cmServer,
                              ContentId blogContentId, User user)
        throws CMException, RemoteException
    {
        if (null == user) {
            return false;
        }
        
        BlogPolicy blog = (BlogPolicy) cmServer.getPolicy(blogContentId);
        return blog.isAllowedToEdit(user.getUserId());
    }
}
