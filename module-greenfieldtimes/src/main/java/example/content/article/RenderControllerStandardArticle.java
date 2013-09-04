package example.content.article;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.model.ModelWrite;
import com.polopoly.paywall.ContentBundle;
import com.polopoly.paywall.PaywallPolicy;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

import example.content.BodyTranslator;
import example.content.RenderControllerExtended;

/**
 * Render controller for Standard Article.
 */
public class RenderControllerStandardArticle extends RenderControllerExtended {

    private static final Logger LOG = Logger.getLogger(RenderControllerStandardArticle.class.getName());

    /**
     * Populates model based on preview, needs to be done before cache key.
     */
    @Override
    public void populateModelBeforeCacheKey(RenderRequest request, TopModel m,
            ControllerContext context) {

        super.populateModelBeforeCacheKey(request, m, context);

        ModelWrite localModel = m.getLocal();

        boolean inPreviewMode = m.getRequest().getPreview().isInPreviewMode();

        BodyTranslator bodyTranslator = new BodyTranslator();
        bodyTranslator.translateBody(request, localModel, "body/value", inPreviewMode);
        String readableContentBundles = "";
        StandardArticlePolicy article = (StandardArticlePolicy) ModelPathUtil.get(context.getContentModel(), "_data");
        try {
            readableContentBundles = article.getReadablePremiumBundles();
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Unable to read premium bundles from article", e);
        }
        localModel.setAttribute("premiumBundles", readableContentBundles);
        localModel.setAttribute("premiumBundleIds", getPremiumContentBundleIds(article));
        localModel.setAttribute("meteredPaywallEnabled", isMeteredPaywallEnabled(article.getCMServer()));
    }

    public String getPremiumContentBundleIds(StandardArticlePolicy article) {
        List<String> contentIds = new ArrayList<String>();
        try {
            for (ContentBundle bundle : article.getPremiumBundles()) {
                contentIds.add(bundle.getContentId().getContentId().getContentIdString());
            }
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Unable to get premium bundles on article with id: " + article.getContentId().getContentIdString());
        }
        return StringUtils.join(contentIds, ":");
    }

    public boolean isMeteredPaywallEnabled(PolicyCMServer cmServer) {
        try {
            PaywallPolicy paywall = PaywallPolicy.getPaywallPolicy(cmServer);
            return (paywall.isEnabled() && paywall.isMetered());
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Unable to fetch paywall settings", e);
        }
        return false;
    }
}
