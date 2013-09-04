package example.blog.command;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.user.server.User;

import example.blog.BlogContext;
import example.membership.UserHandler;
import example.util.Command;
import example.util.Context;

public class EnsureLoggedInCommand
    implements Command
{
    private static final Logger LOG = Logger.getLogger(EnsureLoggedInCommand.class.getName());

    public boolean execute(Context context)
    {
        if (context instanceof BlogContext) {
            BlogContext blogContext = (BlogContext) context;

            HttpServletRequest httpRequest = (HttpServletRequest) blogContext.getRenderRequest();
            
            UserHandler userHandler = blogContext.getUserHandler();
            User loggedInUser = userHandler.getLoggedInUser(httpRequest, null);

            if (null == loggedInUser) {
                LOG.log(Level.FINE, "User is not logged in!");
                blogContext.addBlogErrorMessageToLocalModel("login-error", "not-logged-in");
                return false;
            }
            
            blogContext.setLoggedInUser(loggedInUser);
        }
        
        return true;
    }
}
