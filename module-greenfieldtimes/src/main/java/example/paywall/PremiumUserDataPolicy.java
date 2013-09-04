package example.paywall;

import java.util.Collection;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.paywall.Capability;
import com.polopoly.paywall.ContentBundle;
import com.polopoly.paywall.Engagement;
import com.polopoly.paywall.EngagementFieldPolicy;
import com.polopoly.paywall.PremiumUserData;
import com.polopoly.paywall.Subscription;

/**
 * Policy used to store paywall related data on site users.
 * Uses the <code>p.Engagement</code> field.
 */
public class PremiumUserDataPolicy extends ContentPolicy implements PremiumUserData {

    @Override
    public void buySubscription(Subscription subscription) throws CMException {
        ((EngagementFieldPolicy) getChildPolicy("engagement")).buy(subscription);
    }

    @Override
    public Collection<ContentBundle> getAccessibleContentBundlesByCapability(Capability capability) throws CMException {
        return ((EngagementFieldPolicy) getChildPolicy("engagement")).getContentBundlesByCapability(capability);
    }

    @Override
    public Collection<Engagement> getEngagements() throws CMException {
        return ((EngagementFieldPolicy) getChildPolicy("engagement")).getEngagements();
    }

    @Override
    public void clearEngagements() throws CMException {
        ((EngagementFieldPolicy) getChildPolicy("engagement")).clearEngagements();
    }
}
