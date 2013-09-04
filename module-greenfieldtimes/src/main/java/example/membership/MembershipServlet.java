package example.membership;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.polopoly.application.servlet.ApplicationServletUtil;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.CmClientBase;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.siteengine.membership.UserDataManagerImpl;

import example.captcha.CaptchaSettingsPolicy;
import example.captcha.ClusteredImageCaptchaService;

@SuppressWarnings("serial")
public class MembershipServlet extends MembershipServletBase
{
    public void init(ServletConfig config) throws ServletException
    {
        CmClient cmClient = ((CmClient) ApplicationServletUtil
                .getApplication(config.getServletContext())
                .getApplicationComponent(CmClientBase.DEFAULT_COMPOUND_NAME));

        PolicyCMServer cmServer = cmClient.getPolicyCMServer();
        setPolicyCMServer(cmServer);
        setMembershipSettings(new MembershipSettingsImpl(cmServer));
        setUserHandler(new UserHandlerImpl());
        setUserDataHandler(new UserDataHandlerImpl(cmServer));
        setPasswordService(new PasswordService());
        setServiceDataCookieHandler(new ServiceDataCookieHandlerImpl());
        setUserDataManager(new UserDataManagerImpl(cmClient));

        ClusteredImageCaptchaService captchaService = (ClusteredImageCaptchaService)
            config.getServletContext().getAttribute(CaptchaSettingsPolicy.CAPTCHA_SERVICE_SERVLET_CONTEXT_KEY);

        setCaptchaImageService(captchaService);

        super.init(config);
    }
}
