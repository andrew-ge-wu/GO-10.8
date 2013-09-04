package example.content.image;

import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;
import com.polopoly.siteengine.standard.image.ImageResource;

import example.layout.image.ImageFormatSetup;

public class RenderControllerImage
    extends RenderControllerBase
{

    @Override
    public void populateModelAfterCacheKey(RenderRequest request, TopModel m,
            CacheInfo cacheInfo, ControllerContext context) {

        ImageResource image = (ImageResource) ModelPathUtil.getBean(context.getContentModel());
        new ImageFormatSetup().setupImageDerivativeKeyInModel(m, "landscape", image.getImageSet());

        super.populateModelAfterCacheKey(request, m, cacheInfo, context);
    }
}
