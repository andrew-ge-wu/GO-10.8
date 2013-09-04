package example.membership;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.polopoly.cm.VersionedContentId;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.PostMethod;
import org.mockito.Mock;

import com.google.gson.JsonObject;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.ContentOperationFailedException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.paywall.Capability;
import com.polopoly.paywall.ContentBundle;
import com.polopoly.paywall.PaywallPolicy;
import com.polopoly.paywall.cookie.OnlineAccessCookie;
import com.polopoly.paywall.cookie.OnlineAccessDigestCookie;
import com.polopoly.siteengine.membership.UserDataManager;
import com.polopoly.siteengine.membership.UserDataManager.ServiceId;
import com.polopoly.siteengine.membership.UserDataManager.ServiceInfo;
import com.polopoly.siteengine.membership.UserDataManager.ServiceState;
import com.polopoly.user.server.AuthenticationFailureException;
import com.polopoly.user.server.Caller;
import com.polopoly.user.server.User;
import com.polopoly.user.server.UserId;

import example.membership.mynewslist.MyNewsListData;
import example.membership.tools.Base64Util;
import example.paywall.PremiumUserDataPolicy;
import example.util.CSRFUtil;

public class LoginTest extends MembershipTestBase
{
    private static final String SESSION_KEY = "\"SQ+PKQuzMmkvjg/AR9k8d0YSC/cDyxvxbKoDjZHTxBbCOYUpEHjty9sQAbEFkw\"";

    private static final String BUNDLE_ID_STR = "1.123";
    private static final String SERVICE_DATA_COOKIE_OLD_VALUE = "OLD_VALUE";
    private static final String LOGIN_SERVLET_URI = SERVLET_URI + "/login";
    private static final String LOGOUT_SERVLET_URI = SERVLET_URI + "/logout";
    private static final String SCREEN_NAME = "MyScreenName";
    private Set<ContentBundle> contentBundles;

    @Mock
    ContentBundle contentBundle;

    @Mock
    UserHandler userHandler;

    @Mock
    User user;

    @Mock
    UserId userId;

    @Mock
    ContentPolicy userData;

    @Mock
    PremiumUserDataPolicy userDataPolicy;

    @Mock
    SingleValuePolicy screenNamePolicy;

    @Mock
    UserDataHandler userDataHandler;

    @Mock
    UserDataManager userDataManager;

    @Mock
    ServiceDataCookieHandler serviceDataCookieHandler;

    @Mock
    PolicyCMServer cmServer;

    @Mock
    PaywallPolicy paywallPolicy;

    @Mock
    Policy policy;

    @Mock
    Caller caller;

    @Mock
    Capability capability;

    @Mock
    OnlineAccessCookie onlineAccessCookie;

    @Mock
    OnlineAccessDigestCookie onlineAccessDigestCookie;

    @Mock
    private ContentId contentId;

    JsonObject cookieMapObject;

    private String sessionKey;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        contentBundles = new HashSet<ContentBundle>(Arrays.asList(contentBundle));
        cookieMapObject = new JsonObject();

        sessionKey = "A secret session key";
        Caller caller = new Caller(new UserId("98"), sessionKey);
        when(cmServer.getCurrentCaller()).thenReturn(caller);
        when(contentId.getContentIdString()).thenReturn(BUNDLE_ID_STR);
        when(contentBundle.getContentId()).thenReturn(new VersionedContentId(1, 123, 12345));
    }

    private void addValidLoginParameters(PostMethod method)
        throws Exception
    {
        method.addParameter(ActionLogin.PARAMETER_LOGIN_NAME, LOGINNAME);
        method.addParameter(ActionLogin.PARAMETER_PASSWORD, PASSWORD);
        method.addParameter(ActionLogin.PARAMETER_SITE, SITE_ID);
    }

    public void testLoginWithJustReferer()
        throws Exception
    {
        membershipServlet.setUserHandler(userHandler);
        membershipServlet.setUserDataManager(userDataManager);
        membershipServlet.setServiceDataCookieHandler(serviceDataCookieHandler);
        membershipServlet.setUserDataHandler(userDataHandler);
        membershipServlet.setPolicyCMServer(cmServer);

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
        PostMethod method = new PostMethod(jetty.getURL(LOGIN_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);
        addValidLoginParameters(method);

        int responseCode = httpClient.executeMethod(method);

        assertEquals(302, responseCode);
        assertCookie(httpClient.getState().getCookies(), "loginName", ENCODED_LOGINNAME);
        assertCookie(httpClient.getState().getCookies(), "p_onlineaccess", BUNDLE_ID_STR);
        assertDigestCookie(httpClient.getState().getCookies(), "p_onlineaccess_digest");

        assertEquals(REFERRER, method.getResponseHeader("Location").getValue());

        assertCacheHeadersNoCache(method);

        verify(userHandler).login(any(HttpServletRequest.class),
                any(HttpServletResponse.class), eq(REAL_LOGINNAME), eq(PASSWORD));
    }


    public void testLoadServicesNoPreviousCookie()
        throws Exception
    {
        MyNewsListData bean = new MyNewsListData();
        bean.setData("[\"data\"]");

        membershipServlet.setServiceDataCookieHandler(serviceDataCookieHandler);
        membershipServlet.setUserHandler(userHandler);
        membershipServlet.setUserDataManager(userDataManager);
        membershipServlet.setUserDataHandler(userDataHandler);
        membershipServlet.setPolicyCMServer(cmServer);

        when(userDataHandler.getUserData(userId)).thenReturn(userDataPolicy);

        when(userDataPolicy.getChildPolicy(ActionLogin.CHILD_POLICY_SCREEN_NAME)).thenReturn(screenNamePolicy);
        when(screenNamePolicy.getValue()).thenReturn(SCREEN_NAME);

        when(userHandler.login(any(HttpServletRequest.class),
                               any(HttpServletResponse.class),
                               any(String.class), any(String.class))).thenReturn(user);

        when(serviceDataCookieHandler.getCookieData(any(HttpServletRequest.class))).thenReturn(new JsonObject());

        when(user.getUserId()).thenReturn(userId);

        ServiceId serviceId = new ServiceId("mnl", "7.132");
        ServiceInfo[] s = new ServiceInfo[] { new ServiceInfo(serviceId, ServiceState.ENABLED) };

        when(userDataManager.getServiceInfos(userId)).thenReturn(s);
        when(userDataManager.getOrCreateServiceBean(userId, serviceId)).thenReturn(bean);

        when(PaywallPolicy.getPaywallPolicy(cmServer)).thenReturn(paywallPolicy);

        when(paywallPolicy.getOnlineAccessCapability(cmServer)).thenReturn(capability);

        when(userDataPolicy.getAccessibleContentBundlesByCapability(capability)).thenReturn(contentBundles);

        when(userHandler.getSessionKeyIfPresent(any(HttpServletRequest.class))).thenReturn(SESSION_KEY);


        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty.getURL(LOGIN_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);
        addValidLoginParameters(method);

        int responseCode = httpClient.executeMethod(method);

        verify(userDataManager).getServiceInfos(userId);

        assertEquals(302, responseCode);
        assertCookie(httpClient.getState().getCookies(), "loginName", ENCODED_LOGINNAME);
        assertCookie(httpClient.getState().getCookies(), "p_onlineaccess", BUNDLE_ID_STR);
        assertDigestCookie(httpClient.getState().getCookies(), "p_onlineaccess_digest");
        assertEquals(REFERRER, method.getResponseHeader("Location").getValue());

        assertCacheHeadersNoCache(method);

        verify(userHandler).login(any(HttpServletRequest.class),
                any(HttpServletResponse.class), eq(REAL_LOGINNAME), eq(PASSWORD));

        verify(userDataManager).getOrCreateServiceBean(userId, serviceId);
        assertCookie(httpClient.getState().getCookies(),
                     ActionPersistUserData.COOKIE_NAME_DATA,
                     new Base64Util().encode("{\"mnl\":{\"7.132\":[\"data\"]}}"));
    }

    public void testDataCookieRemovedUponLogin()
        throws Exception
    {
        membershipServlet.setServiceDataCookieHandler(serviceDataCookieHandler);
        membershipServlet.setUserHandler(userHandler);
        membershipServlet.setUserDataManager(userDataManager);
        membershipServlet.setUserDataHandler(userDataHandler);
        membershipServlet.setPolicyCMServer(cmServer);

        when(userDataHandler.getUserData(userId)).thenReturn(userDataPolicy);
        when(userDataPolicy.getChildPolicy(ActionLogin.CHILD_POLICY_SCREEN_NAME)).thenReturn(screenNamePolicy);
        when(screenNamePolicy.getValue()).thenReturn(SCREEN_NAME);

        when(userDataManager.getServiceInfos(userId)).thenReturn(new ServiceInfo[0]);

        when(userHandler.login(any(HttpServletRequest.class),
                               any(HttpServletResponse.class),
                               any(String.class), any(String.class))).thenReturn(user);

        when(user.getUserId()).thenReturn(userId);

        when(PaywallPolicy.getPaywallPolicy(cmServer)).thenReturn(paywallPolicy);

        when(userDataPolicy.getAccessibleContentBundlesByCapability(capability)).thenReturn(contentBundles);

        when(userHandler.getSessionKeyIfPresent(any(HttpServletRequest.class))).thenReturn(SESSION_KEY);


        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty.getURL(LOGIN_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);
        method.addRequestHeader("Cookie", ActionPersistUserData.COOKIE_NAME_DATA
                                          + "=" + SERVICE_DATA_COOKIE_OLD_VALUE);

        addValidLoginParameters(method);

        HttpState state = httpClient.getState();
        state.addCookie(new Cookie("/",
                                   ActionPersistUserData.COOKIE_NAME_DATA,
                                   SERVICE_DATA_COOKIE_OLD_VALUE));

        int responseCode = httpClient.executeMethod(method);

        assertEquals(302, responseCode);
        assertEquals(REFERRER, method.getResponseHeader("Location").getValue());

        Header[] responseHeaders = method.getResponseHeaders("Set-Cookie");

        boolean dataCookieIsCorrect = false;

        for (Header header : responseHeaders) {
            if (header.getValue().startsWith("data")) {
                String value = header.getValue();
                dataCookieIsCorrect =
                    value.indexOf("Expires=Thu, 01 Jan 1970 00:00:00 GMT") != -1;
            }
        }

        assertTrue(dataCookieIsCorrect);
    }

    public void testLoginWithNoReferer()
        throws Exception
    {
        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty.getURL(LOGIN_SERVLET_URI));
        addValidLoginParameters(method);

        int responseCode = httpClient.executeMethod(method);

        assertEquals(400, responseCode);
    }

    public void testLoginWithTargetUrl()
        throws Exception
    {
        membershipServlet.setUserHandler(userHandler);
        membershipServlet.setUserDataManager(userDataManager);
        membershipServlet.setServiceDataCookieHandler(serviceDataCookieHandler);
        membershipServlet.setUserDataHandler(userDataHandler);
        membershipServlet.setPolicyCMServer(cmServer);

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
        PostMethod method = new PostMethod(jetty.getURL(LOGIN_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);
        method.addParameter(ActionLogin.PARAMETER_TARGET_URI, TARGET_URI);
        addValidLoginParameters(method);

        int responseCode = httpClient.executeMethod(method);

        assertEquals(302, responseCode);
        assertCookie(httpClient.getState().getCookies(), "loginName", ENCODED_LOGINNAME);
        assertCookie(httpClient.getState().getCookies(), "p_onlineaccess", BUNDLE_ID_STR);
        assertDigestCookie(httpClient.getState().getCookies(), "p_onlineaccess_digest");
        assertEquals(TARGET_URI, method.getResponseHeader("Location").getValue());

        assertCacheHeadersNoCache(method);

        verify(userHandler).login(any(HttpServletRequest.class),
                any(HttpServletResponse.class), eq(REAL_LOGINNAME), eq(PASSWORD));
    }

    public void testNoPasswordLoginWithTargetUrl()
        throws Exception
    {
        membershipServlet.setUserHandler(userHandler);

        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty.getURL(LOGIN_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);
        method.addParameter(ActionLogin.PARAMETER_TARGET_URI, TARGET_URI);
        method.addParameter(ActionLogin.PARAMETER_LOGIN_NAME, LOGINNAME);
        method.addParameter(ActionLogin.PARAMETER_SITE, SITE_ID);

        int responseCode = httpClient.executeMethod(method);

        assertEquals(302, responseCode);
        assertEquals(REFERRER + "?" + ActionLogin.PARAMETER_FORM_ERROR
                     + "=" + ActionLogin.ERROR_STATUS_AUTHENTICATION_FAILED,
                     method.getResponseHeader("Location").getValue());

        assertCacheHeadersNoCache(method);
    }

    public void testFailedLogin()
        throws Exception
    {
        doThrow(new AuthenticationFailureException(""))
            .when(userHandler)
            .login(any(HttpServletRequest.class),
                   any(HttpServletResponse.class),
                   eq(REAL_LOGINNAME),
                   eq(PASSWORD));

        membershipServlet.setUserHandler(userHandler);

        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty.getURL(LOGIN_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);
        method.addParameter(ActionLogin.PARAMETER_TARGET_URI, TARGET_URI);
        addValidLoginParameters(method);

        int responseCode = httpClient.executeMethod(method);

        assertEquals(302, responseCode);
        assertEquals(REFERRER + "?" + ActionLogin.PARAMETER_FORM_ERROR
                     + "=" + ActionLogin.ERROR_STATUS_AUTHENTICATION_FAILED,
                     method.getResponseHeader("Location").getValue());

        assertCacheHeadersNoCache(method);
    }

    public void testLogout()
        throws Exception
    {
        membershipServlet.setUserHandler(userHandler);
        membershipServlet.setPolicyCMServer(cmServer);

        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty.getURL(LOGOUT_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);
        method.setFollowRedirects(false);
        method.addParameter(CSRFUtil.CSRF_PARAMETER_NAME,
                            sessionKey);

        int responseCode = httpClient.executeMethod(method);

        assertEquals(302, responseCode);
        assertEquals(jetty.getURL("/"), method.getResponseHeader("Location").getValue());
        assertCacheHeadersNoCache(method);

        verify(userHandler).logout(any(HttpServletRequest.class),
               any(HttpServletResponse.class));
    }

    public void testLoginMissingUserData() throws Exception {
        membershipServlet.setUserHandler(userHandler);
        membershipServlet.setUserDataHandler(userDataHandler);

        when(userHandler.login(any(HttpServletRequest.class),
                               any(HttpServletResponse.class),
                               any(String.class),
                               any(String.class))).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);
        when(userDataHandler.getUserData(userId)).thenThrow(new ContentOperationFailedException("does-not-exist"));

        jetty.start();

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(jetty.getURL(LOGIN_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);
        method.addParameter(ActionLogin.PARAMETER_TARGET_URI, TARGET_URI);
        addValidLoginParameters(method);

        int responseCode = httpClient.executeMethod(method);

        assertEquals(302, responseCode);
        assertEquals(REFERRER + "?" + ActionLogin.PARAMETER_FORM_ERROR
                     + "=" + ActionLogin.ERROR_STATUS_USER_DATA_NOT_FOUND,
                     method.getResponseHeader("Location").getValue());

        assertCacheHeadersNoCache(method);
    }
}
