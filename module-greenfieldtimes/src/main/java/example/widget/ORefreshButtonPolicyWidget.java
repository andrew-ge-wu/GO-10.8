package example.widget;

import java.io.IOException;

import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.widget.OAbstractPolicyWidget;
import com.polopoly.cm.policy.PolicyUtil;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OSubmitButton;
import com.polopoly.util.LocaleUtil;

/**
 * Submit button field widget.
 */
@SuppressWarnings("serial")
public class ORefreshButtonPolicyWidget extends OAbstractPolicyWidget
    implements Viewer, Editor
{
    private String label;
    private OSubmitButton refreshButton;

    public void initSelf(OrchidContext oc)
        throws OrchidException
    {
        super.initSelf(oc);

        refreshButton = new OSubmitButton();

        if (label == null) {
            String configuredLabel = PolicyUtil.getLabel(getPolicy());
            label = LocaleUtil.format(configuredLabel, oc.getMessageBundle());
        }

        refreshButton.setLabel(label);
        addAndInitChild(oc, refreshButton);
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public void localRender(OrchidContext oc)
        throws OrchidException,
               IOException
    {
        refreshButton.render(oc);
    }
}
