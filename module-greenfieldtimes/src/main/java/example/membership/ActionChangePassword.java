package example.membership;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;

import javax.ejb.ObjectNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.polopoly.user.server.AuthenticationFailureException;
import com.polopoly.user.server.InvalidPasswordException;
import com.polopoly.user.server.PermissionDeniedException;
import com.polopoly.user.server.User;
import com.polopoly.util.StringUtil;

import example.util.UrlUtil;

public class ActionChangePassword implements Action
{
    public static final String PARAMETER_CURRENT_PASSWORD = "change_current_password";
    public static final String PARAMETER_NEW_PASSWORD = "change_password";
    
    public static final String PARAMETER_CHANGE_ERROR = "changepassword_formerror";
    public static final String ERROR_STATUS_AUTHENTICATION_FAILED = "auth";
    public static final String ERROR_STATUS_PERMISSION_DENIED = "perm";
    public static final String ERROR_STATUS_SERVICE_DOWN = "down";
    public static final String ERROR_STATUS_USER_NOT_LOGGED_IN = "login";
    public static final String ERROR_STATUS_INVALID_NEW_PASSWORD = "inval";
    
    public static final String PARAMETER_CHANGE_SUCCESS = "changed";
    public static final String SUCCESS_STATUS = "true";
        
    private final ActionUtil _actionUtil = new ActionUtil(PARAMETER_CHANGE_ERROR);
    private final UrlUtil _urlUtil = new UrlUtil();
    private final UserHandler _userHandler;
    
    public ActionChangePassword(UserHandler userHandler)
    {
        _userHandler = userHandler;
    }

    public void perform(HttpServletRequest request,
                        HttpServletResponse response)
        throws IOException,
               ServletException
    {
        String referrer =
            _urlUtil.stripRequestParam(request.getHeader(HTTP_HEADER_NAME_REFERRER),
                                       PARAMETER_CHANGE_ERROR);
        
        response.sendRedirect(tryChangePassword(request, response, referrer));
    }
    
    public String tryChangePassword(HttpServletRequest request,
                                    HttpServletResponse response,
                                    String referrer)
    {
        String oldPassword = request.getParameter(PARAMETER_CURRENT_PASSWORD);
        String newPassword = request.getParameter(PARAMETER_NEW_PASSWORD);
        
        if (StringUtil.isEmpty(oldPassword)) {
            return _actionUtil.appendErrorMsg(referrer, ERROR_STATUS_AUTHENTICATION_FAILED);
        }
        
        User user = _userHandler.getLoggedInUser(request, response);
        if (user == null) {
            return _actionUtil.appendErrorMsg(referrer, ERROR_STATUS_USER_NOT_LOGGED_IN);
        }
        
        return doChangePassword(user, oldPassword, newPassword, response, referrer);
    }

    public String doChangePassword(User user,
                                   String oldPassword,
                                   String newPassword,
                                   HttpServletResponse response,
                                   String referrer)
    {
        try {
            _userHandler.changePassword(user, oldPassword, newPassword);
                        
            return _urlUtil.appendQueryParam(referrer, PARAMETER_CHANGE_SUCCESS, SUCCESS_STATUS);
        } catch (ObjectNotFoundException e) {
            return _actionUtil.logAndFail(referrer,
                                          "Cannot find the user '" + user + "'.",
                                          e, ERROR_STATUS_AUTHENTICATION_FAILED);
        } catch (AuthenticationFailureException e) {
            return _actionUtil.logAndFail(referrer,
                                          "Authentication failed for user '" + user + "'.",
                                          e, ERROR_STATUS_AUTHENTICATION_FAILED);
        } catch (InvalidPasswordException e) {
            return _actionUtil.logAndFail(referrer,
                                          "Login was not allowed for user '" + user + "'.",
                                          e, ERROR_STATUS_INVALID_NEW_PASSWORD);
        } catch (PermissionDeniedException e) {
            return _actionUtil.logAndFail(referrer,
                                          "Login was not allowed for user '" + user + "'.",
                                          e, ERROR_STATUS_SERVICE_DOWN);
        } catch (RemoteException e) {
            return _actionUtil.logAndFail(referrer,
                                          Level.WARNING,
                                          "Could not change password.",
                                          e, ERROR_STATUS_SERVICE_DOWN);
        }
    }
}
