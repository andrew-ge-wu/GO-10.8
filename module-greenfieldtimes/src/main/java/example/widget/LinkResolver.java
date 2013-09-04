package example.widget;

import com.polopoly.cm.policy.Policy;
import com.polopoly.orchid.context.OrchidContext;

/**
 * Resolve a link (ie. a string based URL).
 */
public interface LinkResolver
{
    /**
     * Resolve a link.
     *
     * @param oc
     * @param policy the policy used to resolve the link from.
     * @return a string based URL.
     * @throws UnresolvableLinkException
     */
    public String resolveLink(OrchidContext oc, Policy policy) throws UnresolvableLinkException;
}
