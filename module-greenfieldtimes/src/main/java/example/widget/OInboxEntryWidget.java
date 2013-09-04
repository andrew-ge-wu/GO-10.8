package example.widget;

import java.io.IOException;

import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.orchid.event.ActionButtons;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.util.MessageUtil;
import com.polopoly.orchid.widget.OFrameEventImage;
import com.polopoly.user.server.Caller;

@SuppressWarnings("serial")
public class OInboxEntryWidget extends OSearchItemBaseWidget {
    private OFrameEventImage editButton;

    public void initSelf(OrchidContext oc) {
        super.initSelf(oc);
        try {
            VersionedContentId contentId = getPolicy().getContentId();

            PolicyCMServer cmServer = getPolicy().getCMServer();
            Caller caller = cmServer.getCurrentCaller();
            // Use an edit button that doesn't check for locks to avoid
            // risking an extra server roundtrip if the lock cache has timed out
            editButton = ActionButtons.getEditButton(contentId, caller.getUserId(), true, false, cmServer, oc);
            addAndInitChild(oc, editButton);
        }
        catch (OrchidException e) {
            MessageUtil.addErrorMessage(oc, e.getMessage());
        } catch (CMException e) {
            MessageUtil.addErrorMessage(oc, e.getMessage());
        }

        // No checkbox please
        _checkbox = null;
    }

    @Override
    protected void renderToolButtons(Device device, OrchidContext oc)
            throws OrchidException, IOException {
        super.renderToolButtons(device, oc);
        editButton.render(oc);
    }
}
