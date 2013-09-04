package example.membership;

import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ObjectNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;
import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentOperationFailedException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.paywall.ContentBundle;
import com.polopoly.paywall.PaywallPolicy;
import com.polopoly.paywall.PremiumUserData;
import com.polopoly.paywall.cookie.OnlineAccessCookie;
import com.polopoly.paywall.cookie.OnlineAccessDigestCookie;
import com.polopoly.siteengine.membership.UserDataManager;
import com.polopoly.user.server.AuthenticationFailureException;
import com.polopoly.user.server.PermissionDeniedException;
import com.polopoly.user.server.User;

import example.membership.tools.Base64Util;
import example.membership.tools.LoadPersistentServiceDataTool;
import example.membership.tools.SitePrefixUtil;

public class LoginUtil {

    private static final String CLASS = ActionLogin.class.getName();
    private static final Logger LOG = Logger.getLogger(CLASS);

    /* Request parameters. */
    public static final String PARAMETER_LOGIN_NAME = "login_loginname";
    public static final String PARAMETER_PASSWORD = "login_password";
    public static final String PARAMETER_SITE = "login_site";
    public static final String PARAMETER_TARGET_URI = "login_targeturi";

    /* Return parameters. */
    public static final String PARAMETER_FORM_ERROR = "login_formerror";
    public static final String ERROR_STATUS_AUTHENTICATION_FAILED = "auth";
    public static final String ERROR_STATUS_PERMISSION_DENIED = "perm";
    public static final String ERROR_STATUS_SERVICE_DOWN = "down";
    public static final String ERROR_STATUS_USER_DATA_NOT_FOUND = "user";

    /* Cookie constants. */
    public static final String COOKIE_NAME_LOGIN = "loginName";
    public static final String COOKIE_NAME_SCREEN = "screenName";
    public static final String COOKIE_NAME_REMEMBER_ME = "rememberMe";
    private static final String DEFAULT_COOKIE_PATH = "/";
    private static final int SESSION_COOKIE_EXPIRY = -1;
    private static final int DELETE_COOKIE_EXPIRY = 0;

    /* Component names */
    public static final String CHILD_POLICY_SCREEN_NAME = "screenName";

    private final UserHandler _userHandler;
    private final UserDataManager _userDataManager;
    private final ServiceDataCookieHandler _serviceDataCookieHandler;
    private final LoadPersistentServiceDataTool _serviceDataLoader;

    private final ActionUtil _actionUtil = new ActionUtil(PARAMETER_FORM_ERROR);

    private final Base64Util _base64Util = new Base64Util();

    private final SitePrefixUtil _sitePrefixUtil = new SitePrefixUtil();
    private final UserDataHandler _userDataHandler;
    private final PolicyCMServer _cmServer;

    public LoginUtil(UserHandler userHandler, UserDataManager userDataManager, ServiceDataCookieHandler serviceDataCookieHandler,
            UserDataHandler userDataHandler, PolicyCMServer cmServer) {
        _userHandler = userHandler;
        _userDataManager = userDataManager;
        _serviceDataCookieHandler = serviceDataCookieHandler;
        _userDataHandler = userDataHandler;
        _cmServer = cmServer;

        _serviceDataLoader = new LoadPersistentServiceDataTool(_serviceDataCookieHandler, _userDataManager);
    }

    public String doLogin(final HttpServletRequest request, final HttpServletResponse response, final String failureUrl, final String successUrl, final String loginName,
            final String password, final String siteIdString) {
        String realLoginName = _sitePrefixUtil.addPrefix(siteIdString, loginName);
        String screenName = "";

        try {
            User user = _userHandler.login(request, response, realLoginName, password);

            ContentPolicy userData = _userDataHandler.getUserData(user.getUserId());
            screenName = ((SingleValued) userData.getChildPolicy(CHILD_POLICY_SCREEN_NAME)).getValue();

            JsonObject serviceData = _serviceDataLoader.loadPersistentServiceData(user);

            Cookie serviceDataCookie = _actionUtil.createMessageCookieFromJsonObject(ActionPersistUserData.COOKIE_NAME_DATA, serviceData);

            setServiceDataCookie(response, serviceData, serviceDataCookie);

            setCookie(response, COOKIE_NAME_LOGIN, _base64Util.encode(loginName), SESSION_COOKIE_EXPIRY);
            setCookie(response, COOKIE_NAME_SCREEN, _base64Util.encode(screenName), SESSION_COOKIE_EXPIRY);

            PaywallPolicy paywallPolicy = PaywallPolicy.getPaywallPolicy(_cmServer);

            Collection<ContentBundle> contentBundles = ((PremiumUserData) userData).getAccessibleContentBundlesByCapability(paywallPolicy.getOnlineAccessCapability(_cmServer));

            OnlineAccessCookie onlineAccessCookie = new OnlineAccessCookie(contentBundles);
            response.addCookie(onlineAccessCookie);

            String sessionKey = _userHandler.getSessionKeyIfPresent(request);
            OnlineAccessDigestCookie onlineAccessDigestCookie = new OnlineAccessDigestCookie(onlineAccessCookie, sessionKey, paywallPolicy.getSecret());
            response.addCookie(onlineAccessDigestCookie);

            return successUrl;
        } catch (ObjectNotFoundException e) {
            return _actionUtil.logAndFail(failureUrl, "Cannot find the user '" + realLoginName + "'.", e, ERROR_STATUS_AUTHENTICATION_FAILED);
        } catch (AuthenticationFailureException e) {
            return _actionUtil.logAndFail(failureUrl, "Authentication failed for user '" + realLoginName + "'.", e, ERROR_STATUS_AUTHENTICATION_FAILED);
        } catch (PermissionDeniedException e) {
            return _actionUtil.logAndFail(failureUrl, "Login was not allowed for user '" + realLoginName + "'.", e, ERROR_STATUS_SERVICE_DOWN);
        } catch (ServletException e) {
            return _actionUtil.logAndFail(failureUrl, "Login not possible, servlet error. Might also suggest disconnected mode.", e, ERROR_STATUS_SERVICE_DOWN);
        } catch (UnsupportedEncodingException e) {
            return _actionUtil.logAndFail(failureUrl, "Could not set loginName cookie due to bad encoding", e, ERROR_STATUS_SERVICE_DOWN);
        } catch (ContentOperationFailedException e) {
            try {
                _userHandler.logout(request, response);
            } catch (Exception exception) {
                LOG.log(Level.WARNING, "Unable to log out user '" + realLoginName + "'.", exception);
            }
            return _actionUtil.logAndFail(failureUrl, Level.WARNING, "UserData content missing for user '" + realLoginName + "'.", e, ERROR_STATUS_USER_DATA_NOT_FOUND);
        } catch (CMException e) {
            return _actionUtil.logAndFail(failureUrl, "Could not get screen name from content for user '" + realLoginName + "'.", e, ERROR_STATUS_SERVICE_DOWN);
        } catch (RemoteException e) {
            return _actionUtil.logAndFail(failureUrl, "Could not get screen name from content for user '" + realLoginName + "'.", e, ERROR_STATUS_SERVICE_DOWN);
        }
    }

    private void setServiceDataCookie(final HttpServletResponse response, final JsonObject serviceData, final Cookie serviceDataCookie) {
        if (serviceData.entrySet().isEmpty()) {
            serviceDataCookie.setMaxAge(DELETE_COOKIE_EXPIRY);
        } else {
            serviceDataCookie.setMaxAge(SESSION_COOKIE_EXPIRY);
        }
        response.addCookie(serviceDataCookie);
    }

    private void setCookie(final HttpServletResponse response, final String cookieName, final String cookieValue, final int maxAge) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setPath(DEFAULT_COOKIE_PATH);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

}
