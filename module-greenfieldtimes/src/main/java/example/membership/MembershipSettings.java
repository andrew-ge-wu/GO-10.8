package example.membership;

import com.polopoly.cm.client.CMException;

public interface MembershipSettings {
    
    /**
     * Is login allowed or not.
     *
     * @param siteIdString site id of the site where login is enabled or not
     * @throws CMException if the site settings aren't available
     */
    public boolean isLoginAllowed(String siteIdString) throws CMException;
    
    /**
     * Is registration allowed or not.
     *
     * @param siteIdString site id of the site where login is enabled or not
     * @throws CMException if the site settings aren't available
     */
    public boolean isRegistrationAllowed(String siteIdString) throws CMException;
    
    /**
     * Get the mail service to send reset password mails.
     * 
     * @param siteIdString site id of the site to get reset password mail service from
     * @return The reset password mail service
     * @throws CMException if the site settings aren't available
     */
    public ResetPasswordMailService getResetPasswordMailService(
            String siteIdString) throws CMException;
    
}


