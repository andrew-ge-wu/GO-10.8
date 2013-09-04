package example.membership;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;

import javax.ejb.FinderException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import junit.framework.AssertionFailedError;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.mockito.Mock;

import com.dumbster.smtp.SmtpMessage;
import com.polopoly.management.ServiceNotAvailableRuntimeException;
import com.polopoly.user.server.Caller;
import com.polopoly.user.server.User;

import example.DumbsterWrapper;
import example.util.mail.EmailException;

public class ResetPasswordTest extends MembershipTestBase {
    
    private static final String SMTP_HOSTNAME = "localhost";
    
    private static final String MAIL_MESSAGE_PATTERN = "Password:{0},From:{1}";

    private static final String MAIL_SUBJECT = "Reset mail";

    private static final String MAIL_FROM_NAME = "Test";

    private static final String MAIL_FROM_ADDR = "test@polopoly.com";

    private static final String RESET_PASSWORD_SERVLET_URI = SERVLET_URI
            + "/resetpassword";

    private static final String RANDOM_PASSWORD = "abc";

    @Mock
    UserHandler userHandler;

    @Mock
    User user;

    @Mock
    PasswordService passwordService;
   
    DumbsterWrapper dumbster;
    
    public void testResetPassword() throws Exception {
        
        try {
            dumbster = new DumbsterWrapper();
            dumbster.start();
            resetPasswordMailService = 
                new ResetPasswordMailService(SMTP_HOSTNAME,
                                             dumbster.getPort(),
                                             null,
                                             null,
                                             MAIL_FROM_ADDR,
                                             MAIL_FROM_NAME,
                                             MAIL_SUBJECT,
                                             MAIL_MESSAGE_PATTERN,
                                             false,
                                             100,
                                             100);
    
            when(userHandler.getUserByLoginName(REAL_LOGINNAME)).thenReturn(user);
            when(passwordService.generatePassword()).thenReturn(RANDOM_PASSWORD);
    
            membershipServlet.setUserHandler(userHandler);
            membershipServlet.setPasswordService(passwordService);
            membershipServlet.setCaptchaImageService(new DummyCaptchaService());
    
            jetty.start();
    
            HttpClient httpClient = new HttpClient();
            PostMethod method = new PostMethod(jetty
                    .getURL(RESET_PASSWORD_SERVLET_URI));
            method.addRequestHeader("Referer", REFERRER);
    
            method.addParameter(ActionResetPassword.PARAMETER_LOGIN_NAME,
                                LOGINNAME);
            method.addParameter(ActionResetPassword.PARAMETER_SITE, SITE_ID);
    
            int responseCode = httpClient.executeMethod(method);
    
            assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, responseCode);
    
            assertEquals(REFERRER + "?"
                    + ActionResetPassword.PARAMETER_RESET_SUCCESS + "="
                    + ActionResetPassword.SUCCESS_STATUS, method.getResponseHeader(
                    "Location").getValue());
    
            assertCacheHeadersNoCache(method);
    
            verify(user).setPassword(eq(RANDOM_PASSWORD), any(Caller.class));
            
            String message = MessageFormat.format(MAIL_MESSAGE_PATTERN, 
                                                  RANDOM_PASSWORD,
                                                  MAIL_FROM_NAME);
            assertMailSent(MAIL_FROM_ADDR, LOGINNAME, MAIL_SUBJECT, message);
        } finally {
            if (dumbster != null) {
                dumbster.stop();
            }
        }
    }
    
    private void assertMailSent(String from, String to, String subject, String body) {
        
        if (dumbster != null) {
            for(SmtpMessage email : dumbster.getReceivedEmail()) {
                try {
                    assertTrue(email.getHeaderValue("From").indexOf(from) != -1);
                    assertTrue(email.getHeaderValue("To").indexOf(to)  != -1);
                    assertTrue(email.getHeaderValue("Subject").indexOf(subject)  != -1);
                    assertTrue(email.getBody().indexOf(body) != -1);
                } catch (AssertionFailedError e) {
                    fail("No email send from: " + from + " to: " + to
                            + " matching subject: " + subject
                            + " with body: " + body);
                }
            }
        }
    }

    public void testNoReferrer() throws Exception {
        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty
                .getURL(RESET_PASSWORD_SERVLET_URI));

        int responseCode = httpClient.executeMethod(method);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseCode);
    }

    public void testEmptyLoginName() throws Exception {

        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty.getURL(RESET_PASSWORD_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);

        method.addParameter(ActionResetPassword.PARAMETER_LOGIN_NAME, "");
        method.addParameter(ActionResetPassword.PARAMETER_SITE, SITE_ID);

        int responseCode = httpClient.executeMethod(method);

        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, responseCode);

        assertEquals(REFERRER + "?" + ActionResetPassword.PARAMETER_RESET_ERROR
                + "=" + ActionResetPassword.ERROR_STATUS_UNKNOWN_USER, method
                .getResponseHeader("Location").getValue());

        assertCacheHeadersNoCache(method);
    }
    
    public void testUnknownUser() throws Exception {

        when(userHandler.getUserByLoginName(REAL_LOGINNAME)).thenThrow(new FinderException(""));
        
        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty.getURL(RESET_PASSWORD_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);

        method.addParameter(ActionResetPassword.PARAMETER_LOGIN_NAME, "");
        method.addParameter(ActionResetPassword.PARAMETER_SITE, SITE_ID);

        int responseCode = httpClient.executeMethod(method);

        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, responseCode);

        assertEquals(REFERRER + "?" + ActionResetPassword.PARAMETER_RESET_ERROR
                + "=" + ActionResetPassword.ERROR_STATUS_UNKNOWN_USER, method
                .getResponseHeader("Location").getValue());

        assertCacheHeadersNoCache(method);
    }    
    
    public void testEmailException() throws Exception {

        when(userHandler.getUserByLoginName(REAL_LOGINNAME)).thenReturn(user);
        when(passwordService.generatePassword()).thenReturn(RANDOM_PASSWORD);
        
        doThrow(new EmailException("")).
            when(resetPasswordMailService).send(eq(LOGINNAME), any(String.class));
        
        checkStatusAfterException(ActionChangePassword.ERROR_STATUS_SERVICE_DOWN);     
    }

    public void testServletException() throws Exception {
        
        doThrow(new ServletException("")).when(userHandler).getUserByLoginName(REAL_LOGINNAME);
        
        checkStatusAfterException(ActionChangePassword.ERROR_STATUS_SERVICE_DOWN);
    }

    public void testServiceNotAvailableRuntimeException() throws Exception {
        
        doThrow(new ServiceNotAvailableRuntimeException("")).when(userHandler).getUserByLoginName(REAL_LOGINNAME);
        
        checkStatusAfterException(ActionChangePassword.ERROR_STATUS_SERVICE_DOWN);
    }

    private void checkStatusAfterException(String errorStatus) throws Exception {

        membershipServlet.setUserHandler(userHandler);
        membershipServlet.setPasswordService(passwordService);
        membershipServlet.setCaptchaImageService(new DummyCaptchaService());

        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty.getURL(RESET_PASSWORD_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);

        method.addParameter(ActionResetPassword.PARAMETER_LOGIN_NAME,
                            LOGINNAME);
        method.addParameter(ActionResetPassword.PARAMETER_SITE, SITE_ID);

        int responseCode = httpClient.executeMethod(method);

        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, responseCode);

        assertEquals(REFERRER + "?" + ActionResetPassword.PARAMETER_RESET_ERROR
                + "=" + errorStatus, method.getResponseHeader("Location")
                .getValue());

        assertCacheHeadersNoCache(method);
    }

}
