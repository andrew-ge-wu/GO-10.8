package example.membership;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.paywall.PaywallPolicy;
import com.polopoly.paywall.PremiumUserData;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.membership.UserDataManager.ServiceId;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.user.server.User;
import com.polopoly.user.server.jsp.UserFactory;

import example.blog.AntiSamyHTMLValidator;
import example.blog.BlogContext;
import example.blog.BlogRequestContext;
import example.blog.command.BreakChainUnlessHttpPostCommand;
import example.blog.command.CSRFValidationCommand;
import example.blog.command.CreateBlogCommand;
import example.blog.command.EnsureCacheDisabledCommand;
import example.blog.command.EnsureLoggedInCommand;
import example.blog.command.PopulateBlogListCommand;
import example.content.RenderControllerExtended;
import example.util.Chain;
import example.util.ChainImpl;

public class RenderControllerProfileElement
    extends RenderControllerExtended
{
    private static final Logger LOG = Logger.getLogger(RenderControllerProfileElement.class.getName());
    public static final ServiceId SERVICE_ID_BLOG = new ServiceId("ubg", "0");

    public static final String REQUEST_PARAMETER_BLOG_NAME = "blog_name";
    public static final String REQUEST_PARAMETER_BLOG_DESCRIPTION = "blog_description";
    public static final String REQUEST_PARAMETER_BLOG_ADDRESS = "blog_address";

    public static final String REQUEST_PARAMETER_PARENT_PAGE = "parent_page";

    private final Chain chain = new ChainImpl();

    private PopulateBlogListCommand populateBlogListCommand;

    public RenderControllerProfileElement()
    {
        AntiSamyHTMLValidator htmlValidator = new AntiSamyHTMLValidator();
        chain.addCommand(new EnsureCacheDisabledCommand());
        chain.addCommand(new EnsureLoggedInCommand());
        chain.addCommand(new BreakChainUnlessHttpPostCommand());
        chain.addCommand(new CSRFValidationCommand());
        chain.addCommand(new CreateBlogCommand(htmlValidator));

        populateBlogListCommand = new PopulateBlogListCommand();
    }

    /**
     * Populates model. This logic fetches blogs based on request parameters,
     * needs to be done before cache key.
     */
    @Override
    public void populateModelBeforeCacheKey(RenderRequest request,
                                            TopModel m,
                                            ControllerContext context)
    {
        CmClient cmClient = getCmClient(context);
        BlogContext ctx = new BlogRequestContext(request, m, context, cmClient, new UserHandlerImpl());
        chain.execute(ctx);
        populateBlogListCommand.execute(ctx);
        populateEngagement(cmClient.getPolicyCMServer(), request, m);
    }
    
    public void populateEngagement(PolicyCMServer cmServer, RenderRequest request, TopModel m) {
        boolean isPaywallEnabled = false;
        Collection<?> engagements = Collections.emptyList();
        try{
            PaywallPolicy paywall = PaywallPolicy.getPaywallPolicy(cmServer);
            isPaywallEnabled = paywall.isEnabled();
            if (isPaywallEnabled) {
                Object[] userAndCaller = UserFactory.getInstance().getUserAndCallerIfPresent((HttpServletRequest) request, null);
                if (userAndCaller != null && userAndCaller.length > 1 && userAndCaller[0] != null && userAndCaller[1] != null) {
                    User user = (User) userAndCaller[0];
                    UserDataHandler handler = new UserDataHandlerImpl(cmServer);
                    VersionedContentId userDataContentId = handler.getUserData(user.getUserId()).getContentId();
                    PremiumUserData userData = (PremiumUserData) cmServer.getPolicy(userDataContentId);
                    engagements = userData.getEngagements();
                }
            }
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Unable to fetch paywall data", e);
        } catch (ServletException e) {
            LOG.log(Level.WARNING, "Unable to fetch user data", e);
        } catch (RemoteException e) {
            LOG.log(Level.WARNING, "Unable to fetch user id", e);
        }
        m.getLocal().setAttribute("paywallEnabled", isPaywallEnabled);
        m.getLocal().setAttribute("engagements", engagements);
    }
}
