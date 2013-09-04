package example.content.editorialblog;

import com.polopoly.model.ModelWrite;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

import example.content.BodyTranslator;
import example.content.RenderControllerExtended;

/**
 * Render controller for {@link BlogPosting}.
 */
public class RenderControllerBlogPosting
    extends RenderControllerExtended
{
    /**
     * Translates inline image paths.
     *
     * @param request standard render request
     * @param m the top model
     * @param cacheInfo the render cache
     * @param context the controller context
     */
    @Override
    public void populateModelAfterCacheKey(RenderRequest request,
                                           TopModel m,
                                           CacheInfo cacheInfo,
                                           ControllerContext context)
    {
        super.populateModelAfterCacheKey(request, m, cacheInfo, context);

        ModelWrite localModel = m.getLocal();

        boolean inPreviewMode = m.getRequest().getPreview().isInPreviewMode();

        BodyTranslator bodyTranslator = new BodyTranslator();
        bodyTranslator.translateBody(request, localModel, "body/value", inPreviewMode);
    }
}
