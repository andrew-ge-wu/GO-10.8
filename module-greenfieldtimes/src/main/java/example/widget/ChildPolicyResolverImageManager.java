package example.widget;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.cm.policy.Policy;

/**
 * Find the childpolicy of an image manager.
 */
public class ChildPolicyResolverImageManager
    implements ChildPolicyResolver
{
    public Policy resolvePolicy(Policy policy)
    {
        try {
            return policy.getChildPolicy("image");
        } catch (CMException e) {
           throw new CMRuntimeException(e);
        }
    }
}
