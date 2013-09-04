package example.membership;

import java.util.logging.Level;

import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;

public class MailSettingsPolicy extends ContentPolicy
{
    
    public String getSmtpServerHostname()
    {
        try {
            return ((SingleValuePolicy) getChildPolicy("smtpServerHostname")).getValue();
        } catch (CMException ex) {
            logger.log(Level.WARNING, "Failed to retrieve smtpServerHostname property.", ex);
        }

        return null;
    }

    public int getSmtpServerPort()
    {
        try {
            String value = ((SingleValuePolicy) getChildPolicy("smtpServerPort")).getValue();
            return Integer.parseInt(value);
        } catch (CMException ex) {
            logger.log(Level.WARNING, "Failed to retrieve smtpServerPort property.", ex);
        } catch (NumberFormatException ex) {
            logger.log(Level.WARNING, "Filed smtpServerPort is not an number.", ex);
        }

        return 25;
    }
    
    public String getSmtpServerUsername()
    {
        try {
            return ((SingleValuePolicy) getChildPolicy("smtpServerUsername")).getValue();
        } catch (CMException ex) {
            logger.log(Level.WARNING, "Failed to retrieve smtpServerUsername property.", ex);
        }

        return null;
    }
    
    public String getSmtpServerPassword()
    {
        try {
            return ((SingleValuePolicy) getChildPolicy("smtpServerPassword")).getValue();
        } catch (CMException ex) {
            logger.log(Level.WARNING, "Failed to retrieve smtpServerPassword property.", ex);
        }

        return null;
    }

    public int getSmtpSocketTimeout()
    {
        try {
            String timeoutStr = ((SingleValuePolicy) getChildPolicy("smtpServerSocketTimeout")).getValue();
            return Integer.parseInt(timeoutStr);
        } catch (CMException ex) {
            logger.log(Level.WARNING, "Failed to retrieve smtpSocketTimeout property.", ex);
        } catch (NumberFormatException ex) {
            logger.log(Level.WARNING, "Failed smtpSocketTimeout is not an number.", ex);
        }
        
        return 500;
    }
    
    public int getSmtpConnectionTimeout()
    {
        try {
            String timeoutStr = ((SingleValuePolicy) getChildPolicy("smtpServerConnectionTimeout")).getValue();
            return Integer.parseInt(timeoutStr);
        } catch (CMException ex) {
            logger.log(Level.WARNING, "Failed to retrieve smtpConnectionTimeout property.", ex);
        } catch (NumberFormatException ex) {
            logger.log(Level.WARNING, "Failed smtpConnectionTimeout is not an number.", ex);
        }
        
        return 500;
    }
}
