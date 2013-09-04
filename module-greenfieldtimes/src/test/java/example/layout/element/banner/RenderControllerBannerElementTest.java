package example.layout.element.banner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import junit.framework.TestCase;

import com.polopoly.model.Model;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.CacheInfo;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.layout.slot.BannerSlot;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.model.context.ContextScope;
import com.polopoly.siteengine.model.context.SlotScope;

public class RenderControllerBannerElementTest
    extends TestCase
{
    private RenderControllerBannerElement target;

    public void setUp()
        throws Exception
    {
        target = new RenderControllerBannerElement();
    }

    public void testShouldCreateLinkToImageUsingSlotWidthAndSlotName()
        throws Exception
    {
        String expectedLink = "/img/banners/header600.gif";
        String slotName="header";
        int width = 600;

        String bannerLink = target.createBannerLink(width, slotName);

        assertEquals(expectedLink, bannerLink);
    }

    public void testBannerLinkIsAddedToLocalModel()
        throws Exception
    {
        ModelWrite local = mock(ModelWrite.class);
        TopModel top = mock(TopModel.class);

        when(top.getLocal()).thenReturn(local);

        Model model = mock(Model.class);
        ControllerContext context = mock(ControllerContext.class);

        when(context.getContentModel()).thenReturn(model);

        BannerResourcePolicy bannerResourcePolicy = mock(BannerResourcePolicy.class);

        when(bannerResourcePolicy.getBannerLink(0)).thenReturn(null);

        BannerSlot bannerSlot = mock(BannerSlot.class);
        ContextScope contextScope = mock(ContextScope.class);

        when(top.getContext()).thenReturn(contextScope);

        SlotScope slotScope = mock(SlotScope.class);

        when(contextScope.getSlot()).thenReturn(slotScope);
        when(slotScope.getBean()).thenReturn(bannerSlot);

        when(bannerSlot.getWidth()).thenReturn(954);
        when(bannerSlot.getName()).thenReturn("selected/pagelayout/footer");

        CacheInfo cacheInfo = mock(CacheInfo.class);
        RenderControllerBannerElement element = new RenderControllerBannerElement();

        when(element.getBannerResourcePolicy(context)).thenReturn(bannerResourcePolicy);

        element.populateModelAfterCacheKey(null, top, cacheInfo, context);

        verify(local).setAttribute("bannerLink", "/img/banners/footer954.gif");
    }
}
