package example.widget;

import java.io.IOException;

import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.util.PolicyWidgetUtil;
import com.polopoly.cm.app.widget.OComplexFieldPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;

/**
 * Widget presenting a video player in the GUI.
 */
@SuppressWarnings("serial")
public class OVideoPlayerWidget extends OComplexFieldPolicyWidget implements
        Viewer, Editor 
{
    Policy _topPolicy;

    PlayerScriptFactory playerScriptFactory = new PlayerScriptFactory();
    LinkResolver videoLinkResolver;
    LinkResolver imageLinkResolver;
    ChildPolicyResolver imageManagerPolicyResolver;

    public void initSelf(OrchidContext oc) throws OrchidException
    {
        _topPolicy = this.getContentSession().getTopPolicy();
        LinkResolver fileResolver = new LinkResolverFromFile();
        LinkResolver urlResolver = new LinkResolverFromTextInput();

        videoLinkResolver = new LinkResolverVideo(urlResolver, fileResolver);
        imageLinkResolver = new LinkResolverFromImageManager();
        imageManagerPolicyResolver = new ChildPolicyResolverImageManager();
    }

    public void localRender(OrchidContext oc) throws OrchidException,
            IOException
    {

        renderVideoPreview(oc);

    }

    void renderVideoPreview(OrchidContext oc) throws IOException,
            OrchidException
    {
        String videoLink;
        

        try {
            videoLink = videoLinkResolver.resolveLink(oc, _topPolicy);
        } catch (UnresolvableLinkException e) {
            videoLink = "";
        }

        String imageLink;
        try {
            Policy imageManagerPolicy =
                    imageManagerPolicyResolver.resolvePolicy(_topPolicy);
            imageLink = imageLinkResolver.resolveLink(oc, imageManagerPolicy);
        } catch (UnresolvableLinkException e) {
            imageLink = "";
        }

        String widgetId = String.valueOf(getWidgetId());
        
        // Java script for actual player
        String playerString =
                playerScriptFactory.createPlayerScript(videoLink, imageLink, widgetId);

        Device dev = oc.getDevice();
        dev.print(playerString);
                    
    }
    
    public void preRender(OrchidContext oc) throws OrchidException
    {
        try {
            if (PolicyWidgetUtil.isEditMode(this)) {
                getContentSession().getTopWidget().store();
            }
        } catch (CMException e) {
            handleError(e, oc);
        }
    }

    
}
