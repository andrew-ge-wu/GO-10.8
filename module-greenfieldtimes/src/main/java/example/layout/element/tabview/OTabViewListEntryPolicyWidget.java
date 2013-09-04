package example.layout.element.tabview;

import java.io.IOException;
import java.util.Iterator;

import com.atex.plugins.baseline.widget.OContentListEntryBasePolicyWidget;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.app.orchid.widget.OContentIcon;
import com.polopoly.cm.app.orchid.widget.OContentIdLink;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentListRead;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;

import example.widget.OContentIconCreator;

/**
 * Content list entry renderer widget that shows the contents of the column
 * splitter element in a schematic way when viewed in the GUI.
 */
@SuppressWarnings("serial")
public class OTabViewListEntryPolicyWidget extends OContentListEntryBasePolicyWidget {

    private final OContentIconCreator contentIconCreator = new OContentIconCreator();

    @Override
    protected void renderEntryBody(final Device device,
                                   final OrchidContext oc)
        throws OrchidException, IOException
    {
        try {
            ContentPolicy policy = (ContentPolicy) contentPolicy;
            ContentListRead elements = policy.getContentList("elements/slotElements");

            boolean first = true;
            String firstElementName = "";
            Iterator<ContentReference> iter = elements.getListIterator();

            boolean hasElements = false;

            if (iter.hasNext()) {
                hasElements = true;
            }

            if (hasElements) {
                device.println("<div class='tabviewElementClear'>&nbsp;</div>");
                device.println("<div class='tabviewElement'>");
                device.println("<ul>");
            }

            while (iter.hasNext()) {
                ContentReference entryRef = iter.next();

                ContentId entryId = entryRef.getReferredContentId();
                ContentPolicy entry = (ContentPolicy) _cmServer.getPolicy(entryId);

                OContentIcon entryIcon = contentIconCreator.createContentIcon(entry, _cmServer);
                entryIcon.init(oc);
                entryIcon.update(oc);
                OContentIdLink entryLink = new OContentIdLink();
                entryLink.setContentId(entryId);
                entryLink.setLabel(entry.getName());
                entryLink.init(oc);

                String liStyle = "";

                if (first) {
                    liStyle = " class='selected'";
                    firstElementName = entry.getName();
                    first = false;
                }

                device.println("<li" + liStyle + ">");
                device.println("<span>");
                entryIcon.render(oc);
                entryLink.render(oc);
                device.println("</span>");
                device.println("</li>");
            }

            if (hasElements) {
                device.println("</ul>");

                if (firstElementName != null && firstElementName.length() > 0) {
                    device.println("<div class='hr'></div>");
                    device.println("<div class='tabviewElementBody'>" + firstElementName + "...</div>");
                }

                device.println("</div>");
            }
        } catch (CMException cme) {
            handleError(cme, oc);
        }
    }
}
