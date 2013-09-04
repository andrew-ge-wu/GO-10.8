package example.widget;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.orchid.event.Actions;
import com.polopoly.cm.app.orchid.widget.ContentStateRenderHelper;
import com.polopoly.cm.app.orchid.widget.OContentIcon;
import com.polopoly.cm.app.util.UIUtil;
import com.polopoly.cm.app.util.UntitledListEntryToContentIdTitle;
import com.polopoly.cm.app.widget.OAbstractPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.event.EventData;
import com.polopoly.orchid.js.JSButton;
import com.polopoly.orchid.js.JSClipEncoder;
import com.polopoly.orchid.js.JSWidgetUtil;
import com.polopoly.orchid.widget.OCheckbox;
import com.polopoly.orchid.widget.OFrameEventLink;
import com.polopoly.util.StringUtil;

/**
 * Base widget presenting a search result item in the gui.
 */
public class OSearchItemBaseWidget extends OAbstractPolicyWidget
    implements Viewer, Editor
{
    private static final long serialVersionUID = 4131397936409158454L;
    private static final Logger LOG = Logger.getLogger(OSearchItemBaseWidget.class.getName());

    protected OContentIcon _icon;
    protected OFrameEventLink _nameLink;
    protected JSButton _copyButton;
    protected OCheckbox _checkbox;

    protected UntitledListEntryToContentIdTitle untitledToContentIdTitle =
        new UntitledListEntryToContentIdTitle();

    protected ContentStateRenderHelper contentStateRenderHelper;

    public void initSelf(OrchidContext oc)
    {
        try {
            ContentPolicy policy = (ContentPolicy) getPolicy();

            // Create and init content icon holder
            _icon = new OContentIcon();
            _icon.initContent((Policy) policy, getContentSession().getPolicyCMServer());
            addAndInitChild(oc, _icon);

            // Create link and event to open content in view mode
            _nameLink = new OFrameEventLink();
            addAndInitChild(oc, _nameLink);

            String contentName = policy.getContent().getName();
            ContentId contentId = policy.getContentId().getContentId();

            String label = untitledToContentIdTitle.createTitle(contentName,contentId);
            _nameLink.setLabel(label);

            EventData eventData = createLinkEvent(policy, oc);
            _nameLink.setEventData(eventData);

            // Create copy button
            String encodedClip = JSClipEncoder.getDefaultEncoder().encodeClip(
                    UIUtil.getResourceData(oc, getPolicy()));

            if (!StringUtil.isEmpty(encodedClip)) {
                _copyButton = JSWidgetUtil.createCopyButton(oc);
                _copyButton.setOnClick("JSClipboard.copyEncodedClip(\""
                        + encodedClip + "\")");

                addAndInitChild(oc, _copyButton);

                // Create checkbox
                _checkbox = new OCheckbox();
                addAndInitChild(oc, _checkbox);
                _checkbox.setInitialValue(encodedClip);
            }
        } catch (Exception e) {
            LOG.logp(Level.WARNING, CLASS, "initSelf",
                    "Could  not initialize", e);
        }
    }

    protected EventData createLinkEvent(final ContentPolicy policy,
                                        final OrchidContext oc)
    {
        return Actions.getViewEventData(policy.getContent().getContentId().getContentId());
    }

    /**
     * Returns true if selected, false if not
     *
     * @return true if selected, false if not
     */
    public boolean isSelected()
    {
        return _checkbox != null ? _checkbox.isChecked() : false;
    }

    public void localRender(OrchidContext oc)
        throws OrchidException, IOException
    {
        Device device = oc.getDevice();
        contentStateRenderHelper = new ContentStateRenderHelper((ContentPolicy) getPolicy());

        try {
            device.println("<div class='customContainer customSearchHit draggable"
                           + contentStateRenderHelper.getStateCssClassNames()
                           + "' polopoly:contentid='" + getPolicy().getContentId().getContentId().getContentIdString()
                           + "' polopoly:inputtemplateid='" + getPolicy().getInputTemplate().getContentId().getContentId().getContentIdString()
                           + "' polopoly:widgetid='" + this.getCompoundId()
                           + "'>");
        } catch (CMException e) {
            throw new OrchidException(e);
        }
        
        renderEntryHeader(device, oc);
        renderEntryBody(device, oc);
        renderEntryFooter(device, oc);

        device.println("</div>");
    }

    protected void renderEntryHeader(final Device device,
                                     final OrchidContext oc)
        throws OrchidException, IOException
    {
        device.println("<div class=\"listEntryHeader clearfix\">");

        renderToolbox(device, oc);
        renderContentTitle(device, oc);

        device.println("</div>");
    }

    protected void renderEntryBody(final Device device,
                                   final OrchidContext oc)
        throws OrchidException, IOException
    {

    }

    protected void renderEntryFooter(final Device device,
                                     final OrchidContext oc)
        throws OrchidException, IOException
    {

    }

    protected void renderToolbox(final Device device,
                                 final OrchidContext oc)
        throws OrchidException, IOException
    {
        device.println("<div class='tools'>");
        renderToolButtons(device, oc);
        device.println("</div>");
    }

    protected void renderToolButtons(final Device device,
                                     final OrchidContext oc)
        throws OrchidException, IOException
    {
        if (_copyButton != null) {
            _copyButton.render(oc);
        }

        if (_checkbox != null) {
            _checkbox.render(oc);
        }
    }

    protected void renderContentTitle(final Device device,
                                      final OrchidContext oc)
        throws OrchidException, IOException
    {
        device.println("<div class=\"listEntryContentInfo\">");

        renderIcon(device, oc);
        if (contentStateRenderHelper != null) {
            contentStateRenderHelper.renderIcons(oc);
        }
        renderLink(device, oc);

        
        device.println("</div>");
    }

    protected void renderIcon(final Device device,
                              final OrchidContext oc)
        throws OrchidException, IOException
    {
        _icon.render(oc);
    }

    protected void renderLink(final Device device,
                              final OrchidContext oc)
        throws OrchidException, IOException
    {
        _nameLink.render(oc);
    }
}
