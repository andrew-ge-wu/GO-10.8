package example.akismet;

import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
    
/**
 * Policy to trim akismet key.
 */
public class AkismetKeyPolicy extends SingleValuePolicy {

    /**
     * Trim or set null when empty.
     */
    public String getValue()
        throws CMException
    {
        return trim(super.getValue());
    }

    /**
     * Trim or set null when empty.
     */
    public void setValue(String value)
        throws CMException
    {
        super.setValue(trim(value));
    }
    
    private String trim(String value)
    {
        if (null == value) {
            return null;
        }

        String trimmedValue = value.trim();

        if (trimmedValue.length() == 0) {
            return null;
        }

        return trimmedValue;
    }
    
}
