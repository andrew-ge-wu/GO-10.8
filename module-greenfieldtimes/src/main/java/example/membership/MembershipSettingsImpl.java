package example.membership;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.siteengine.resource.Resources;
import com.polopoly.siteengine.structure.SitePolicy;

/**
 * Reads membership settings.
 */
public class MembershipSettingsImpl implements MembershipSettings
{
    private final PolicyCMServer cmServer;

    /**
     * @param cmServer cm server to load settings from
     */
    public MembershipSettingsImpl(PolicyCMServer cmServer) {
        this.cmServer = cmServer;
    }
    
    private MembershipConfigurationPolicy getMembershipConfigurationPolicy(
            String siteIdString)
        throws CMException
    {
        ContentId id = ContentIdFactory.createContentId(siteIdString);
        ContentPolicy siteContent = (ContentPolicy) cmServer.getPolicy(id);

        return (MembershipConfigurationPolicy)
            siteContent.getChildPolicy("membershipConfiguration");

    }

    /**
     * Check if login is available on site.
     *
     * @param siteIdString site id of the site holding the settings
     */
    public boolean isLoginAllowed(String siteIdString)
        throws CMException
    {
        return getMembershipConfigurationPolicy(siteIdString).isLoginAllowed();
    }
    
    /**
     * Check if registration is available on site.
     *
     * @param siteIdString site id of the site holding the settings
     */
    public boolean isRegistrationAllowed(String siteIdString)
        throws CMException
    {
        return getMembershipConfigurationPolicy(siteIdString).isRegistrationAllowed();
    }

    public ResetPasswordMailService getResetPasswordMailService(String siteIdString)
        throws CMException
    {
        MembershipConfigurationPolicy membershipConfPolicy = getMembershipConfigurationPolicy(siteIdString);
        
        // Defaults to the empty string, rather than null
        String fromAddr = membershipConfPolicy.getMailSenderAddress();
        String fromName = membershipConfPolicy.getMailSender();
        
        if (null == fromAddr) {
            fromAddr = "";
        }
        
        if (null == fromName) {
            fromName = "";
        }
        
        boolean sendAsHtml = membershipConfPolicy.isHtmlMail();

        ContentId siteId = ContentIdFactory.createContentId(siteIdString);
        SitePolicy sitePolicy = (SitePolicy) cmServer.getPolicy(siteId);
        Resources resources = sitePolicy.getResources();
        
        String messageSubject = (String) resources.getResourceMap().get("resetpassword.subject");
        String messageContentPattern = (String) resources.getResourceMap().get("resetpassword.messagepattern");
        
        ContentId mailSettingsId = new ExternalContentId("example.MailSettingsConfig");
        MailSettingsPolicy mailSettingsPolicy = (MailSettingsPolicy) cmServer.getPolicy(mailSettingsId);

        String smtpHostname = mailSettingsPolicy.getSmtpServerHostname();
        int smtpPort = mailSettingsPolicy.getSmtpServerPort();
        String smtpUser = mailSettingsPolicy.getSmtpServerUsername();
        String smtpPassword = mailSettingsPolicy.getSmtpServerPassword();
       
        int socketTimeout = mailSettingsPolicy.getSmtpSocketTimeout();
        int connectionTimeout = mailSettingsPolicy.getSmtpConnectionTimeout();
        
        return new ResetPasswordMailService(smtpHostname,
                                            smtpPort,
                                            smtpUser,
                                            smtpPassword,
                                            fromAddr,
                                            fromName,
                                            messageSubject,
                                            messageContentPattern,
                                            sendAsHtml,
                                            socketTimeout,
                                            connectionTimeout);
    }
    
}
