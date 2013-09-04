package example.collection;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

import example.content.article.StandardArticlePolicy;
import example.layout.element.teaser.TeaserPolicy;

/**
 * Use this content list wrapper in teasers to default teaser image to
 * referred article image.
 * 
 */
public class TeaserImageInitContentListWrapper extends ContentListWrapperLimited
{

    private static final Logger LOG = Logger.getLogger(TeaserImageInitContentListWrapper.class.getName());

    public void postAddCallback(int index, ContentReference contentRef) {
        try {
            TeaserPolicy teaserPolicy = (TeaserPolicy)getWrapperDefinition().getPolicy();
            if (teaserPolicy.getReferredImage() == null) {

                PolicyCMServer cmServer = getWrapperDefinition().getPolicyCMServer();
                Policy articlePolicy = cmServer.getPolicy(contentRef.getReferredContentId());

                if (articlePolicy instanceof StandardArticlePolicy) {
                    StandardArticlePolicy article = (StandardArticlePolicy)articlePolicy;
                    ContentId imageReference = article.getDefaultReferredImage();

                    if (imageReference != null) {
                        teaserPolicy.setReferredImage(imageReference);
                    }

                }
            }

        } catch (CMException e) {
            LOG.log(Level.INFO, "Failed to set image from article", e);
        }
    }
}