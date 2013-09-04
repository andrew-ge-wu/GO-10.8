package example.membership;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.polopoly.cm.VersionedContentId;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.mockito.Mock;

import com.google.gson.JsonObject;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.management.ServiceNotAvailableRuntimeException;
import com.polopoly.paywall.Capability;
import com.polopoly.paywall.ContentBundle;
import com.polopoly.paywall.PaywallPolicy;
import com.polopoly.siteengine.membership.UserDataManager;
import com.polopoly.siteengine.membership.UserDataManager.ServiceInfo;
import com.polopoly.user.server.InvalidLoginNameException;
import com.polopoly.user.server.InvalidPasswordException;
import com.polopoly.user.server.NotUniqueException;
import com.polopoly.user.server.User;
import com.polopoly.user.server.UserId;

import example.paywall.PremiumUserDataPolicy;

public class RegistrationTest extends MembershipTestBase
{
    private static final String REGISTER_SERVLET_URI = SERVLET_URI + "/register";

    private static final String SCREEN_NAME = "MyScreenName";
    private static final String SESSION_KEY = "\"SQ+PKQuzMmkvjg/AR9k8d0YSC/cDyxvxbKoDjZHTxBbCOYUpEHjty9sQAbEFkw\"";
    private static final String BUNDLE_ID_STR = "1.123";
    private Set<ContentBundle> contentBundles;

    @Mock
    ContentBundle contentBundle;

    @Mock
    UserHandler userHandler;

    @Mock
    RegistrationFormHandler formHandler;

    @Mock
    UserDataHandler userDataHandler;

    @Mock
    PolicyCMServer cmServer;

    @Mock
    UserDataManager userDataManager;

    @Mock
    ServiceDataCookieHandler serviceDataCookieHandler;

    @Mock
    PremiumUserDataPolicy userDataPolicy;

    @Mock
    SingleValuePolicy screenNamePolicy;

    @Mock
    PaywallPolicy paywallPolicy;

    @Mock
    Capability capability;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        contentBundles = new HashSet<ContentBundle>(Arrays.asList(contentBundle));
        when(contentBundle.getContentId()).thenReturn(new VersionedContentId(1, 123, 12345));
    }

    public void testRegisterNotUniqueLoginName()
        throws Exception
    {
        testRegisterUserServerError(new NotUniqueException(""),
                                    ActionRegister.PARAMETER_LOGIN_NAME,
                                    RegistrationFormHandler.ERROR_KEY_NOT_UNIQUE);
    }

    public void testRegisterInvalidLoginName()
        throws Exception
    {
        testRegisterUserServerError(new InvalidLoginNameException(""),
                                    ActionRegister.PARAMETER_LOGIN_NAME,
                                    RegistrationFormHandler.ERROR_INVALID_EMAIL);
    }

    public void testRegisterInvalidPassword()
        throws Exception
    {
        testRegisterUserServerError(new InvalidPasswordException(""),
                                    ActionRegister.PARAMETER_PASSWORD,
                                    RegistrationFormHandler.ERROR_INVALID_PASSWORD);
    }

    public void testRegisterServiceNotAvailable()
        throws Exception
    {
        when(userDataHandler.getFormHandler(SITE_ID)).thenReturn(formHandler);

        when(formHandler.validateFormData(any(HttpServletRequest.class))).thenReturn(new HashMap<String, String>());

        when(formHandler.getEchoRequestData(any(HttpServletRequest.class))).thenReturn(new HashMap<String, String>());

        doThrow(new ServiceNotAvailableRuntimeException())
            .when(userHandler)
            .register(any(HttpServletRequest.class),
                      any(HttpServletResponse.class),
                      eq(REAL_LOGINNAME),
                      eq(PASSWORD));

        membershipServlet.setUserDataHandler(userDataHandler);
        membershipServlet.setUserHandler(userHandler);
        membershipServlet.setCaptchaImageService(new DummyCaptchaService());

        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty.getURL(REGISTER_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);
        method.addParameter(ActionRegister.PARAMETER_LOGIN_NAME, LOGINNAME);
        method.addParameter(ActionRegister.PARAMETER_PASSWORD, PASSWORD);
        method.addParameter(ActionRegister.PARAMETER_SCREEN_NAME, SCREENNAME);
        method.addParameter(ActionRegister.PARAMETER_SITE, SITE_ID);

        int responseCode = httpClient.executeMethod(method);

        assertEquals(302, responseCode);
        assertEquals(REFERRER + "?" + ActionRegister.PARAMETER_FORM_ERROR
                     + "=" + ActionRegister.ERROR_STATUS_SERVICE_DOWN,
                     method.getResponseHeader("Location").getValue());
        assertCacheHeadersNoCache(method);
    }

    public void testRegister()
        throws Exception
    {
        when(userDataHandler.getFormHandler(SITE_ID)).thenReturn(formHandler);

        when(formHandler.validateFormData(any(HttpServletRequest.class))).thenReturn(new HashMap<String, String>());

        when(formHandler.getEchoRequestData(any(HttpServletRequest.class))).thenReturn(new HashMap<String, String>());

        doThrow(new ServiceNotAvailableRuntimeException())
          .when(userHandler)
          .login(any(HttpServletRequest.class),
                 any(HttpServletResponse.class),
                 eq(REAL_LOGINNAME),
                 eq(PASSWORD));

        User user = mock(User.class);
        UserId userId = mock(UserId.class);
        when(user.getUserId()).thenReturn(userId);

        when(userHandler.register(any(HttpServletRequest.class),
               any(HttpServletResponse.class),
               eq(REAL_LOGINNAME),
               eq(PASSWORD))).thenReturn(user);

        ContentPolicy userData = mock(ContentPolicy.class);

        when(userDataHandler.createUserData(userId)).thenReturn(userData);

        membershipServlet.setUserHandler(userHandler);
        membershipServlet.setUserDataManager(userDataManager);
        membershipServlet.setServiceDataCookieHandler(serviceDataCookieHandler);
        membershipServlet.setUserDataHandler(userDataHandler);
        membershipServlet.setPolicyCMServer(cmServer);
        membershipServlet.setCaptchaImageService(new DummyCaptchaService());

//        login
        when(userDataHandler.getUserData(userId)).thenReturn(userDataPolicy);

        when(userDataPolicy.getChildPolicy(ActionLogin.CHILD_POLICY_SCREEN_NAME)).thenReturn(screenNamePolicy);
        when(screenNamePolicy.getValue()).thenReturn(SCREEN_NAME);

        when(userHandler.login(any(HttpServletRequest.class),
                               any(HttpServletResponse.class),
                               any(String.class), any(String.class))).thenReturn(user);

        when(serviceDataCookieHandler.getCookieData(any(HttpServletRequest.class))).thenReturn(new JsonObject());

        when(user.getUserId()).thenReturn(userId);
        when(userDataManager.getServiceInfos(userId)).thenReturn(new ServiceInfo[]{});

        when(PaywallPolicy.getPaywallPolicy(cmServer)).thenReturn(paywallPolicy);

        when(paywallPolicy.getOnlineAccessCapability(cmServer)).thenReturn(capability);

        when(userDataPolicy.getAccessibleContentBundlesByCapability(capability)).thenReturn(contentBundles);

        when(userHandler.getSessionKeyIfPresent(any(HttpServletRequest.class))).thenReturn(SESSION_KEY);


        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty.getURL(REGISTER_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);
        method.addParameter(ActionRegister.PARAMETER_LOGIN_NAME, LOGINNAME);
        method.addParameter(ActionRegister.PARAMETER_PASSWORD, PASSWORD);
        method.addParameter(ActionRegister.PARAMETER_SCREEN_NAME, SCREENNAME);
        method.addParameter(ActionRegister.PARAMETER_SITE, SITE_ID);

        int responseCode = httpClient.executeMethod(method);

        assertEquals(302, responseCode);

        assertCookie(httpClient.getState().getCookies(), "loginName", ENCODED_LOGINNAME);
        assertCookie(httpClient.getState().getCookies(), "p_onlineaccess", BUNDLE_ID_STR);
        assertDigestCookie(httpClient.getState().getCookies(), "p_onlineaccess_digest");
        assertEquals(REFERRER + "?" + ActionRegister.PARAMETER_FORM_SUCCESS + "=" + ActionRegister.SUCCESS_STATUS, method.getResponseHeader("Location").getValue());

        assertCacheHeadersNoCache(method);

        verify(user, times(3)).getUserId();
        verify(formHandler).writeFormData(eq(userData), any(HttpServletRequest.class));
        verify(userDataHandler).commitUserData(any(ContentPolicy.class));
    }

    public void testRegisterCoreFormErrorEmptyFields()
        throws Exception
    {
        Map<String, String> requestParameters = new HashMap<String, String>();

        requestParameters.put(ActionRegister.PARAMETER_LOGIN_NAME, "");
        requestParameters.put(ActionRegister.PARAMETER_PASSWORD, "");
        requestParameters.put(ActionRegister.PARAMETER_SCREEN_NAME, SCREENNAME);

        Map<String, String> expectedErrors = new HashMap<String, String>();

        expectedErrors.put(ActionRegister.PARAMETER_LOGIN_NAME,
                           RegistrationFormHandler.ERROR_EMPTY_FIELD);

        expectedErrors.put(ActionRegister.PARAMETER_PASSWORD,
                           RegistrationFormHandler.ERROR_EMPTY_FIELD);

        testRegisterCoreValidationError(requestParameters, expectedErrors);
    }

    public void testRegisterCustomFormErrorEmptyFields()
        throws Exception
    {
       Map<String, String> expectedErrors = new HashMap<String, String>();

       expectedErrors.put(ActionRegister.PARAMETER_SCREEN_NAME,
                          RegistrationFormHandler.ERROR_EMPTY_FIELD);

       testRegisterCustomValidationError(expectedErrors);
    }

    public void testRegisterCoreFormErrorInvalidLoginName()
        throws Exception
    {
        Map<String, String> requestParameters = new HashMap<String, String>();

        requestParameters.put(ActionRegister.PARAMETER_LOGIN_NAME, "my***loginname");
        requestParameters.put(ActionRegister.PARAMETER_PASSWORD, PASSWORD);
        requestParameters.put(ActionRegister.PARAMETER_SCREEN_NAME, SCREENNAME);

        Map<String, String> expectedErrors = new HashMap<String, String>();

        expectedErrors.put(ActionRegister.PARAMETER_LOGIN_NAME,
                           RegistrationFormHandler.ERROR_INVALID_EMAIL);

        testRegisterCoreValidationError(requestParameters, expectedErrors);
    }

    public void testRegisterCoreFormErrorPasswordTooShort()
        throws Exception
    {
        Map<String, String> requestParameters = new HashMap<String, String>();

        requestParameters.put(ActionRegister.PARAMETER_LOGIN_NAME, LOGINNAME);
        requestParameters.put(ActionRegister.PARAMETER_PASSWORD, "a");
        requestParameters.put(ActionRegister.PARAMETER_SCREEN_NAME, SCREENNAME);

        Map<String, String> expectedErrors = new HashMap<String, String>();

        expectedErrors.put(ActionRegister.PARAMETER_PASSWORD,
                           RegistrationFormHandler.ERROR_PASSWORD_TOO_SHORT);

        testRegisterCoreValidationError(requestParameters, expectedErrors);
    }

    public void testRegisterCustomFormErrorBadScreenName()
        throws Exception
    {
        Map<String, String> expectedErrors = new HashMap<String, String>();

        expectedErrors.put(ActionRegister.PARAMETER_SCREEN_NAME,
                           RegistrationFormHandler.ERROR_BAD_SCREENNAME);

        testRegisterCustomValidationError(expectedErrors);
    }

    public void testRegisterDisabled()
        throws Exception
    {
        MembershipSettings membershipSettings = mock(MembershipSettings.class);

        when(membershipSettings.isRegistrationAllowed(SITE_ID)).thenReturn(false);

        membershipServlet.setMembershipSettings(membershipSettings);

        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty.getURL(REGISTER_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);
        method.addParameter(ActionRegister.PARAMETER_LOGIN_NAME, LOGINNAME);
        method.addParameter(ActionRegister.PARAMETER_PASSWORD, PASSWORD);
        method.addParameter(ActionRegister.PARAMETER_SCREEN_NAME, SCREENNAME);
        method.addParameter(ActionRegister.PARAMETER_SITE, SITE_ID);

        int responseCode = httpClient.executeMethod(method);

        assertEquals(302, responseCode);
        assertEquals(REFERRER + "?" + ActionRegister.PARAMETER_FORM_ERROR
                     + "=" + ActionRegister.ERROR_STATUS_SERVICE_DOWN,
                     method.getResponseHeader("Location").getValue());
        assertCacheHeadersNoCache(method);
    }

    public void testRegisterNoReferer()
        throws Exception
    {
        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty.getURL(REGISTER_SERVLET_URI));
        method.addParameter(ActionRegister.PARAMETER_LOGIN_NAME, LOGINNAME);
        method.addParameter(ActionRegister.PARAMETER_PASSWORD, PASSWORD);
        method.addParameter(ActionRegister.PARAMETER_SCREEN_NAME, SCREENNAME);
        method.addParameter(ActionRegister.PARAMETER_SITE, SITE_ID);

        int responseCode = httpClient.executeMethod(method);

        assertEquals(400, responseCode);
    }

    public void testRegistrationEchoDataWithNonEmailLoginName()
        throws Exception
    {
        Map<String, String> expectedMap = new HashMap<String, String>();
        Map<String, String> returnFromFormHandlerMap = new HashMap<String, String>();
        Map<String, String> postParameters = new HashMap<String, String>();

        postParameters.put(ActionRegister.PARAMETER_LOGIN_NAME, "mmm");
        postParameters.put(ActionRegister.PARAMETER_PASSWORD, "something");
        postParameters.put(ActionRegister.PARAMETER_SCREEN_NAME, "nick");

        expectedMap.put(ActionRegister.PARAMETER_LOGIN_NAME, "mmm");
        expectedMap.put(ActionRegister.PARAMETER_SCREEN_NAME, "nick");

        returnFromFormHandlerMap.put(ActionRegister.PARAMETER_SCREEN_NAME, "nick");

        when(userDataHandler.getFormHandler(SITE_ID)).thenReturn(formHandler);

        when(formHandler.validateFormData(any(HttpServletRequest.class))).thenReturn(new HashMap<String, String>());

        when(formHandler.getEchoRequestData(any(HttpServletRequest.class))).thenReturn(returnFromFormHandlerMap);

        membershipServlet.setUserDataHandler(userDataHandler);
        membershipServlet.setUserHandler(userHandler);
        membershipServlet.setCaptchaImageService(new DummyCaptchaService());

        jetty.start();

        HttpClient httpClient = new HttpClient();

        PostMethod method = new PostMethod(jetty.getURL(REGISTER_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);

        for (String key : postParameters.keySet()) {
            method.addParameter(key, postParameters.get(key));
        }

        method.addParameter(ActionRegister.PARAMETER_SITE, SITE_ID);

        int responseCode = httpClient.executeMethod(method);


        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, responseCode);

        assertEquals(REFERRER,
                     method.getResponseHeader("Location").getValue());

        assertCacheHeadersNoCache(method);

        Cookie[] cookies = httpClient.getState().getCookies();

        assertFormContentCookie(cookies, expectedMap);
     }

    public void testRegistrationEchoDataWithBadScreenName()
            throws Exception
    {
        Map<String, String> expectedMap = new HashMap<String, String>();
        Map<String, String> returnFromFormHandlerMap = new HashMap<String, String>();
        Map<String, String> postParameters = new HashMap<String, String>();
        Map<String, String> formErrorMap = new HashMap<String, String>();

        final String badScreenName = "nick##*%$";

        postParameters.put(ActionRegister.PARAMETER_LOGIN_NAME, "polopoly@polopoly.com");
        postParameters.put(ActionRegister.PARAMETER_PASSWORD, "something");
        postParameters.put(ActionRegister.PARAMETER_SCREEN_NAME, badScreenName);

        expectedMap.put(ActionRegister.PARAMETER_LOGIN_NAME, "polopoly@polopoly.com");
        expectedMap.put(ActionRegister.PARAMETER_SCREEN_NAME, badScreenName);

        formErrorMap.put(ActionRegister.PARAMETER_SCREEN_NAME,
                RegistrationFormHandler.ERROR_BAD_SCREENNAME);

        returnFromFormHandlerMap.put(ActionRegister.PARAMETER_SCREEN_NAME, badScreenName);

        when(userDataHandler.getFormHandler(SITE_ID)).thenReturn(formHandler);

        when(formHandler.validateFormData(any(HttpServletRequest.class))).thenReturn(formErrorMap);

        when(formHandler.getEchoRequestData(any(HttpServletRequest.class))).thenReturn(returnFromFormHandlerMap);

        membershipServlet.setUserDataHandler(userDataHandler);
        membershipServlet.setUserHandler(userHandler);
        membershipServlet.setCaptchaImageService(new DummyCaptchaService());

        jetty.start();

        HttpClient httpClient = new HttpClient();

        PostMethod method = new PostMethod(jetty.getURL(REGISTER_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);

        for (String key : postParameters.keySet())
        {
            method.addParameter(key, postParameters.get(key));
        }

        method.addParameter(ActionRegister.PARAMETER_SITE, SITE_ID);

        int responseCode = httpClient.executeMethod(method);

        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, responseCode);

        assertEquals(REFERRER, method.getResponseHeader("Location")
                .getValue());

        assertCacheHeadersNoCache(method);

        Cookie[] cookies = httpClient.getState().getCookies();

        assertFormContentCookie(cookies, expectedMap);
    }


    private void testRegisterUserServerError(final Exception exception,
                                            final String parameterKey,
                                            final String errorMessageKey)
        throws Exception
    {
        when(userDataHandler.getFormHandler(SITE_ID)).thenReturn(formHandler);

        when(formHandler.validateFormData(any(HttpServletRequest.class))).thenReturn(new HashMap<String, String>());

        when(formHandler.getEchoRequestData(any(HttpServletRequest.class))).thenReturn(new HashMap<String, String>());

        doThrow(exception).when(userHandler).
            register(any(HttpServletRequest.class),
                    any(HttpServletResponse.class),
                    eq(REAL_LOGINNAME),
                    eq(PASSWORD));

        membershipServlet.setUserDataHandler(userDataHandler);
        membershipServlet.setUserHandler(userHandler);
        membershipServlet.setCaptchaImageService(new DummyCaptchaService());

        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty.getURL(REGISTER_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);
        method.addParameter(ActionRegister.PARAMETER_LOGIN_NAME, LOGINNAME);
        method.addParameter(ActionRegister.PARAMETER_PASSWORD, PASSWORD);
        method.addParameter(ActionRegister.PARAMETER_SCREEN_NAME, SCREENNAME);
        method.addParameter(ActionRegister.PARAMETER_SITE, SITE_ID);

        int responseCode = httpClient.executeMethod(method);

        Map<String, String> expectedErrors = new HashMap<String, String>();
        expectedErrors.put(parameterKey, errorMessageKey);

        assertFormErrorsCookie(httpClient.getState().getCookies(), expectedErrors);
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, responseCode);

        assertEquals(REFERRER,
                     method.getResponseHeader("Location").getValue());

        assertCacheHeadersNoCache(method);
    }

    public void testRegisterCoreValidationError(Map<String, String> parameters,
                                                Map<String, String> expectedErrors)
        throws Exception
    {
        when(userDataHandler.getFormHandler(SITE_ID)).thenReturn(formHandler);

        when(formHandler.validateFormData(any(HttpServletRequest.class))).thenReturn(new HashMap<String, String>());

        when(formHandler.getEchoRequestData(any(HttpServletRequest.class))).thenReturn(new HashMap<String, String>());

        membershipServlet.setUserDataHandler(userDataHandler);
        membershipServlet.setUserHandler(userHandler);
        membershipServlet.setCaptchaImageService(new DummyCaptchaService());

        jetty.start();

        HttpClient httpClient = new HttpClient();

        PostMethod method = new PostMethod(jetty.getURL(REGISTER_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);

        for (String key : parameters.keySet()) {
            method.addParameter(key, parameters.get(key));
        }

        method.addParameter(ActionRegister.PARAMETER_SITE, SITE_ID);

        int responseCode = httpClient.executeMethod(method);


        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, responseCode);

        assertEquals(REFERRER,
                     method.getResponseHeader("Location").getValue());

        assertCacheHeadersNoCache(method);

        Cookie[] cookies = httpClient.getState().getCookies();
        assertFormErrorsCookie(cookies, expectedErrors);
    }

    public void testRegisterCustomValidationError(Map<String, String> expectedErrors)
        throws Exception
    {
        when(userDataHandler.getFormHandler(SITE_ID)).thenReturn(formHandler);

        when(formHandler.validateFormData(any(HttpServletRequest.class))).thenReturn(expectedErrors);

        when(formHandler.getEchoRequestData(any(HttpServletRequest.class))).thenReturn(new HashMap<String, String>());

        membershipServlet.setUserDataHandler(userDataHandler);
        membershipServlet.setUserHandler(userHandler);
        membershipServlet.setCaptchaImageService(new DummyCaptchaService());

        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty.getURL(REGISTER_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);
        method.addParameter(ActionRegister.PARAMETER_LOGIN_NAME, LOGINNAME);
        method.addParameter(ActionRegister.PARAMETER_PASSWORD, PASSWORD);
        method.addParameter(ActionRegister.PARAMETER_SCREEN_NAME, SCREENNAME);
        method.addParameter(ActionRegister.PARAMETER_SITE, SITE_ID);

        int responseCode = httpClient.executeMethod(method);

        assertFormErrorsCookie(httpClient.getState().getCookies(), expectedErrors);
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, responseCode);

        assertEquals(REFERRER,
                     method.getResponseHeader("Location").getValue());

        assertCacheHeadersNoCache(method);
    }
}
