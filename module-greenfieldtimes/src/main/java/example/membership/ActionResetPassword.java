package example.membership;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;

import javax.ejb.FinderException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.polopoly.cm.client.CMException;
import com.polopoly.management.ServiceNotAvailableRuntimeException;
import com.polopoly.user.server.Caller;
import com.polopoly.user.server.InvalidPasswordException;
import com.polopoly.user.server.PermissionDeniedException;
import com.polopoly.user.server.User;
import com.polopoly.util.StringUtil;

import example.captcha.ClusteredImageCaptchaService;
import example.membership.tools.SitePrefixUtil;
import example.util.UrlUtil;
import example.util.mail.EmailException;

public class ActionResetPassword implements Action
{
    /* Request parameters. */
    public static final String PARAMETER_LOGIN_NAME = "reset_loginname";
    public static final String PARAMETER_SITE = "reset_site";
    public static final String PARAMETER_CAPTCHA = "reset_captcha";

    /* Return parameters. */
    public static final String PARAMETER_RESET_ERROR = "resetpassword_formerror";
    public static final String ERROR_STATUS_SERVICE_DOWN = "down";
    public static final String ERROR_STATUS_UNKNOWN_USER = "user";
    public static final String ERROR_INVALID_CAPTCHA = "captcha";
    public static final String PARAMETER_RESET_SUCCESS = "reset";
    public static final String SUCCESS_STATUS = "true";

    private final ActionUtil _actionUtil = new ActionUtil(PARAMETER_RESET_ERROR);

    private final UrlUtil _urlUtil = new UrlUtil();
    
    private final SitePrefixUtil _sitePrefixUtil = new SitePrefixUtil();

    private final UserHandler _userHandler;

    private final ClusteredImageCaptchaService _captchaService;

    private final MembershipSettings _membershipSettings;

    private final PasswordService _passwordService;

    public ActionResetPassword(UserHandler userHandler,
                               MembershipSettings membershipSettings,
                               PasswordService passwordService,
                               ClusteredImageCaptchaService captchaService)
    {
        _userHandler = userHandler;
        _membershipSettings = membershipSettings;
        _passwordService = passwordService;
        _captchaService = captchaService;
    }

    public void perform(HttpServletRequest request,
                        HttpServletResponse response)
        throws IOException, ServletException
    {
        String referrer =
            _urlUtil.stripRequestParam(request.getHeader(HTTP_HEADER_NAME_REFERRER),
                                       PARAMETER_RESET_ERROR);

        response.sendRedirect(tryResetPassword(request, response, referrer));
    }

    public String tryResetPassword(HttpServletRequest request,
                                   HttpServletResponse response,
                                   String referrer)
    {
        String loginName = request.getParameter(PARAMETER_LOGIN_NAME);

        if (StringUtil.isEmpty(loginName)) {
            return _actionUtil.appendErrorMsg(referrer,
                    ERROR_STATUS_UNKNOWN_USER);
        }

        String siteIdString = request.getParameter(PARAMETER_SITE);
        
        String realLoginName = _sitePrefixUtil.addPrefix(siteIdString, loginName);

        String captchaAnswer = request.getParameter(PARAMETER_CAPTCHA);

        if (!_captchaService.validateAnswer(captchaAnswer, request, response)) {
            return _actionUtil.appendErrorMsg(referrer, ERROR_INVALID_CAPTCHA);
        }

        return doResetPassword(loginName, realLoginName, siteIdString, referrer);
    }

    public String doResetPassword(String loginName,
                                  String realLoginName,
                                  String siteId,
                                  String referrer)
    {
        try {
            User user = _userHandler.getUserByLoginName(realLoginName);

            String newPassword = _passwordService.generatePassword();
            
            user.setPassword(newPassword, Caller.NOBODY_CALLER);
            
            ResetPasswordMailService mailService =
                _membershipSettings.getResetPasswordMailService(siteId);
            
            mailService.send(loginName, newPassword);

            return _urlUtil.appendQueryParam(referrer,
                                             PARAMETER_RESET_SUCCESS,
                                             SUCCESS_STATUS);
        } catch (FinderException e) {
            return _actionUtil.logAndFail(referrer, "Unknown login name '"
                    + realLoginName + "'.",
                    e,
                    ERROR_STATUS_UNKNOWN_USER);
        } catch (EmailException e) {
            return _actionUtil.logAndFail(referrer,
                    "Unable to send email to user '" + realLoginName + "'.",
                    e,
                    ERROR_STATUS_SERVICE_DOWN);
        } catch (ServletException e) {
            return _actionUtil.logAndFail(referrer,
                    "Login was not allowed for user '" + realLoginName + "'.",
                    e,
                    ERROR_STATUS_SERVICE_DOWN);
        } catch (RemoteException e) {
            return _actionUtil.logAndFail(referrer, Level.WARNING,
                    "Could not reset password.",
                    e,
                    ERROR_STATUS_SERVICE_DOWN);
        } catch (PermissionDeniedException e) {
            return _actionUtil.logAndFail(referrer,
                    "Login was not allowed for user '" + realLoginName + "'.",
                    e,
                    ERROR_STATUS_SERVICE_DOWN);
        } catch (ServiceNotAvailableRuntimeException e) {
            return _actionUtil.logAndFail(referrer, Level.WARNING,
                    "Registration not possible, service not available.",
                    e,
                    ERROR_STATUS_SERVICE_DOWN);
        } catch (InvalidPasswordException e) {
            return _actionUtil.logAndFail(referrer, Level.WARNING,
                    "Failed to generate an acceptable password.",
                    e,
                    ERROR_STATUS_SERVICE_DOWN);
        } catch (CMException e) {
            return _actionUtil.logAndFail(referrer, Level.WARNING,
                   "Internal error, cannot reset password.",
                   e,
                   ERROR_STATUS_SERVICE_DOWN);
        }
    }
    
}
