package example.content.image;

import java.io.IOException;

import com.polopoly.cm.app.widget.OContextTopPolicyWidget;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;

public class OImageContextPolicyWidget extends OContextTopPolicyWidget {

    private static final long serialVersionUID = 7147507028537341489L;

    public void localRender(OrchidContext oc) throws OrchidException, IOException {

        if (getContentListEntryWidget() != null) {
            getContentListEntryWidget().render(oc);
        }

        if (!isFinished()) {
            Device device = oc.getDevice();

            device.println("<div class='customContainer customEntry'>");
            device.println("<div class='imageEntryContainer'>");

            if (getToolbarWidget() != null) {
                device.println("<div class='contextTools'>");
                getToolbarWidget().render(oc);
                device.println("</div>");
            }

            device.println("<div class='fields'>");
            renderFields(oc);
            device.println("</div>");
            device.println("</div>");
            device.println("</div>");
        }
    }
}
