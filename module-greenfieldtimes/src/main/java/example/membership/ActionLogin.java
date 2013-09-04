package example.membership;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.siteengine.membership.UserDataManager;
import com.polopoly.util.StringUtil;

import example.util.UrlUtil;

/**
 * This action is used to log a user on the site. When logging in user data will
 * be loaded from content and set in the "data" cookie. This is done via the
 * UserDataManager. See {@link example.membership.ActionPersistUserData} and
 * my news list Javascript and output template for examples.
 */
public class ActionLogin implements Action
{
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

    /* Component names */
    public static final String CHILD_POLICY_SCREEN_NAME = "screenName";

    private final UrlUtil _urlUtil = new UrlUtil();

    private final MembershipSettings _membershipSettings;

    private final LoginUtil _loginUtil;


    private final ActionUtil _actionUtil = new ActionUtil(PARAMETER_FORM_ERROR);


    public ActionLogin(UserHandler userHandler,
                       UserDataManager userDataManager,
                       ServiceDataCookieHandler serviceDataCookieHandler,
                       MembershipSettings membershipSettings,
                       UserDataHandler userDataHandler,
                       PolicyCMServer cmServer)
    {
        _loginUtil = new LoginUtil(userHandler, userDataManager, serviceDataCookieHandler, userDataHandler, cmServer);
        _membershipSettings = membershipSettings;
    }

    public void perform(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException
    {
        String targetUri = request.getParameter(PARAMETER_TARGET_URI);
        String referrer = request.getHeader(HTTP_HEADER_NAME_REFERRER);
        referrer = _urlUtil.stripRequestParam(referrer, ActionRegister.PARAMETER_FORM_SUCCESS);
        referrer = _urlUtil.stripRequestParam(referrer, ActionResetPassword.PARAMETER_RESET_SUCCESS);

        String loginFailedUrl = _urlUtil.stripRequestParam(referrer, PARAMETER_FORM_ERROR);

        String loginSuccessUrl = (targetUri == null ? referrer : targetUri);

        response.sendRedirect(tryLogin(request,
                                       response,
                                       loginFailedUrl,
                                       _urlUtil.stripRequestParam(loginSuccessUrl,
                                                                  PARAMETER_FORM_ERROR)));
    }

    private String tryLogin(final HttpServletRequest request,
                            final HttpServletResponse response,
                            final String failureUrl,
                            final String successUrl)
        throws ServletException, IOException
    {

        String loginName    = request.getParameter(PARAMETER_LOGIN_NAME);
        String password     = request.getParameter(PARAMETER_PASSWORD);
        String siteIdString = request.getParameter(PARAMETER_SITE);

        if (!isLoginEnabled(siteIdString)) {
            return _actionUtil.appendErrorMsg(failureUrl, ERROR_STATUS_SERVICE_DOWN);
        }

        if (StringUtil.isEmpty(password) || StringUtil.isEmpty(loginName)) {
            return _actionUtil.appendErrorMsg(failureUrl, ERROR_STATUS_AUTHENTICATION_FAILED);
        }

        return _loginUtil.doLogin(request,
                       response,
                       failureUrl,
                       successUrl,
                       loginName,
                       password,
                       siteIdString);
    }

    private boolean isLoginEnabled(String siteIdString)
    {
        try {
            return _membershipSettings.isLoginAllowed(siteIdString);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Could not read membership settings for site '"
                    + siteIdString + ", not possible to login.", e);

            return false;
        }
    }
}
