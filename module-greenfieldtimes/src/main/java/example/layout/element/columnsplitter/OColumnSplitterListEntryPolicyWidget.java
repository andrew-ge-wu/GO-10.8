package example.layout.element.columnsplitter;

import java.io.IOException;
import java.util.Iterator;

import com.atex.plugins.baseline.widget.OContentListEntryBasePolicyWidget;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.app.orchid.widget.OContentIcon;
import com.polopoly.cm.app.orchid.widget.OContentIdLink;
import com.polopoly.cm.app.policy.SelectPolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentListRead;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;

import example.layout.element.ElementPolicy;
import example.widget.OContentIconCreator;

/**
 * Content list entry rendering widget that will show
 * the contents of the column splitter element in a
 * schematic way when viewed in the GUI.
 *
 * Only shows one level of contents.
 */
@SuppressWarnings("serial")
public class OColumnSplitterListEntryPolicyWidget extends OContentListEntryBasePolicyWidget
{
    private final OContentIconCreator contentIconCreator = new OContentIconCreator();

    /**
     * Renders a column with the contents of the
     * given content list including an icon and
     * a link.
     */
    private void renderColumn(final ContentListRead cList,
                              final Device device,
                              final OrchidContext oc)
        throws OrchidException, CMException, IOException
    {
        for (Iterator<ContentReference> i = cList.getListIterator(); i.hasNext();) {
            ContentReference entryRef = i.next();

            ContentId entryId = entryRef.getReferenceMetaDataId();
            if (entryId == null) {
                entryId = entryRef.getReferredContentId();
            }

            ContentPolicy entry = (ContentPolicy) _cmServer.getPolicy(entryId);

            OContentIcon entryIcon = contentIconCreator.createContentIcon(entry, _cmServer);
            entryIcon.init(oc);
            entryIcon.update(oc);
            OContentIdLink entryLink = new OContentIdLink();
            entryLink.setContentId(entryId);
            entryLink.setLabel(entry.getName());
            entryLink.init(oc);

            device.println("<div class='customEntry'>");
            entryIcon.render(oc);
            entryLink.render(oc);
            device.println("</div>");
        }
    }

    @Override
    protected void renderEntryBody(final Device device,
                                   final OrchidContext oc)
        throws OrchidException, IOException
    {
        try {
            ElementPolicy splitterPolicy = (ElementPolicy) contentPolicy;
            SelectPolicy ratioPolicy = (SelectPolicy) splitterPolicy.getChildPolicy("ratio");

            ContentListRead col1 = splitterPolicy.getContentList("leftSlot/slotElements");
            ContentListRead col2 = splitterPolicy.getContentList("rightSlot/slotElements");

            String containerType = ratioPolicy.getValue();

            device.println("<div class=\"columnSplitterContainer " + containerType + "\">");

            // Column 1
            device.println("<div class=\"col1\">");
            renderColumn(col1, device, oc);
            device.println("</div>");

            // Column 2
            device.println("<div class=\"col2\">");
            renderColumn(col2, device, oc);
            device.println("</div>");

            device.println("</div>");
        } catch (CMException cme) {
            handleError(cme, oc);
        }
    }
}
