package example.membership;

import java.util.logging.Level;

import com.polopoly.cm.app.policy.CheckboxPolicy;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.model.ModelTypeDescription;

import example.content.ContentBasePolicy;

/**
 * GT-version policy for the field "p.siteengine.Site.MembershipConfiguration".
 */
public class MembershipConfigurationPolicy
        extends ContentBasePolicy
        implements ModelTypeDescription
{

    /**
     * Returns the value of the "isRegistrationAllowed" subfield.
     *
     * @return true if registration is allowed, else false
     */
    public boolean isRegistrationAllowed()
    {
        try {
            return ((CheckboxPolicy) getChildPolicy("allowRegistration")).getChecked();
        } catch (CMException ex) {
            logger.log(Level.WARNING, "Failed to retrieve allowRegistration property.", ex);
        }

        return false;
    }

    /**
     * Returns the value of the "isLoginAllowed" subfield.
     *
     * @return true if login is allowed, else false
     */
    public boolean isLoginAllowed()
    {
        try {
            return ((CheckboxPolicy) getChildPolicy("allowLogin")).getChecked();
        } catch (CMException ex) {
            logger.log(Level.WARNING, "Failed to retrieve allowLogin property.", ex);
        }

        return false;
    }

    /**
     * Returns the value of the "mailSender" subfield.
     *
     * @return the mail sender address, or <code>null</code>
     */
    public String getMailSender()
    {
        try {
            return ((SingleValuePolicy) getChildPolicy("mailSender")).getValue();
        } catch (CMException ex) {
            logger.log(Level.WARNING, "Failed to retrieve mailSender property.", ex);
        }
        
        return null;
    }
    
    /**
     * Returns the value of the "mailSenderAddress" subfield.
     *
     * @return the mail sender address, or <code>null</code>
     */
    public String getMailSenderAddress()
    {
        try {
            return ((SingleValuePolicy) getChildPolicy("mailSenderAddress")).getValue();
        } catch (CMException ex) {
            logger.log(Level.WARNING, "Failed to retrieve mailSenderAddress property.", ex);
        }
        
        return null;
    }
    
    /**
     * Returns the value of the "htmlMail" subfield.
     *
     * @return true if to send mail as HTML, else false (plain text)
     */
    public boolean isHtmlMail()
    {
        try {
            return ((CheckboxPolicy) getChildPolicy("htmlMail")).getChecked();
        } catch (CMException ex) {
            logger.log(Level.WARNING, "Failed to retrieve htmlMail property.", ex);
        }
        
        return false;
    }
    
}