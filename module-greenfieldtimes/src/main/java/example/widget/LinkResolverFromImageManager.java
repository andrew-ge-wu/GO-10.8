package example.widget;

import java.io.IOException;

import com.polopoly.cm.app.imagemanager.ImageManagerPolicy;
import com.polopoly.cm.app.imagemanager.ImageSetPolicy;
import com.polopoly.cm.app.util.URLBuilder;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.OrchidContext;

/**
 * Resolve a link from an image manager.
 */
public class LinkResolverFromImageManager
    implements LinkResolver
{
    public String resolveLink(OrchidContext oc, Policy policy)
        throws UnresolvableLinkException
    {
        ImageManagerPolicy imageManagerPolicy = (ImageManagerPolicy) policy;

        ImageSetPolicy imagePolicy;
        String imageLink;
        try {
            imagePolicy = imageManagerPolicy.getSelectedImage();
            if (imagePolicy != null) {
                String imagePath = imagePolicy.getImage("preview").getPath();

                imageLink =
                        URLBuilder.getFileUrl(imagePolicy.getContentId(),
                                              imagePath, oc);

            } else {
                throw new UnresolvableLinkException("No image uploaded");
            }
        } catch (CMException e) {
            throw new CMRuntimeException(e);
        } catch (IOException e) {
            throw new UnresolvableLinkException("Could not access image", e);
        } catch (OrchidException e) {
            throw new RuntimeException(e);
        }

        return imageLink;
    }
}
