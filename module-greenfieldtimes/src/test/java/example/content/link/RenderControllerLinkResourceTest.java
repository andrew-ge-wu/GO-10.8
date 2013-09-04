package example.content.link;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.mockito.Mock;

import com.polopoly.model.ModelBase;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

import example.MockitoBase;

public class RenderControllerLinkResourceTest extends MockitoBase {

    @Mock
    private TopModel topModel;

    private RenderControllerLinkResource target;

    @Mock
    private RenderRequest request;

    @Mock
    private ControllerContext context;

    private ModelBase localModel;

    private Map<String, String> attrsToInherit;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        target = new RenderControllerLinkResource();

        localModel = new ModelBase();
        attrsToInherit = new HashMap<String, String>();

        when(topModel.getLocal()).thenReturn(localModel);
    }

    public void testShouldUseInheritedLinkTextIfPresent()
        throws Exception
    {
        when(request.getAttribute(RenderControllerLinkResource.ATTR_NAME_LINK_TEXT)).thenReturn("inherited");

        target.populateModelBeforeCacheKey(request, topModel, context);

        assertTrue("Link did not use inherited text", localModel.getAttribute("text").equals("inherited"));
    }

    public void testShouldUseAllInheritedAttributesIfPresent()
        throws Exception
    {
        attrsToInherit.put("class", "right");
        attrsToInherit.put("style", "padding: 4px;");
        attrsToInherit.put("polopoly:contentid", "1.100.1000");

        when(request.getAttribute(RenderControllerLinkResource.ATTR_NAME_LINK_ATTRS)).thenReturn(attrsToInherit);

        target.populateModelBeforeCacheKey(request, topModel, context);

        assertTrue("Link did not preserve html-attribute",
                   ((String)((Map<?, ?>) (localModel.getAttribute("attrs"))).get("class"))
                       .indexOf("right") != -1);

        assertTrue("Link did not preserve html-attribute",
                   ((String)((Map<?, ?>) (localModel.getAttribute("attrs"))).get("style"))
                       .indexOf("padding: 4px;") != -1);

        assertTrue("Link did not preserve polopoly-attribute",
                   ((String)((Map<?, ?>) (localModel.getAttribute("attrs"))).get("polopoly:contentid"))
                       .indexOf("1.100.1000") != -1);
    }

    public void testShouldNeverUseInheritedHrefIfPresent()
        throws Exception
    {
        attrsToInherit.put("href", "http://www.polopoly.com");
        ModelPathUtil.set(localModel, "content/link/selected/href/value", "http://www.atex.com");

        when(request.getAttribute(RenderControllerLinkResource.ATTR_NAME_LINK_ATTRS)).thenReturn(attrsToInherit);

        target.populateModelBeforeCacheKey(request, topModel, context);

        assertTrue("Link did not use local href",
                   ((String)((Map<?, ?>) (localModel.getAttribute("attrs"))).get("href"))
                       .indexOf("http://www.atex.com") != -1);
    }

    public void testShouldUseInheritedTitleEvenIfLocalIsPresent()
        throws Exception
    {
        attrsToInherit.put("title", "inheritedTitle");

        ModelPathUtil.set(localModel, "content/title/value", "localTitle");

        when(request.getAttribute(RenderControllerLinkResource.ATTR_NAME_LINK_ATTRS)).thenReturn(attrsToInherit);

        target.populateModelBeforeCacheKey(request, topModel, context);

        assertTrue("Link did not use inherited title",
                   ((String)((Map<?, ?>) (localModel.getAttribute("attrs"))).get("title"))
                       .indexOf("inheritedTitle") != -1);
    }
}
