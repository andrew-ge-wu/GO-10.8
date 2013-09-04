package example.content;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.plugins.baseline.policy.BaselinePolicy;
import com.polopoly.cm.ContentInfo;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.UserDataPolicy;

/**
 * Base policy for all policies.
 */
public class ContentBasePolicy extends BaselinePolicy implements ContentBaseModelTypeDescription
{
    private static Logger LOG = Logger.getLogger(ContentBasePolicy.class.getName());

    /**
     * Convenience method to access value of <code>SingleValued</code> child
     * policies. The method is null-safe and will return "" if the child policy
     * or component doesn't exist.
     *
     * @param name name of the child policy
     *
     * @return the value of the child policy
     * @exception CMException if an error occurs
     */
    public final String getChildValue(String name)
    {
        return getChildValue(name, "");
    }

    /**
     * Convenience method to access value of <code>SingleValued</code> child
     * policies. The method is null-safe and will return the given default value
     * if the child policy or component doesn't exist.
     *
     * @param name the name of the child policy
     * @param defaultValue the desired default value
     *
     * @return the value of the child policy
     * @exception CMException if an error occurs
     */
    public final String getChildValue(String name, String defaultValue)
    {
        try {
            SingleValued child = (SingleValued) getChildPolicy(name);

            if (child == null) {
                return defaultValue;
            }

            return (child.getValue() != null) ? child.getValue() : defaultValue;
        } catch (ClassCastException cce) {
            LOG.warning(name + " in " + getContentId() + " has unsupported policy.");
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error getting child value", e);
        }

        return defaultValue;
    }

    protected void setChildValue(String singleValuedPolicyName, String value)
        throws CMException
    {
        Policy singleValued = getChildPolicy(singleValuedPolicyName);
        if (singleValued instanceof SingleValued) {
            ((SingleValued) singleValued).setValue(value);
        } else {
            LOG.warning("Failed to set value=" + value + " for " + singleValuedPolicyName
                        + " in " + getContentId() + "(policy was "
                        + (singleValued != null ? singleValued.getClass().getName() : "null") + ")");
        }
    }

    /**
     * Gets creation date of this content in format (yyyy-MM-dd).
     */
    public Date getCreated()
        throws CMException
    {
        ContentInfo info = getCMServer().getContentInfo(getContentId());
        return new Date(info.getCreationTime());
    }

    /**
     * Gets modification date in format (yyyy-MM-dd). Uses commit date of
     * content. Will return now if content is not committed, e.g. in preview.
     */
    public Date getModified()
    {
        Date commitDate = getVersionInfo().getVersionCommitDate();

        // If is preview, this version will not have been committed yet. Use now
        // insetead.
        if (commitDate != null) {
            return commitDate;
        }

        return new Date();
    }

    /**
     * Gets creator, i.e. the creator of the first version of this content.
     *
     * @return the {@link UserDataPolicy} of the creator
     * @throws CMException
     */
    public UserDataPolicy getCreator() throws
        CMException
    {
        ContentInfo info = getCMServer().getContentInfo(getContentId());

        UserDataPolicy userPolicy =
                (UserDataPolicy) getCMServer().getPolicy(
                        new ExternalContentId(info.getCreatedBy().getPrincipalIdString()));

        return userPolicy;
    }

    /**
     * Get modifier, i.e. the creator of the latest version of this content.
     */
    public UserDataPolicy getModifier()
        throws CMException
    {
        UserDataPolicy userPolicy =
                (UserDataPolicy) getCMServer().getPolicy(
                        new ExternalContentId(getCreatedBy().getPrincipalIdString()));

        return userPolicy;
    }
}
