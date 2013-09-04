package example.membership;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.polopoly.user.server.AuthenticationFailureException;
import com.polopoly.user.server.Caller;
import com.polopoly.user.server.InvalidLoginNameException;
import com.polopoly.user.server.InvalidPasswordException;
import com.polopoly.user.server.NotUniqueException;
import com.polopoly.user.server.PermissionDeniedException;
import com.polopoly.user.server.User;
import com.polopoly.user.server.jsp.UserFactory;

public class UserHandlerImpl implements UserHandler
{
    private static final String CLASS = UserHandlerImpl.class.getName();
    private static final Logger LOG = Logger.getLogger(CLASS);

    public User login(HttpServletRequest request, HttpServletResponse response,
                      String loginName, String password)
        throws ObjectNotFoundException, ServletException,
               AuthenticationFailureException, PermissionDeniedException
    {
        Object[] userAndUserId = UserFactory.getInstance().loginUser(request,
                                                                     response,
                                                                     loginName,
                                                                     password);

        return (User) userAndUserId[0];
    }

    public void logout(HttpServletRequest request, HttpServletResponse response)
        throws ObjectNotFoundException, ServletException,
               PermissionDeniedException, AuthenticationFailureException
    {
        UserFactory.getInstance().logoutUser(request, response);
    }

    public User register(HttpServletRequest request, HttpServletResponse response,
                         String loginName, String password)
        throws ServletException, NotUniqueException,
               InvalidLoginNameException, InvalidPasswordException
    {
        return UserFactory.getInstance().registerUser(request, response, loginName, password);
    }

    public User getUserByLoginName(String loginName)
        throws ServletException, FinderException
    {
        return UserFactory.getInstance().getUserByLoginName(loginName);
    }

    public User getLoggedInUser(HttpServletRequest request,
                                HttpServletResponse response)
    {
        Object[] result = null;

        try {
            result = UserFactory.getInstance().getLoggedInUserAndCaller(request,
                                                                        response,
                                                                        null);
        } catch (ServletException e) {
            LOG.log(Level.WARNING,
                    "Trying to get logged in user with faked user id.");
        }

        return (User) (result != null ? result[0] : null);
    }

    public void changePassword(User user,
                               String oldPassword,
                               String newPassword)
        throws ObjectNotFoundException, AuthenticationFailureException,
               PermissionDeniedException, InvalidPasswordException,
               RemoteException
    {
        user.changePassword(oldPassword, newPassword);
    }

    public User getUserIfPresent(HttpServletRequest request,
                                 HttpServletResponse response)
        throws ServletException
    {
        Object[] userAndCaller =
            UserFactory.getInstance().getUserAndCallerIfPresent(request,
                                                                response);
        if (userAndCaller[0] != null) {
            return ((User) userAndCaller[0]);
        }

        return null;
    }


    public boolean isLoggedInAsSiteUser(HttpServletRequest request) {
        return getLoggedInUser(request, null) != null && hasLoginName(request);
    }

    private boolean hasLoginName(HttpServletRequest request) {
        for (Cookie cookie: request.getCookies()) {
            if (cookie.getName().equals(ActionLogin.COOKIE_NAME_LOGIN)) {
                return true;
            }
        }
        return false;
    }

    public String getSessionKeyIfPresent(HttpServletRequest request) throws ServletException {
        Object[] userAndCaller = UserFactory.getInstance().getUserAndCallerIfPresent(request, null);
        return ((Caller) userAndCaller[1]).getSessionKey().replaceAll("=", "");
    }
}
