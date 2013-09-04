package example.widget;

import com.polopoly.cm.policy.Policy;

/**
 * Resolve a child policy
 */
public interface ChildPolicyResolver
{
    /**
     * Resolve a child policy
     *
     * @param policy the policy to resolve a child policy from.
     * @return the child policy.
     */
    public Policy resolvePolicy(Policy policy);
}
