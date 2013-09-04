package example.membership;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ObjectNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.polopoly.user.server.AuthenticationFailureException;
import com.polopoly.user.server.PermissionDeniedException;

public class ActionLogout implements Action {

    private final UserHandler userHandler;
    private Logger LOG = Logger.getLogger(ActionLogout.class.getName());

    public ActionLogout(UserHandler userHandler) {
        this.userHandler = userHandler;
    }

    public void perform(HttpServletRequest request,
                        HttpServletResponse response)
        throws IOException, ServletException
    {
        try {
            userHandler.logout(request, response);
        } catch (PermissionDeniedException e) {
            LOG.log(Level.FINE, "Permission denied on logout.", e);
        } catch (ServletException e) {
            LOG.log(Level.FINE, "Logout failed.", e);
        } catch (ObjectNotFoundException e) {
            LOG.log(Level.FINE, "Object not found on logout.", e);
        } catch (AuthenticationFailureException e) {
            LOG.log(Level.FINE, "Authentication failed on logout.", e);
        }
        
        response.sendRedirect("/");
    }
}