package example.membership;

import java.text.MessageFormat;

import example.util.mail.EmailException;
import example.util.mail.MailService;

public class ResetPasswordMailService extends MailService {

    private final String messageSubject;

    private final String messagePattern;

    private final boolean useHtml;

    private final String fromName;

    public ResetPasswordMailService(String hostName,
                                    int port,
                                    String smtpUser,
                                    String smtpPasswd,
                                    String fromAddr,
                                    String fromName,
                                    String messageSubject,
                                    String messagePattern,
                                    boolean sendAsHtml,
                                    int soTimeout,
                                    int connectionTimeout)
    {
        super(hostName, port, fromAddr, smtpUser, smtpPasswd, soTimeout, connectionTimeout);
        this.fromName = fromName;
        this.messageSubject = messageSubject;
        this.messagePattern = messagePattern;
        useHtml = sendAsHtml;
    }

    public String send(String to, String password) throws EmailException
    {
        return super.send(to, messageSubject, MessageFormat.format(messagePattern,
                password, fromName), useHtml);
    }

}
