package example.layout.element.gallery;

import java.util.logging.Logger;

import com.polopoly.cm.app.imagemanager.ImageSet;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;
import com.polopoly.siteengine.standard.image.ImageResource;

import example.layout.image.ImageFormatSetup;

public class GalleryElementController
    extends RenderControllerBase
{
    private static final Logger LOG = Logger.getLogger(GalleryElementController.class.getName());

    @Override
    public void populateModelAfterCacheKey(RenderRequest request, TopModel m,
            CacheInfo cacheInfo, ControllerContext context) {

        Model contentModel = context.getContentModel();

        GalleryElementPolicy gallery = (GalleryElementPolicy) ModelPathUtil.getBean(contentModel);
        try {
            ImageResource image = gallery.getImageResource(0);
            ImageSet imageSet = image != null ? image.getImageSet() : null;
            if (imageSet != null) {
                new ImageFormatSetup().setupImageDerivativeKeyInModel(m, "landscape", imageSet);
            } else {
                LOG.fine("Could not get first image in gallery '"
                         + gallery.getContentId().getContentIdString() + "'.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not get first image in gallery '"
                                       + gallery.getContentId().getContentIdString()
                                       + "'.", e);
        }

        super.populateModelAfterCacheKey(request, m, cacheInfo, context);
    }
}
