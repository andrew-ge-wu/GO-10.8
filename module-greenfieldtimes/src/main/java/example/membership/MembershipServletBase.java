package example.membership;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.siteengine.membership.UserDataManager;

import example.captcha.ClusteredImageCaptchaService;

/**
 * Servlet for managing membership actions on the site.
 */
@SuppressWarnings("serial")
public class MembershipServletBase extends HttpServlet
{
    private MembershipSettings membershipSettings;
    private UserHandler userHandler;
    private UserDataHandler userDataHandler;
    private ServiceDataCookieHandler serviceDataCookieHandler;
    private UserDataManager userDataManager;
    private ClusteredImageCaptchaService captchaImageService;
    private PasswordService passwordService;

    private PolicyCMServer cmServer;

    private final Map<String, Action> dispatcherMap = new HashMap<String, Action>();

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        dispatcherMap
            .put("/logout",
                 new ActionUncacheableResponse(
                     new CSRFValidationAction(cmServer,
                         new ActionLogout(userHandler))));

        dispatcherMap
            .put("/login",
                 new ActionUncacheableResponse
                 (new ActionFailOnNoReferer
                  (new ActionLogin(userHandler,
                                   userDataManager,
                                   serviceDataCookieHandler,
                                   membershipSettings,
                                   userDataHandler,
                                   cmServer))));

        dispatcherMap
            .put("/register",
                 new ActionUncacheableResponse
                 (new ActionFailOnNoReferer
                  (new ActionRegister(userDataManager, serviceDataCookieHandler, userHandler, membershipSettings,
                                      userDataHandler,
                                      captchaImageService, cmServer))));

        dispatcherMap.put("/changepassword",
                          new ActionUncacheableResponse(
                              new CSRFValidationAction(cmServer,
                                  new ActionFailOnNoReferer(
                                      new ActionChangePassword(userHandler)))));

        dispatcherMap.put("/resetpassword",
                new ActionUncacheableResponse(
                    new ActionFailOnNoReferer(
                        new ActionResetPassword(userHandler,
                                                membershipSettings,
                                                passwordService,
                                                captchaImageService))));

        dispatcherMap.put("/persist",
                          new ActionUncacheableResponse(
                              new CSRFValidationAction(cmServer,
                                  new ActionPersistUserData(userHandler,
                                                            serviceDataCookieHandler,
                                                            userDataManager))));
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        String path = request.getPathInfo();
        Action action = dispatcherMap.get(path); // No need to synchronize, just concurrent gets.

        if (null != action) {
            action.perform(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    public void setMembershipSettings(MembershipSettings membershipSettings) {
        this.membershipSettings = membershipSettings;
    }

    public void setUserHandler(UserHandler userHandler) {
        this.userHandler = userHandler;
    }

    public void setServiceDataCookieHandler(ServiceDataCookieHandler serviceDataCookieHandler) {
        this.serviceDataCookieHandler = serviceDataCookieHandler;
    }

    public void setUserDataManager(UserDataManager userDataManager) {
        this.userDataManager = userDataManager;
    }

    public void setPasswordService(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    public void setCaptchaImageService(ClusteredImageCaptchaService captchaImageService) {
        this.captchaImageService = captchaImageService;
    }

    public void setUserDataHandler(UserDataHandler userDataHandler) {
        this.userDataHandler = userDataHandler;
    }

    public void setPolicyCMServer(PolicyCMServer cmServer) {
        this.cmServer = cmServer;
    }
}