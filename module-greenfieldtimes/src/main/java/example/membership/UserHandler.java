package example.membership;

import java.rmi.RemoteException;

import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.polopoly.user.server.AuthenticationFailureException;
import com.polopoly.user.server.InvalidLoginNameException;
import com.polopoly.user.server.InvalidPasswordException;
import com.polopoly.user.server.NotUniqueException;
import com.polopoly.user.server.PermissionDeniedException;
import com.polopoly.user.server.User;

public interface UserHandler {

    void logout(HttpServletRequest request, HttpServletResponse response)
                throws ObjectNotFoundException, ServletException,
                       PermissionDeniedException, AuthenticationFailureException;

    User login(HttpServletRequest request, HttpServletResponse response,
               String loginName, String password)
               throws ObjectNotFoundException, ServletException,
                      AuthenticationFailureException, PermissionDeniedException;

    User register(HttpServletRequest request, HttpServletResponse response,
                  String loginName, String password)
        throws ServletException, NotUniqueException,
               InvalidLoginNameException, InvalidPasswordException;

    /**
     * @return the user for the given login name
     */
    User getUserByLoginName(String loginName)
        throws ServletException, FinderException;

    /**
     * @return the logged in user or null if the user is not logged in.
     */
    User getLoggedInUser(HttpServletRequest request,
                         HttpServletResponse response);

    void changePassword(User user, String oldPassword, String newPassword)
        throws ObjectNotFoundException, AuthenticationFailureException,
               PermissionDeniedException, InvalidPasswordException,
               RemoteException;

    /**
     * @return the user found in the request or null if none present.
     */
    User getUserIfPresent(HttpServletRequest request,
                          HttpServletResponse response)
        throws ServletException;

    /**
     * @return true if the user is logged in, and has a loginName cookie, which
     * is only true if you're a site user.
     */
    boolean isLoggedInAsSiteUser(HttpServletRequest request);

    /**
     * @param request
     * @return Session key if present
     * @throws ServletException
     */
    String getSessionKeyIfPresent(HttpServletRequest request) throws ServletException;
}
