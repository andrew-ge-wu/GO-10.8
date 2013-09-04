package example.layout.element.sitegraphics;

import com.polopoly.cm.app.imagemanager.ImageProvider;
import com.polopoly.cm.app.imagemanager.ImageSet;
import com.polopoly.cm.client.CMException;
import com.polopoly.model.ModelTypeDescription;

import example.layout.element.ElementPolicy;

public class SiteGraphicsElementPolicy extends ElementPolicy implements ModelTypeDescription
{
    public ImageSet getImage() throws CMException {
        ImageProvider imageProvider = (ImageProvider) getChildPolicy("image");
        return imageProvider.getSelectedImage();
    }
}
