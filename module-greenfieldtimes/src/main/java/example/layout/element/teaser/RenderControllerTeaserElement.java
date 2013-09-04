package example.layout.element.teaser;

import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

import example.content.ImageProviderPolicy;
import example.layout.image.ImageFormatSetup;

/**
 * A render controller for <strong>Teaser elements</strong> to prepare data for
 * the presentation view.
 */
public class RenderControllerTeaserElement
    extends RenderControllerBase
{
    @Override
    public void populateModelBeforeCacheKey(RenderRequest request, TopModel m, ControllerContext context) {
        super.populateModelBeforeCacheKey(request, m, context);
        m.getLocal().setAttribute("article", ModelPathUtil.get(context.getContentModel(), "articles/list[0]/content"));
    }

    @Override
    public void populateModelAfterCacheKey(RenderRequest request, TopModel m,
            CacheInfo cacheInfo, ControllerContext context) {

        ModelWrite local = m.getLocal();
        Model contentModel = context.getContentModel();

        try {
            TeaserPolicy teaser = (TeaserPolicy) ModelPathUtil.getBean(contentModel);
            local.setAttribute("hasArticleReference", teaser.getArticleId() != null);

            ImageProviderPolicy referredImage = teaser.getReferredImage();
            if (referredImage != null) {
                local.setAttribute("referredImageId", referredImage.getContentId());

                String derivativeType;
                Object customDerivativeType = m.getStack().getAttribute("derivativeType");
                if(customDerivativeType != null &&
                        customDerivativeType instanceof String &&
                        ((String)customDerivativeType).length() > 0) {
                    derivativeType = (String) customDerivativeType;
                } else {
                    derivativeType = (String) ModelPathUtil.get(context.getContentModel(),
                                                                   "image/selectedDerivativeType");
                }

                new ImageFormatSetup().setupImageDerivativeKeyInModel(m, derivativeType, referredImage.getImageSet());

                setImagePosition(m, derivativeType);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to setup model", e);
        }

        super.populateModelAfterCacheKey(request, m, cacheInfo, context);
    }

    private void setImagePosition(TopModel m, String derivativeType)
    {
        Object imagePositionObj = m.getStack().getAttribute("imagePosition");
        String imagePosition = imagePositionObj instanceof String ? (String) imagePositionObj : null;
        if (imagePosition == null || imagePosition.length() == 0) {
            if ("landscape".equals(derivativeType) || derivativeType == null) {
                imagePosition = "top";
            } else if ("box".equals(derivativeType)) {
                imagePosition = "inline";
            }
        }
        m.getLocal().setAttribute("imagePosition", imagePosition);
    }
}
