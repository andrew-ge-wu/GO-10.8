package example.util.mail;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Utility class to send e-mails.
 */
public abstract class MailUtil
{
    private static final String CLASS = MailUtil.class.getName();
    private static final Logger LOG = Logger.getLogger(CLASS);

    public static final String FORMAT_PLAIN_TEXT = "text/plain";
    public static final String FORMAT_HTML_TEXT = "text/html";

    private static final int MIN_DOMAIN_LENGTH = 2;

    /**
     * Private constructor to prevent creation.
     */
    private MailUtil()
    {
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
        LOG.entering(CLASS, "isValidEmailAddress", email);

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

        LOG.exiting(CLASS, "isValidEmailAddress");
        return true;
    }
    
    /**
     * Utility method to send mail.
     * 
     * @param toAddress comma separated email addresses
     * @param fromName name of the sender
     * @param fromAddress email address of the sender
     * @param subject email subject
     * @param message email message
     * @param smtpHost valid smtp host
     * @param charset character set of the mail like iso-8859-1 or utf-8
     * @param format "text/plain" or "text/html"
     * @throws EmailException if the mail could not be sent
     */
    public static void sendMail(String toAddress,
                                String fromName,
                                String fromAddress,
                                String subject,
                                String message,
                                String smtpHost,
                                String charset,
                                String format)
        throws EmailException
    {
        final String METHOD = "sendMail(String toAddress, String fromName, String fromAddress," + " String subject, String message," + " String smtpHost, String charset, String format)";

        Properties mailProperties = System.getProperties();
        mailProperties.put("mail.smtp.host", smtpHost);

        // Get session
        javax.mail.Session mailSession = javax.mail.Session.getInstance(mailProperties, null);

        try {
            // Define message
            MimeMessage mimeMessage = new MimeMessage(mailSession);
            mimeMessage.setFrom(new InternetAddress(fromAddress, fromName));
            //mimeMessage.setReplyTo(arg0);
            mimeMessage.setSubject(subject, charset);
            InternetAddress[] internetAddresses = {new InternetAddress (fromAddress)};
            mimeMessage.setReplyTo(internetAddresses);

            // Add recipients
            mimeMessage.setRecipient(RecipientType.TO, new InternetAddress(toAddress));

            // Construct the message
            MimeMultipart mimeMultipart = new MimeMultipart("alternative");
            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setContent(message, format);
            mimeMultipart.addBodyPart(bodyPart);

            mimeMessage.setContent(mimeMultipart);

            // Send message
            Transport.send(mimeMessage);
        } catch (AddressException e) {
            String msg = "Invalid email addresses";
            LOG.logp(Level.WARNING, CLASS, METHOD, msg, e);
            throw new EmailException(msg, e);
        } catch (UnsupportedEncodingException e) {
            String msg = "Invalid encoding";
            LOG.logp(Level.WARNING, CLASS, METHOD, msg, e);
            throw new EmailException(msg, e);
        } catch (MessagingException e) {
            String msg = "Failed to send message to " + toAddress + " from " + fromAddress + " by using SMTPHost " + smtpHost;
            LOG.logp(Level.WARNING, CLASS, METHOD, msg, e);
            throw new EmailException(msg, e);
        }
    }

}
