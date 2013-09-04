package example.widget;

import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.orchid.context.OrchidContext;

/**
 * Resolve a link from a text input.
 */
public class LinkResolverFromTextInput
    implements LinkResolver
{
    public String resolveLink(OrchidContext oc, Policy policy)
        throws UnresolvableLinkException
    {
        String url;
        try {
            SingleValued singleValued = (SingleValued) policy;

            url = singleValued.getValue();
        } catch (CMException e) {
            throw new CMRuntimeException(e);
        }

        if (url == null || "".equals(url)) {
            throw new UnresolvableLinkException("Can't resolve empty link");
        }
        return url;
    }
}
