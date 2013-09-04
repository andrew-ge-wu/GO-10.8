package example.membership;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpMethod;
import org.mockito.Mock;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.octo.captcha.service.CaptchaServiceException;
import com.polopoly.cm.client.CMException;
import com.polopoly.util.Base64;

import example.JettyWrapper;
import example.MockitoBase;
import example.captcha.ClusteredImageCaptchaService;
import example.membership.tools.SitePrefixUtil;

public class MembershipTestBase extends MockitoBase {

    static final String TARGET_URI = "http://gt.com/mypage";
    static final String REFERRER = "http://gt.com/1.1293";

    static final String SERVLET_URI = "/membership";
    static final String SERVLET_URI_MATCHES = SERVLET_URI + "/*";

    static final String SITE_ID = "2.111";
    static final String LOGINNAME = "x@gt.com";
    static final String ENCODED_LOGINNAME = "eEBndC5jb20=";
    static final String PASSWORD = "123456";
    static final String SCREENNAME = "Mr X";
    static final String REAL_LOGINNAME = new SitePrefixUtil().addPrefix(SITE_ID, LOGINNAME);

    Gson gson = new Gson();
    JettyWrapper jetty;
    MembershipServletBase membershipServlet;

    @Mock
    ResetPasswordMailService resetPasswordMailService;

    public class DummyMembershipSettings implements MembershipSettings
    {
        public boolean isLoginAllowed(String siteIdString) throws CMException {
            return SITE_ID.equals(siteIdString);
        }

        public boolean isRegistrationAllowed(String siteIdString) throws CMException {
            return SITE_ID.equals(siteIdString);
        }

        public ResetPasswordMailService getResetPasswordMailService(
                String siteIdString) throws CMException {
            return resetPasswordMailService;
        }
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        membershipServlet = new MembershipServletBase();
        membershipServlet.setMembershipSettings(new DummyMembershipSettings());

        jetty = new JettyWrapper();
        jetty.addServlet(membershipServlet, SERVLET_URI_MATCHES);
    }

    void assertFormErrorsCookie(Cookie[] cookies,
                                        Map<String,String> expectedErrors)
        throws Exception
    {
        assertCookie(cookies, expectedErrors, ActionRegister.ERROR_COOKIE_NAME);
    }

    void assertFormContentCookie(Cookie[] cookies,
            Map<String, String> expectedContent) throws Exception
    {
        assertCookie(cookies, expectedContent, ActionRegister.ECHO_COOKIE_NAME);
    }

    private void assertCookie(Cookie[] cookies,
            Map<String, String> expectedValues, String cookieName)
            throws IOException
    {
        JsonObject e = new JsonObject();
        for (Map.Entry<String, String> ee : expectedValues.entrySet()) {
            e.addProperty(ee.getKey(), ee.getValue());
        }
        String json = gson.toJson(e);

        String value = Base64.encodeBytes(json.getBytes(),
                Base64.DONT_BREAK_LINES);

        assertCookie(cookies, cookieName, value);
    }

    void assertCacheHeadersNoCache(HttpMethod method)
    {
        assertEquals("private, no-store, no-cache, must-revalidate, max-age=0, s-max-age=0, post-check=0, pre-check=0",
                method.getResponseHeader("Cache-Control").getValue());
        assertEquals("no-cache", method.getResponseHeader("Pragma").getValue());
        assertEquals("Thu, 01 Jan 1970 00:00:00 GMT", method.getResponseHeader("Expires").getValue());
    }

    void assertCookie(Cookie[] cookies, String name, String value)
    {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                assertEquals("cookie " + name + " incorrect value", value, cookie.getValue());
                return;
            }
        }

        fail("cookie " + name + " did not exist");
    }

    void assertDigestCookie(Cookie[] cookies, String name) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                assertEquals("Seems the digest values does not have correct length", 64, cookie.getValue().length());
            }
        }
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();

        jetty.stop();
    }

    class DummyCaptchaService implements ClusteredImageCaptchaService {

        public BufferedImage getImageChallenge(HttpServletResponse respose)
                throws CaptchaServiceException {
            return null;
        }

        public boolean validateAnswer(String answer,
                HttpServletRequest request, HttpServletResponse respose)
                throws CaptchaServiceException {
            return true;
        }

        public boolean isEnabled()
        {
            return true;
        }

        public void setEnabled(boolean enabled)
        {
        }
    }
}
