package example.layout.element.banner;

import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.layout.slot.BannerSlot;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

/**
 * Describe class RenderControllerBannerElement here.
 */
public class RenderControllerBannerElement
    extends RenderControllerBase
{
    @Override
    public void populateModelAfterCacheKey(RenderRequest request, TopModel m,
            CacheInfo cacheInfo, ControllerContext context) {
        super.populateModelAfterCacheKey(request, m, cacheInfo, context);

        BannerSlot slot = (BannerSlot) m.getContext().getSlot().getBean();

        BannerResourcePolicy policy = getBannerResourcePolicy(context);
        String bannerLink = policy.getBannerLink(slot.getWidth());

        if(bannerLink == null) {
            bannerLink = createBannerLink(slot.getWidth(), slot.getName());
        }

        m.getLocal().setAttribute("bannerLink", bannerLink);
    }

    public String createBannerLink(int width, String slotName)
    {
        // Slot name includes page layout, we just want the actual name of the slot
        slotName = slotName.substring(slotName.lastIndexOf('/') + 1, slotName.length());
        return "/img/banners/" + slotName + width + ".gif";
    }

    public BannerResourcePolicy getBannerResourcePolicy(ControllerContext context) 
    {
        return (BannerResourcePolicy)ModelPathUtil.getBean(context.getContentModel());
    }
}
