package example.content.image;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentFileInfo;
import com.polopoly.cm.app.Resource;
import com.polopoly.cm.app.imagemanager.ImageSet;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentRead;

import example.content.ImageProviderPolicy;

/**
 * Policy representing an image resource.
 */

public class ImagePolicy extends ImageProviderPolicy
    implements Resource
{

    protected static final String BINARY_IMAGE = "image";
    Logger LOG = Logger.getLogger(ImageProviderPolicy.class.getName());

    /**
     * Get image specific clip
     */
    public Map<String, String> getResourceData() throws CMException
    {
        try {
            ImageSet selectedImage = getImageSet();

            if (selectedImage != null) {
                Map<String, String> map = new HashMap<String, String>();

                // Use original image when pasting copy/pasting into an p.ImageManager.
                // Note that getFileInfo and exportFile must support this path, 
                // i.e. derivates are not supported unless front generation is disabled 
                // and exclusively using p.ImageManager (not p.HttpImageManager).
                String originalPath = selectedImage.getImage().getPath();
                // Use landscape_490 derivative when copy/pasting images
                String path = selectedImage.getImage("landscape_490").getPath();
                map.put(Resource.FIELD_CONTENT_FILE_PATH, path);
                map.put(Resource.FIELD_RESOURCE_TYPE, BINARY_IMAGE);
                map.put(Resource.FIELD_IMG_ALT, getName());
                map.put(Resource.FIELD_IMAGE_CONTENT_FILE_PATH, originalPath);

                if (selectedImage.getImage().isAbsolute()) {
                    map.put(Resource.FIELD_AUTHORITATIVE_FILE_URL, path);
                }

                return map;
            }
        } catch (Exception e) {
            logger.logp(Level.WARNING, CLASS, "getResourceData",
                    "Failed to create resource data for " + getContentId(), e);
        }

        return null;
    }
    
    @Override
    public ContentFileInfo getFileInfo(String path)
        throws CMException, IOException
    {
        try {
            ImageSet imageSet = getImageSet();
            if (imageSet instanceof ContentRead) {
                return ((ContentRead) imageSet).getFileInfo(path);
            }
            
        } catch (FileNotFoundException ignore) { }

        return super.getFileInfo(path);
    }
    
    @Override
    public void exportFile(String path, OutputStream data)
        throws CMException, IOException
    {
        try {
            ImageSet imageSet = getImageSet();
            if (imageSet instanceof ContentRead) {
                ((ContentRead) imageSet).exportFile(path, data);
                return;
            }
            
        } catch (FileNotFoundException ignore) { }
        
        super.exportFile(path, data);
    }
}
