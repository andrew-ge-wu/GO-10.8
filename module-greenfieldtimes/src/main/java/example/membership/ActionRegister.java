package example.membership;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.management.ServiceNotAvailableRuntimeException;
import com.polopoly.siteengine.membership.UserDataManager;
import com.polopoly.user.server.InvalidLoginNameException;
import com.polopoly.user.server.InvalidPasswordException;
import com.polopoly.user.server.NotUniqueException;
import com.polopoly.user.server.User;
import com.polopoly.util.StringUtil;

import example.captcha.ClusteredImageCaptchaService;
import example.membership.tools.SitePrefixUtil;
import example.util.UrlUtil;
import example.util.mail.MailUtil;

public class ActionRegister implements Action
{
    private static final String CLASS = ActionRegister.class.getName();
    private static final Logger LOG = Logger.getLogger(CLASS);

    /* Cookie names */
    public static final String ERROR_COOKIE_NAME = "formErrors";
    public static final String ECHO_COOKIE_NAME  = "formContent";

    /* Request parameters. */
    public static final String PARAMETER_LOGIN_NAME = "reg_loginname";
    public static final String PARAMETER_PASSWORD = "reg_password";
    public static final String PARAMETER_SCREEN_NAME = "reg_screenname";
    public static final String PARAMETER_SITE = "reg_site";
    public static final String PARAMETER_CAPTCHA = "reg_captcha";

    /* Return parameters. */
    public static final String PARAMETER_FORM_ERROR = "reg_formerror";
    public static final String ERROR_STATUS_REGISTRATION_FAILED = "fail";
    public static final String ERROR_STATUS_SERVICE_DOWN = "down";
    public static final String PARAMETER_FORM_SUCCESS = "reg_success";
    public static final String SUCCESS_STATUS = "true";

    private final UrlUtil _urlUtil = new UrlUtil();
    private final ActionUtil _actionUtil = new ActionUtil(PARAMETER_FORM_ERROR);
    private final SitePrefixUtil _sitePrefixUtil = new SitePrefixUtil();


    private final UserHandler _userHandler;
    private final MembershipSettings _membershipSettings;
    private final UserDataHandler _userDataHandler;
    private final ClusteredImageCaptchaService _captchaService;
    private final LoginUtil _loginUtil;

    public ActionRegister(UserDataManager userDataManager,
                          ServiceDataCookieHandler serviceDataCookieHandler,
                          UserHandler userHandler,
                          MembershipSettings membershipSettings,
                          UserDataHandler userDataHandler,
                          ClusteredImageCaptchaService captchaService,
                          PolicyCMServer cmServer)
    {
        _userHandler = userHandler;
        _membershipSettings = membershipSettings;
        _userDataHandler = userDataHandler;
        _captchaService = captchaService;
        _loginUtil = new LoginUtil(userHandler, userDataManager, serviceDataCookieHandler, userDataHandler, cmServer);
    }

    public void perform(HttpServletRequest request,
                        HttpServletResponse response)
        throws IOException, ServletException
    {
        String referrer = request.getHeader(HTTP_HEADER_NAME_REFERRER);
        String loginName = request.getParameter(PARAMETER_LOGIN_NAME);
        String password = request.getParameter(PARAMETER_PASSWORD);
        String siteIdString = request.getParameter(PARAMETER_SITE);

        String redirectUrl =
            doRegister(request, response, referrer, loginName, password, siteIdString);

        boolean isLoginAllowed = false;
        try {
            isLoginAllowed = _membershipSettings.isLoginAllowed(siteIdString);
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Unable to determine if site available for login");
        }
        if (isLoginAllowed && redirectUrl.contains(PARAMETER_FORM_SUCCESS)) {
            String loginFailedUrl = _urlUtil.stripRequestParam(referrer, PARAMETER_FORM_ERROR);
            redirectUrl = _loginUtil.doLogin(request, response, loginFailedUrl, redirectUrl, loginName, password, siteIdString);
        }
        response.sendRedirect(redirectUrl);
    }

    private String doRegister(HttpServletRequest request,
                              HttpServletResponse response,
                              String referer,
                              String loginName,
                              String password,
                              String siteIdString)
    {
        if (!isRegisterEnabled(siteIdString)) {
            return _actionUtil.appendErrorMsg(referer, ERROR_STATUS_SERVICE_DOWN);
         }

        String realLoginName = _sitePrefixUtil.addPrefix(siteIdString, loginName);

        Map<String, String> echoVariables = new HashMap<String, String>();
        Map<String, String> formErrors = new HashMap<String, String>();

        try {
            RegistrationFormHandler formHandler =
                _userDataHandler.getFormHandler(siteIdString);

            formErrors.putAll(validateCoreData(request, response));
            formErrors.putAll(formHandler.validateFormData(request));

            echoVariables.put(PARAMETER_LOGIN_NAME, loginName);
            echoVariables.putAll(formHandler.getEchoRequestData(request));

            response.addCookie(_actionUtil.createMessageCookie(ECHO_COOKIE_NAME,
                                                               echoVariables));

            if (formErrors.size() != 0) {
                response.addCookie(_actionUtil.createMessageCookie(ERROR_COOKIE_NAME,
                                                                   formErrors));
                return referer;
            }

            User user =
                _userHandler.register(request, response, realLoginName, password);

            ContentPolicy userData =
                _userDataHandler.createUserData(user.getUserId());

            formHandler.writeFormData(userData, request);

            _userDataHandler.commitUserData(userData);

            response.addCookie(deleteCookie(ERROR_COOKIE_NAME));
            response.addCookie(deleteCookie(ECHO_COOKIE_NAME));

            return _urlUtil.appendQueryParam(referer, PARAMETER_FORM_SUCCESS, SUCCESS_STATUS);
        } catch (NotUniqueException e) {
            response.addCookie(_actionUtil.createMessageCookie(ERROR_COOKIE_NAME,
                                                               PARAMETER_LOGIN_NAME,
                                                               RegistrationFormHandler.ERROR_KEY_NOT_UNIQUE));

            response.addCookie(_actionUtil.createMessageCookie(ECHO_COOKIE_NAME,
                                                               echoVariables));
            return referer;
        } catch (InvalidLoginNameException e) {
            response.addCookie(_actionUtil.createMessageCookie(ERROR_COOKIE_NAME,
                                                               PARAMETER_LOGIN_NAME,
                                                               RegistrationFormHandler.ERROR_INVALID_EMAIL));

            response.addCookie(_actionUtil.createMessageCookie(ECHO_COOKIE_NAME,
                                                               echoVariables));
            return referer;
        } catch (InvalidPasswordException e) {
            response.addCookie(_actionUtil.createMessageCookie(ERROR_COOKIE_NAME,
                                                               PARAMETER_PASSWORD,
                                                               RegistrationFormHandler.ERROR_INVALID_PASSWORD));

            response.addCookie(_actionUtil.createMessageCookie(ECHO_COOKIE_NAME,
                                                               echoVariables));
            return referer;
        } catch (ServiceNotAvailableRuntimeException e) {
            response.addCookie(deleteCookie(ECHO_COOKIE_NAME));
            return _actionUtil.logAndFail(referer,
                                          Level.WARNING,
                                          "Registration not possible, service not available.",
                                          e, ERROR_STATUS_SERVICE_DOWN);
        } catch (ServletException e) {
            response.addCookie(deleteCookie(ECHO_COOKIE_NAME));
            return _actionUtil.logAndFail(referer,
                                          Level.WARNING,
                                          "Login not possible, servlet error.",
                                          e, ERROR_STATUS_SERVICE_DOWN);
        } catch (CMException e) {
            response.addCookie(deleteCookie(ECHO_COOKIE_NAME));
            return _actionUtil.logAndFail(referer,
                                          Level.WARNING,
                                          "Could not create or commit user data.",
                                          e, ERROR_STATUS_SERVICE_DOWN);
        } catch (RemoteException e) {
            response.addCookie(deleteCookie(ECHO_COOKIE_NAME));
            return _actionUtil.logAndFail(referer,
                                          Level.WARNING,
                                          "Could not create or commit user data.",
                                          e, ERROR_STATUS_SERVICE_DOWN);
        }
    }

    private Cookie deleteCookie(String cookieName)
    {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);

        return cookie;
    }

    private boolean isRegisterEnabled(String siteIdString) {
        try {
            return _membershipSettings.isRegistrationAllowed(siteIdString);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Could not read membership settings for site '"
                    + siteIdString + ", not possible to register.", e);
            return false;
        }
    }

    private Map<String, String> validateCoreData(HttpServletRequest request,
                                                 HttpServletResponse response)
    {
        Map<String, String> errors = new HashMap<String, String>();

        String loginName =
            request.getParameter(ActionRegister.PARAMETER_LOGIN_NAME);

        if (StringUtil.isEmpty(loginName)) {
            errors.put(ActionRegister.PARAMETER_LOGIN_NAME,
                       RegistrationFormHandler.ERROR_EMPTY_FIELD);
        } else if (!MailUtil.isValidEmailAddress(loginName)) {
            errors.put(ActionRegister.PARAMETER_LOGIN_NAME,
                       RegistrationFormHandler.ERROR_INVALID_EMAIL);
        }

        String password =
            request.getParameter(ActionRegister.PARAMETER_PASSWORD);

        if (StringUtil.isEmpty(password)) {
            errors.put(ActionRegister.PARAMETER_PASSWORD,
                       RegistrationFormHandler.ERROR_EMPTY_FIELD);
        } else if (password.trim().length() < 4) {
            errors.put(ActionRegister.PARAMETER_PASSWORD,
                       RegistrationFormHandler.ERROR_PASSWORD_TOO_SHORT);
        }

        String captchaAnswer =
            request.getParameter(ActionRegister.PARAMETER_CAPTCHA);

        if (!validateCaptchaAnswer(captchaAnswer, request, response)) {
            errors.put(ActionRegister.PARAMETER_CAPTCHA,
                    RegistrationFormHandler.ERROR_BAD_CAPTCHA);
        }

        return errors;
    }

    boolean validateCaptchaAnswer(String answer, HttpServletRequest request,
                                  HttpServletResponse response)
    {
        return _captchaService.validateAnswer(answer, request, response);
    }

}
