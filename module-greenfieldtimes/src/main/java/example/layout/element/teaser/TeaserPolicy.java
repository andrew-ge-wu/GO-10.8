package example.layout.element.teaser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.imagemanager.GeneratedImage;
import com.polopoly.cm.app.imagemanager.Image;
import com.polopoly.cm.app.imagemanager.ImageEditorPolicy;
import com.polopoly.cm.app.imagemanager.ImageGenerationFailedException;
import com.polopoly.cm.app.imagemanager.ImageGenerator;
import com.polopoly.cm.app.imagemanager.ImageSet;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentOperationFailedException;
import com.polopoly.cm.client.LinkContent;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.siteengine.layout.ContentRepresentative;
import com.polopoly.siteengine.standard.image.ImageResource;

import example.content.ImageProviderPolicy;
import example.layout.element.ElementPolicy;

public class TeaserPolicy extends ElementPolicy
    implements ImageGenerator,
               ImageResource,
               LinkContent,
               ContentRepresentative
{
    private static final Logger LOG = Logger.getLogger(TeaserPolicy.class.getName());
    private static final String ARTICLES_LIST = "articles";
    public static final ExternalContentId TEASER_INPUT_TEMPLATE_ID =
        new ExternalContentId("example.TeaserElement");

    public String getName() throws CMException {
        String name = super.getName();
        if (name == null) {
            ContentPolicy article = getArticle();
            if (article != null) {
                return article.getName();
            }
        }

        return name;
    }

    /**
     * Get referred article from this teaser.
     *
     * @return null if no article has yet been referred or the article object is
     *         not available for some reason
     */
    public ContentPolicy getArticle()
        throws CMException
    {
        ContentId articleId = getArticleId();
        if (articleId != null) {
            try {
                return (ContentPolicy) getCMServer().getPolicy(articleId);
            } catch (ContentOperationFailedException articleNotFound) {
                LOG.log(Level.FINE, "Unable to get article " + articleId
                        + " for teaser " + getContentId(), articleNotFound);
            }
        }
        return null;
    }

    public ContentId getArticleId()
        throws CMException
    {
        ContentList list = getContentList(ARTICLES_LIST);
        return (list != null && list.size() > 0) ? list.getEntry(0).getReferredContentId() : null;
    }

    public void setArticleId(ContentId articleId)
        throws CMException
    {
        ContentId unversionedId = articleId.getContentId();
        ContentId currentArticleId = getArticleId();
        if (currentArticleId != null) {
            if (currentArticleId.equals(unversionedId)) {
                // Already done
                return;
            }
            getContentList(ARTICLES_LIST).remove(0);
        }
        getContentList(ARTICLES_LIST).add(0, new ContentReference(unversionedId, null));
    }

    public ImageProviderPolicy getReferredImage()
        throws CMException
    {
        try {
            ContentId imageId = getImageEditor().getReferredImageId();
            if (imageId != null) {
                return (ImageProviderPolicy) getCMServer().getPolicy(imageId);
            }

        } catch (ContentOperationFailedException imageNotFound) {
            LOG.log(Level.FINE, "Unable to get referred image for teaser "
                                 + getContentId(), imageNotFound);
        }
        return null;
    }

    public void setReferredImage(ContentId referredImage) throws CMException {
        getImageEditor().setReferredImageId(referredImage);
    }

    public GeneratedImage generateImage(String path, long modifiedSince)
         throws IOException, ImageGenerationFailedException
    {
        try {
            return getImageEditor().generateImage(path, modifiedSince);
        } catch (CMException e) {
            throw new ImageGenerationFailedException(
                    "Image generation failed: " + e.getMessage());
        }
    }

    public String getImageDescription()
    {
        try {
            return getImageEditor().getImageDescription();
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Could not get image editor", e);
        }

        return null;
    }

    public String getImageName()
    {
        try {
            return getImageEditor().getImageName();
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Could not get image editor", e);
        }

        return null;
    }

    public ImageSet getImageSet()
    {
        try {
            return getImageEditor().getImageSet();
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Could not get image editor", e);
        }

        return null;
    }

    private ImageEditorPolicy getImageEditor()
        throws CMException
    {
        return (ImageEditorPolicy) getChildPolicy("image");
    }

    public ContentId getLinkedContentId()
    {
        try {
            return getArticleId();
        } catch (CMException e) {
            // Won't happen?
            return null;
        }
    }
    
    public List<ContentId> getRepresentedContent() {
        List<ContentId> containedIds = new ArrayList<ContentId>();

        ContentId articleId = getLinkedContentId();
        if (articleId != null) {
            containedIds.add(articleId);
        }

        try {
            ImageSet imageSet = getImageSet();
            if (imageSet != null) {
                Image image = imageSet.getImage();
                if (image != null) {
                    ContentId imageContentId = image.getImageContentId();
                    if (imageContentId != null) {
                        containedIds.add(imageContentId);
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.FINE, "Failed to add image to contained IDs.", e);
        }

        return containedIds;
    }
}
