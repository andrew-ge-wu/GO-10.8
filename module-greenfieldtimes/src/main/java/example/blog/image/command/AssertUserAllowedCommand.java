package example.blog.image.command;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.user.server.User;

import example.blog.BlogPolicy;
import example.blog.image.BlogImageContext;
import example.blog.image.FckEditorUploadResponse;
import example.membership.UserHandler;
import example.util.Command;
import example.util.Context;

public class AssertUserAllowedCommand implements Command
{
    private static final Logger LOG = Logger.getLogger(AssertUserAllowedCommand.class.getName());
    
    public boolean execute(Context context)
    {
        BlogImageContext blogImageContext = (BlogImageContext) context;

        PolicyCMServer cmServer = blogImageContext.getPolicyCMServer();
        HttpServletRequest servletRequest = blogImageContext.getRequest();
        
        FckEditorUploadResponse fckEditorResponse = blogImageContext.getFckEditorResponse();

        try {
            ContentId blogContentId = blogImageContext.getBlogContentId();
            UserHandler userHandler = blogImageContext.getUserHandler();

            User user = userHandler.getLoggedInUser(servletRequest, null);
            
            if (user == null) {
                LOG.log(Level.WARNING, "Not logged in user tried to edit blog post.");
                fckEditorResponse.setStatus(FckEditorUploadResponse.Status.PERMISSION_DENIED);
                
                return false;
            }

            BlogPolicy blog = (BlogPolicy) cmServer.getPolicy(blogContentId);
            boolean isAllowedToEdit = blog.isAllowedToEdit(user.getUserId());
            
            if (!isAllowedToEdit) {
                LOG.log(Level.WARNING, "Non-owner user tried to edit blog post.");
                fckEditorResponse.setStatus(FckEditorUploadResponse.Status.PERMISSION_DENIED);
                
                return false;
            }
            
            return true;
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Could not get blog content from server.", e);
            fckEditorResponse.setStatus(FckEditorUploadResponse.Status.SERVER_ERROR);
        } catch (RemoteException e) {
            LOG.log(Level.SEVERE, "Program bug, should never happen.", e);
            fckEditorResponse.setStatus(FckEditorUploadResponse.Status.SERVER_ERROR);
        }

        return false;
    }
}
