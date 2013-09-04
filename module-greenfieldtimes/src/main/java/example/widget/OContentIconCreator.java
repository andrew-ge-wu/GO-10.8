package example.widget;

import com.polopoly.cm.app.orchid.widget.OContentIcon;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

public class OContentIconCreator
{
    /**
     * Creates a content info icon given the policy.
     *
     * @param policy the policy to create content info icon for
     * @param cmServer the policycmserver to use
     *
     * @return the created content info icon
     */
    public OContentIcon createContentIcon(final Policy policy,
                                          final PolicyCMServer cmServer)
    {
        OContentIcon entryIcon = new OContentIcon();

        entryIcon.initContent(policy, cmServer);
        entryIcon.setStylesheetClass("icon");

        return entryIcon;
    }
}
