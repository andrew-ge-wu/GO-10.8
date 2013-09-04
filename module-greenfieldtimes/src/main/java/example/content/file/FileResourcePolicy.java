package example.content.file;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.plugins.baseline.url.UrlResolver;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.Resource;
import com.polopoly.cm.app.policy.FilePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;

import example.content.ResourceBasePolicy;

/**
 * A Policy representing a file resource.
 */
public class FileResourcePolicy extends ResourceBasePolicy
    implements Resource
{
    private static final Logger LOG = Logger.getLogger(FileResourcePolicy.class.getName());
    private static final ContentId ICON_EXTERNAL_ID = new ExternalContentId("p.Icons");

    private static final String RESOURCE_TYPE = "file";

    private ContentId iconContentId;
    private FileExistsChecker fileExistsChecker;

    protected void initSelf()
    {
        super.initSelf();

        try {
            Content iconContent = (Content) getCMServer().getContent(ICON_EXTERNAL_ID);

            iconContentId = iconContent.getContentId();
            fileExistsChecker = new FileExistsChecker(iconContent);
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Unable to find icon content", e);
            iconContentId = ICON_EXTERNAL_ID;
        }
    }

    /**
     * Returns the icon path for the image size "48x48".
     *
     * @param urlResolver the URL resolver to use
     * @return the icon path
     */
    public String getThumbnailPath(UrlResolver urlResolver)
    {
        return getIconPath(urlResolver, "48x48");
    }

    /**
     * Returns the icon path for the image size "16x16".
     *
     * @param urlResolver the URL resolver to use
     * @return the icon path
     */
    public String getSmallIconPath(UrlResolver urlResolver)
    {
        return getIconPath(urlResolver, "16x16");
    }

    /**
     * Returns the path to the icon for a specified image size.
     * The image size can either be "16x16" or "48x48".
     *
     * @param urlResolver the URL resolver to use
     * @param imageSize the image size
     * @return the icon path
     */
    String getIconPath(UrlResolver urlResolver,
                       String imageSize)
    {
        String iconPath = null;
        String mimeType = null;

        try {
            mimeType = getFilePolicy().getMimeType();

            String fileName = "image/" + imageSize + "/" + mimeType + ".png";

            if (fileExistsChecker == null || !fileExistsChecker.fileExists(fileName)) {
                fileName = "/image/" + imageSize +
                           "/application/octet-stream.png";
            }

            iconPath = urlResolver.getFileUrl(iconContentId, fileName);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Unable to get icon file info for mime-type: " + mimeType, e);
        }

        return iconPath;
    }

    /**
     * Returns the full path to the file
     *
     * @param urlResolver the URL resolver to use
     */
    public String getPreviewPath(UrlResolver urlResolver)
    {
        String previewPath = null;

        try {
            String path = getFilePolicy().getFullFilePath();

            if (path != null) {
                previewPath = urlResolver.getFileUrl(getContentId(), path);
            }
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Unable to resolve file preview path", e);
        }

        return previewPath;
    }

    /**
     * Returns the file policy
     *
     * @return the file policy of this file resource
     * @throws CMException if something goes wrong
     */
    public FilePolicy getFilePolicy()
        throws CMException
    {
        return (FilePolicy) getChildPolicy("file");
    }

    public Map<String, String> getResourceData()
        throws CMException
    {
        try {
            FilePolicy filePolicy = getFilePolicy();
            String fileName = filePolicy.getFileName();

            if (fileName != null) {
                Map<String, String> map = new HashMap<String, String>();

                String filePath = filePolicy.getFullFilePath();

                map.put(Resource.FIELD_CONTENT_FILE_PATH, filePath);
                map.put(Resource.FIELD_RESOURCE_TYPE, RESOURCE_TYPE);
                map.put(Resource.FIELD_IMG_ALT, getName());

                return map;
            }
        } catch (Exception e) {
            logger.logp(Level.WARNING, CLASS, "getResourceData",
                    "Failed to create resource data for " + getContentId(), e);
        }

        return null;
    }
}
