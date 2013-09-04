package example.membership;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.rmi.RemoteException;

import javax.ejb.ObjectNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.mockito.Mock;

import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.user.server.AuthenticationFailureException;
import com.polopoly.user.server.Caller;
import com.polopoly.user.server.InvalidPasswordException;
import com.polopoly.user.server.PermissionDeniedException;
import com.polopoly.user.server.User;
import com.polopoly.user.server.UserId;

import example.util.CSRFUtil;

public class ChangePasswordTest extends MembershipTestBase {
    private static final String CHANGE_PASSWORD_SERVLET_URI = SERVLET_URI
            + "/changepassword";

    private static final String NEW_PASSWORD = "MyNewPassword";

    @Mock
    UserHandler userHandler;

    @Mock
    User user;

    @Mock
    PolicyCMServer cmServer;

    private String sessionKey;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        sessionKey = "A secret session key";
        Caller caller = new Caller(new UserId("98"), sessionKey);
        when(cmServer.getCurrentCaller()).thenReturn(caller);
        membershipServlet.setPolicyCMServer(cmServer);

    }

    public void testChangePassword() throws Exception
    {
        when(userHandler.getLoggedInUser(any(HttpServletRequest.class),
                                         any(HttpServletResponse.class))).thenReturn(user);

        membershipServlet.setUserHandler(userHandler);
        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty.getURL(CHANGE_PASSWORD_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);

        method.addParameter(ActionChangePassword.PARAMETER_CURRENT_PASSWORD,
                            PASSWORD);
        method.addParameter(ActionChangePassword.PARAMETER_NEW_PASSWORD,
                            NEW_PASSWORD);

        method.addParameter(CSRFUtil.CSRF_PARAMETER_NAME,
                            sessionKey);

        int responseCode = httpClient.executeMethod(method);

        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, responseCode);

        assertEquals(REFERRER + "?" + ActionChangePassword.PARAMETER_CHANGE_SUCCESS
                     + "=" + ActionChangePassword.SUCCESS_STATUS,
                     method.getResponseHeader("Location").getValue());

        assertCacheHeadersNoCache(method);

        verify(userHandler).changePassword(user, PASSWORD, NEW_PASSWORD);
    }

    public void testNoReferrer() throws Exception {
        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty
                .getURL(CHANGE_PASSWORD_SERVLET_URI));

        method.addParameter(CSRFUtil.CSRF_PARAMETER_NAME,
                            sessionKey);


        int responseCode = httpClient.executeMethod(method);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseCode);
    }

    public void testEmptyPassword() throws Exception {
        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty
                .getURL(CHANGE_PASSWORD_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);

        method.addParameter(ActionChangePassword.PARAMETER_CURRENT_PASSWORD,
                            "");
        method.addParameter(ActionChangePassword.PARAMETER_NEW_PASSWORD,
                NEW_PASSWORD);

        method.addParameter(CSRFUtil.CSRF_PARAMETER_NAME,
                            sessionKey);

        int responseCode = httpClient.executeMethod(method);

        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, responseCode);

        assertEquals(REFERRER + "?"
                + ActionChangePassword.PARAMETER_CHANGE_ERROR + "="
                + ActionChangePassword.ERROR_STATUS_AUTHENTICATION_FAILED,
                method.getResponseHeader("Location").getValue());

        assertCacheHeadersNoCache(method);
    }

    public void testNotLoggedIn() throws Exception {

        when(userHandler.getLoggedInUser(any(HttpServletRequest.class),
                any(HttpServletResponse.class))).thenReturn(null);

        membershipServlet.setUserHandler(userHandler);

        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty
                .getURL(CHANGE_PASSWORD_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);

        method.addParameter(ActionChangePassword.PARAMETER_CURRENT_PASSWORD,
                PASSWORD);
        method.addParameter(ActionChangePassword.PARAMETER_NEW_PASSWORD,
                NEW_PASSWORD);

        method.addParameter(CSRFUtil.CSRF_PARAMETER_NAME,
                            sessionKey);

        int responseCode = httpClient.executeMethod(method);

        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, responseCode);

        assertEquals(REFERRER + "?"
                + ActionChangePassword.PARAMETER_CHANGE_ERROR + "="
                + ActionChangePassword.ERROR_STATUS_USER_NOT_LOGGED_IN, method
                .getResponseHeader("Location").getValue());

        assertCacheHeadersNoCache(method);
    }

    public void testUserNotFound() throws Exception {
        checkStatusAfterException(new ObjectNotFoundException(""),
                ActionChangePassword.ERROR_STATUS_AUTHENTICATION_FAILED);
    }

    public void testWrongPassword() throws Exception {
        checkStatusAfterException(new AuthenticationFailureException(""),
                ActionChangePassword.ERROR_STATUS_AUTHENTICATION_FAILED);
    }

    public void testInvalidNewPassword() throws Exception {
        checkStatusAfterException(new InvalidPasswordException(""),
                ActionChangePassword.ERROR_STATUS_INVALID_NEW_PASSWORD);
    }

    public void testPermissionDenied() throws Exception {
        checkStatusAfterException(new PermissionDeniedException(""),
                ActionChangePassword.ERROR_STATUS_SERVICE_DOWN);
    }

    public void testRemoteException() throws Exception {
        checkStatusAfterException(new RemoteException(""),
                ActionChangePassword.ERROR_STATUS_SERVICE_DOWN);
    }

    private void checkStatusAfterException(Throwable exception,
            String errorStatus) throws Exception {

        when(userHandler.getLoggedInUser(any(HttpServletRequest.class),
                any(HttpServletResponse.class))).thenReturn(user);

        doThrow(exception).when(userHandler).changePassword(user, PASSWORD,
                NEW_PASSWORD);

        membershipServlet.setUserHandler(userHandler);

        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty
                .getURL(CHANGE_PASSWORD_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);

        method.addParameter(ActionChangePassword.PARAMETER_CURRENT_PASSWORD,
                PASSWORD);
        method.addParameter(ActionChangePassword.PARAMETER_NEW_PASSWORD,
                NEW_PASSWORD);

        method.addParameter(CSRFUtil.CSRF_PARAMETER_NAME,
                            sessionKey);

        int responseCode = httpClient.executeMethod(method);

        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, responseCode);

        assertEquals(REFERRER + "?"
                + ActionChangePassword.PARAMETER_CHANGE_ERROR + "="
                + errorStatus, method.getResponseHeader("Location").getValue());

        assertCacheHeadersNoCache(method);
    }
}
