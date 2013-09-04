package example.util.mail;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

public class MailService
{

    private static final String SYSTEM_PROPERTY_MAIL_SMTP_CONNECTIONTIMEOUT =
        "mail.smtp.connectiontimeout";

    private static final String SYSTEM_PROPERTY_MAIL_SMTP_TIMEOUT =
        "mail.smtp.timeout";

    private static final Logger LOG = Logger.getLogger(MailService.class
            .getName());

    private static final int MIN_DOMAIN_LENGTH = 2;

    private final String hostName;

    private final String fromAddr;

    private final int port;

    private final String smtpUser;

    private final String smtpPasswd;

    private int soTimeout;

    private int connectionTimeout;

    public MailService(String hostName,
                       int port,
                       String fromAddr,
                       String smtpUser,
                       String smtpPasswd,
                       int soTimeout,
                       int connectionTimeout)
    {
        this.hostName = hostName;
        this.port = port;
        this.fromAddr = fromAddr;
        this.smtpUser = smtpUser;
        this.smtpPasswd = smtpPasswd;
        this.soTimeout = soTimeout;
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Verifies the syntax of an e-mail address.
     * 
     * @param email the email address to verify.
     * 
     * @return true if e-mail has valid syntax.
     */
    public static boolean isValidEmailAddress(String email)
    {
        if (email == null) {
            return false;
        }
        
        /* Check if @ is found */
        int indexOfAt = email.indexOf("@");
        if (!(indexOfAt > 0 && email.indexOf(" ") < 0 && email.lastIndexOf('.') < email.length() - 1)) {
            return false;
        }

        String legalNameCharacters = "abcdefghijklmnopqrstuvwxyz01234567890-+_.";
        String legalDomainCharacters = "abcdefghijklmnopqrstuvwxyz01234567890-.";
        email = email.toLowerCase();

        // take out x@
        String name = email.substring(0, indexOfAt);

        // check if name is valid
        if (name.length() <= 0) {
            return false;
        }

        String nonLitterals = "-+_.";
        int amountOfNonLitterals = 0;
        for (int i = 0; i < name.length(); i++) {
            if (legalNameCharacters.indexOf(name.substring(i, i + 1)) < 0) {
                return false;
            }
            if (nonLitterals.indexOf(name.substring(i, i + 1)) >= 0) {
                amountOfNonLitterals++;
            }
        }
        if (amountOfNonLitterals == name.length()) {
            return false;
        }

        int emailLength = email.length();
        if (!(emailLength > indexOfAt + 1)) {
            return false;
        }

        // take out @x
        String domain = email.substring(indexOfAt + 1, emailLength);
        // check if domain is valid
        if (!(domain.length() > MIN_DOMAIN_LENGTH + 1 && domain.indexOf(".") >= 0)) {
            return false;
        }

        for (int i = 0; i < domain.length(); i++) {
            if (legalDomainCharacters.indexOf(domain.substring(i, i + 1)) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Utility method to send mail.
     * 
     * @param to
     *            comma separated email addresses
     * @param subject
     *            email subject
     * @param content
     *            email content
     * @param useHtml
     *            send in html or as plain text
     * 
     * @return the id of the sent message
     * 
     * @throws EmailException
     *             if the mail could not be sent
     */
    public String send(String to, String subject, String content, boolean useHtml)
            throws example.util.mail.EmailException
    {
        String messageId = null;

        HtmlEmail email = new HtmlEmail();
        
        if (smtpUser != null && !"".equals(smtpUser.trim())) {
            email.setAuthentication(smtpUser, (smtpPasswd == null ? "" : smtpPasswd));
        }
        
        email.setHostName(hostName);
        email.setSmtpPort(port);
        try {
            email.addTo(to);
            email.setFrom(fromAddr);
            email.setSubject(subject);
            if (useHtml) {
                email.setHtmlMsg(content);
            } else {
                email.setTextMsg(content);
            }

            email.getMailSession().getProperties().setProperty(SYSTEM_PROPERTY_MAIL_SMTP_TIMEOUT,
                                                               String.valueOf(this.soTimeout));
            email.getMailSession().getProperties().setProperty(SYSTEM_PROPERTY_MAIL_SMTP_CONNECTIONTIMEOUT,
                                                               String.valueOf(this.connectionTimeout));
            
            messageId = email.send();
        } catch (EmailException e) {
            LOG.log(Level.WARNING, "Unable to send email", e);
            throw new example.util.mail.EmailException("Unable to send email",e);
        }
        return messageId;
    }
    
    
}
