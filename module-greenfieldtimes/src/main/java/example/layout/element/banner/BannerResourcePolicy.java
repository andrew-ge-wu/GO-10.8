package example.layout.element.banner;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.app.imagemanager.Image;
import com.polopoly.cm.app.imagemanager.ImageProvider;
import com.polopoly.cm.app.imagemanager.ImageSet;
import com.polopoly.cm.client.CMException;
import com.polopoly.siteengine.layout.Element;

import example.content.ContentBasePolicy;

/**
 * A Policy representing a banner resource.
 */
public class BannerResourcePolicy extends ContentBasePolicy implements Element {

    private static final Logger LOG = Logger.getLogger(BannerResourcePolicy.class.getName());

    public String getBannerLink(int width) 
    {
        String link = null;
        String derivativeType = getDerivativeType(width);

        try {
            ImageProvider imageProvider = (ImageProvider) getChildPolicy("image");
            ImageSet selectedImage = imageProvider.getSelectedImage();
            Image image = (selectedImage == null) ? null : selectedImage.getImage(derivativeType);

            if(selectedImage != null) {
                link = "/polopoly_fs/"
                       + image.getImageContentId().getContentIdString()
                       + "!/"
                       + image.getPath();
            }
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Unable to resolve file path", e);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Unable to resolve file path", e);
        }

        return link;
    }

    //derivative type: landscape_993, aspect_300
    public String getDerivativeType(int width) 
    {
        return (width == 993) ? "landscape_993" : (width == 300) ? "aspect_300" : ""; 
    }

}
