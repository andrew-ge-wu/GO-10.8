package example.widget;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mock;

import com.polopoly.cm.policy.Policy;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;

public class OVideoPlayerWidgetTest extends VideoPlayerBaseTestCase {
    private OVideoPlayerWidget target;
    @Mock
    private PlayerScriptFactory playerScriptFactory;
    @Mock
    private LinkResolver videoLinkResolver;
    @Mock
    private LinkResolver imageLinkResolver;
    private String playerScript;
    @Mock
    private ChildPolicyResolver imageManagerPolicyResolver;
    @Mock
    private Device dev;
    private int widgetId;

    @SuppressWarnings("serial")
    protected void setUp() throws Exception
    {
        super.setUp();
        when(oc.getDevice()).thenReturn(dev);
        widgetId = 100;
        
        target = new OVideoPlayerWidget() {
            public int getWidgetId() throws OrchidException
            {
                return widgetId;
            }
        };

        target._topPolicy = policy;

        target.playerScriptFactory = playerScriptFactory;

        target.videoLinkResolver = videoLinkResolver;

        target.imageLinkResolver = imageLinkResolver;

        target.imageManagerPolicyResolver = imageManagerPolicyResolver;
      
        playerScript = "script";

    }

    public void testRenderShouldRenderPlayerScriptWithLinksProvidedByLinkFactories()
            throws Exception
    {

        when(videoLinkResolver.resolveLink(oc, policy)).thenReturn(videoLink);

        Policy childPolicy = mock(Policy.class);

        when(imageManagerPolicyResolver.resolvePolicy(policy)).thenReturn(childPolicy);

        when(imageLinkResolver.resolveLink(oc, childPolicy)).thenReturn(imageLink);

        when(playerScriptFactory.createPlayerScript(videoLink, imageLink, "" + widgetId)).thenReturn(playerScript);

        target.renderVideoPreview(oc);
        verify(dev).print(playerScript);
    }

    public void testRenderShouldDisplayPlayerWithEmptyFileNameWhenVideoLinkResolverThrowsUnresolvedLinkException()
            throws Exception
    {

        when(videoLinkResolver.resolveLink(oc, policy)).thenThrow(new UnresolvableLinkException("No video link"));

        Policy childPolicy = mock(Policy.class);
        when(imageManagerPolicyResolver.resolvePolicy(policy)).thenReturn(childPolicy);

        when(imageLinkResolver.resolveLink(oc, childPolicy)).thenReturn(imageLink);

        when(playerScriptFactory.createPlayerScript("", imageLink, "" + widgetId)).thenReturn(playerScript);

        target.renderVideoPreview(oc);
        verify(dev).print(playerScript);

    }

    public void testRenderShouldRenderPlayerScriptWithEmptyImageWhenImageLinkThrowsUnresolvedLinkException()
            throws Exception
    {

        when(videoLinkResolver.resolveLink(oc, policy)).thenReturn(videoLink);

        Policy childPolicy = mock(Policy.class);
        when(imageManagerPolicyResolver.resolvePolicy(policy)).thenReturn(childPolicy);

        when(imageLinkResolver.resolveLink(oc, childPolicy)).thenThrow(new UnresolvableLinkException(
                                        "unresolvable image link"));

        when(playerScriptFactory.createPlayerScript(videoLink, "", "" + widgetId)).thenReturn(playerScript);

        target.renderVideoPreview(oc);
        verify(dev).print(playerScript);
    }

}
