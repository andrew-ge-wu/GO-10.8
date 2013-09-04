package example.content;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.plugins.baseline.url.UrlResolver;
import com.polopoly.cm.app.imagemanager.GeneratedImage;
import com.polopoly.cm.app.imagemanager.Image;
import com.polopoly.cm.app.imagemanager.ImageExifMetadataProvider;
import com.polopoly.cm.app.imagemanager.ImageGenerationFailedException;
import com.polopoly.cm.app.imagemanager.ImageGenerator;
import com.polopoly.cm.app.imagemanager.ImageIptcMetadataProvider;
import com.polopoly.cm.app.imagemanager.ImageManagerPolicy;
import com.polopoly.cm.app.imagemanager.ImageProvider;
import com.polopoly.cm.app.imagemanager.ImageSet;
import com.polopoly.cm.app.imagemanager.ImageSetPolicy;
import com.polopoly.cm.app.policy.SelectableSubFieldPolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.model.ModelTypeDescription;
import com.polopoly.siteengine.standard.image.ImageResource;
import com.polopoly.util.StringUtil;


import example.content.image.ImageSetAsMap;

/**
 * Policy for content having an image.
 */
public class ImageProviderPolicy extends ResourceBasePolicy
    implements ImageResource,
               ImageGenerator,
               ImageExifMetadataProvider,
               ImageIptcMetadataProvider,
               ModelTypeDescription
{
    private static final Logger LOG = Logger.getLogger(ImageProviderPolicy.class.getName());

    public String getThumbnailPath(UrlResolver urlResolver)
    {
        try {
            Image image = getSmallThumbnail();

            if (image != null) {
                return image.isAbsolute() ? image.getPath()
                    : urlResolver.getFileUrl(getContentId(), image.getPath());
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Unable to get thumbnail", e);
        }

        return null;
    }

    public String getPreviewPath(UrlResolver urlResolver)
    {
        try {
            Image image = getOriginalImage();

            if (image != null) {
                return image.isAbsolute() ? image.getPath()
                    : urlResolver.getFileUrl(getContentId(), image.getPath());
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Unable to get preview path", e);
        }

        return null;
    }

    public Image getSmallThumbnail()
        throws CMException,
               IOException
    {
        return getImageSet() == null ? null
            : getImageSet().getThumbnail(ImageSetPolicy.THUMBNAIL_SIZE_SMALL);
    }

    public Image getMediumThumbnail()
        throws CMException,
               IOException
    {
        return getImageSet() == null ? null
            : getImageSet().getThumbnail(ImageSetPolicy.THUMBNAIL_SIZE_MEDIUM);
    }

    public Image getOriginalImage()
        throws CMException,
               IOException
    {
        return getImageSet() == null ? null : getImageSet().getImage();
    }

    /**
     * Gets pre-generated derivative images wrapped as a Map.
     *
     * @return a map of the pre-generated derivatives
     * @throws CMException
     */
    public ImageSetAsMap getDerivatives()
        throws CMException
    {
        return getImageSet() == null ? null : new ImageSetAsMap(getImageSet());
    }

    /*
     * Getters for metadata
     */

    public String getDesc()
    {
        String createdTime = getCreationTime();
        String contact = getChildValue("contact");

        return "Photo: " + getChildValue("byline", "N/A")
               + (!StringUtil.isEmpty(contact) ? " (" + contact + ")" : "")
               +  ", License: " + getChildValue("licenseurl", "N/A") + createdTime;
    }

    /**
     * Get time of image creation as a string.
     *
     * @return time of image creation as a string.
     */
    private String getCreationTime()
    {
        String creationMetaData = (String) getExifMetadata().get("[Exif]Date/Time Original");
        String createdTime;

        if (creationMetaData != null && !"".equals(creationMetaData)) {
            createdTime = ", Created: " + creationMetaData;
        } else {
            createdTime = "";
        }

        return createdTime;
    }

    /**
     * Gets EXIF metadata
     */
    public Map<?, ?> getExifMetadata()
    {
        Map<?, ?> imageExifMetadata;

        try {
            imageExifMetadata = ((ImageExifMetadataProvider) getOriginalImage()).getExifMetadata();
        } catch (Exception e) {
            imageExifMetadata = new HashMap<Object, Object>();
        }

        return imageExifMetadata;
    }

    /**
     * Gets IPTC metadata
     */
    public Map<?, ?> getIptcMetadata()
    {
        Map<?, ?> imageIptcMetadata;

        try {
            imageIptcMetadata = ((ImageIptcMetadataProvider) getOriginalImage()).getIptcMetadata();
        } catch (Exception e) {
            imageIptcMetadata = new HashMap<Object, Object>();
        }

        return imageIptcMetadata;
    }

    /*
     * Convenience methods
     */
    protected ImageManagerPolicy getImageProvider()
        throws CMException
    {
        SelectableSubFieldPolicy selectableSubFieldPolicy =
                (SelectableSubFieldPolicy) getChildPolicy("imageType");

        String selectedField = selectableSubFieldPolicy.getSelectedSubFieldName();

        if (selectedField == null) {
            selectedField = "image";
        }

        return (ImageManagerPolicy) selectableSubFieldPolicy.getChildPolicy(selectedField);
    }

    // ------------------------------------------------------------------------
    // ImageResource
    // ------------------------------------------------------------------------
    public String getImageName()
    {
        try {
            return getName();

        } catch (CMException cme) {
            LOG.log(Level.WARNING, "Unable to read image name from " + getContentId(), cme);
        }

        return null;
    }

    public String getImageDescription()
    {
        return getDesc();
    }

    public ImageSet getImageSet()
    {
        try {
            ImageProvider imageProvider = getImageProvider();

            if (imageProvider != null) {
                return imageProvider.getSelectedImage();
            }
        } catch (CMException cme) {
            LOG.log(Level.WARNING, "Unable to read image set from " + getContentId(), cme);
        }

        return null;
    }

    // ------------------------------------------------------------------------
    // ImageGenerator
    // ------------------------------------------------------------------------
    public GeneratedImage generateImage(String path,
                                        long modifiedSince)
        throws IOException,
               ImageGenerationFailedException
    {
        try {
            return getImageProvider().generateImage(path, modifiedSince);
        } catch (CMException cme) {
            throw new ImageGenerationFailedException
                ("Unable to generate image:" + path + " for content:" + getContentId(), cme);
        }
    }
}
