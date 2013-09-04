package example.paywall;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.paywall.ContentBundle;
import com.polopoly.paywall.PaywallPolicy;
import com.polopoly.paywall.Subscription;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

import example.content.article.StandardArticlePolicy;

/**
 * RenderController for the overlay displayed by the paywall when
 * "hitting the wall", either by reaching max amount of clicks of a metered
 * paywall or by reaching a premium article not covered by a subscription of the
 * viewer.
 */
public class PaywallRenderController extends RenderControllerBase {
    private static final Logger LOG = Logger.getLogger(PaywallRenderController.class.getName());

    @Override
    public void populateModelBeforeCacheKey(RenderRequest request, TopModel m, ControllerContext context) {
        CmClient cmClient = getCmClient(context);

        String requestedArticleIdStr = request.getParameter("aId");

        ContentId requestedArticleId = null;
        if (requestedArticleIdStr != null && !"".equals(requestedArticleIdStr)) {
            requestedArticleId = ContentIdFactory.createContentId(requestedArticleIdStr);
        }

        Collection<ContentBundle> validBundles = new ArrayList<ContentBundle>();
        Collection<Subscription> validSubscriptions = new ArrayList<Subscription>();
        String requestedArticleTitle = null;
        boolean paywallIsMetered = false;
        int paywallMeteredPeriod = 0;
        PolicyCMServer cmServer = cmClient.getPolicyCMServer();

        try {
            PaywallPolicy paywall = PaywallPolicy.getPaywallPolicy(cmServer);

            paywallIsMetered = paywall.isMetered();
            paywallMeteredPeriod = paywall.getMeteredPeriodInDays();
            if (requestedArticleId == null) {
                validBundles = paywall.getContentBundles();
                validSubscriptions = paywall.getSubscriptions();
            } else {
                StandardArticlePolicy articlePolicy = (StandardArticlePolicy) cmServer.getPolicy(requestedArticleId);

                requestedArticleTitle = articlePolicy.getName();
                validBundles = articlePolicy.getPremiumBundles();
                validSubscriptions = paywall.getSubscriptionsByContentBundles(validBundles);

            }
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Unable to fetch paywall data", e);
        }
        m.getLocal().setAttribute("requestedArticleTitle", requestedArticleTitle);
        m.getLocal().setAttribute("requestedArticleId", requestedArticleId);
        m.getLocal().setAttribute("validBundles", validBundles);
        m.getLocal().setAttribute("validSubscriptions", validSubscriptions);
        m.getLocal().setAttribute("paywallismetered", paywallIsMetered);
        m.getLocal().setAttribute("meteredPeriod", paywallMeteredPeriod);
        m.getLocal().setAttribute("articleId", requestedArticleIdStr);
    }
}
