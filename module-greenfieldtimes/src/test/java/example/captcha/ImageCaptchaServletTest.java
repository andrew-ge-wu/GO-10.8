/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example.captcha;

import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.mockito.Mock;

import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;

import example.JettyWrapper;
import example.MockitoBase;

public class ImageCaptchaServletTest 
    extends MockitoBase
{
    public static final String VERIFY_KEY_SERVLET_URI = "/captcha";
    public static final String VERIFY_KEY_SERVLET_MAPPING = VERIFY_KEY_SERVLET_URI + "/*";
    public static final String VALIDATE_SERVLET_URI = "/validate";
    public static final String VALIDATE_SERVLET_MAPPING =  VALIDATE_SERVLET_URI + "/*";

    private JettyWrapper jetty;
    private HttpServlet imageCaptchaServlet;
    private HttpServlet validatorServlet;
    private static String KEY = null;
    private Cipher cipher;
    
    @Mock
    PolicyCMServer policyCmServer;
    
    @Mock
    CaptchaSettingsPolicy captchaSettingsPolicy;

    @Override
    protected void setUp() 
        throws Exception
    {
        super.setUp();

        if (null == KEY) {            
            KEY = DESCipher.generateSecretKey();
        }

        cipher = new DESCipher(KEY);
        jetty = new JettyWrapper();

        imageCaptchaServlet = new ImageCaptchaServlet();
        jetty.addServlet(imageCaptchaServlet, VERIFY_KEY_SERVLET_MAPPING);
        validatorServlet = new ValidatorServlet();
        jetty.addServlet(validatorServlet, VALIDATE_SERVLET_MAPPING);
        
        jetty.getContext().addEventListener(new TestServletContextListener());

        jetty.start();
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        try {
            if (jetty != null) {
                jetty.stop();
                jetty = null;
            }
        } finally {
            super.tearDown();
        }
    }

    public void testDESCiphersShareSecretKey()
        throws Exception
    {
        String key = DESCipher.generateSecretKey();
        
        Cipher c1 = new DESCipher(key);
        Cipher c2 = new DESCipher(key);

        String msg = "Hoppla!";
        
        assertEquals(msg, c2.decrypt(c1.encrypt(msg)));
    }

    public void testValidationRequest()
        throws Exception
    {
        HttpClient httpClient = new HttpClient();
        HttpMethod method = new GetMethod(jetty.getURL(VERIFY_KEY_SERVLET_URI));
        httpClient.executeMethod(method);

        Cookie[] cookies = httpClient.getState().getCookies();
        
        assertEquals("Got wrong cookie count", 1, cookies.length);

        sendToValidatingServlet(cipher.decrypt(cookies[0].getValue()),cookies[0]);
    }

    private void sendToValidatingServlet(String answer, Cookie captchaCookie)
        throws Exception
    {
        HttpState initialState = new HttpState();
        initialState.addCookie(captchaCookie);

        HttpClient httpClient = new HttpClient();
        httpClient.setState(initialState);
        httpClient.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        HttpMethod method = new GetMethod(jetty.getURL(VALIDATE_SERVLET_URI + "?" + "answer=" + answer));

        httpClient.executeMethod(method);

        assertEquals("true", method.getResponseBodyAsString());
    }

    @SuppressWarnings("serial")
    private class ValidatorServlet
        extends HttpServlet
    {
        private ClusteredImageCaptchaService captchaService;

        @Override
        public void init(ServletConfig config)
            throws ServletException
        {
            super.init(config);
            
            captchaService = (ClusteredImageCaptchaService)
                config.getServletContext().getAttribute(CaptchaSettingsPolicy.CAPTCHA_SERVICE_SERVLET_CONTEXT_KEY);
        }
        
        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
        {
            boolean result =
                captchaService.validateAnswer(request.getParameter("answer"), request, response);

            response.getWriter().write(String.valueOf(result));
        }
    }
    
    private class TestServletContextListener
        implements javax.servlet.ServletContextListener
    {
        public void contextDestroyed(ServletContextEvent arg0)
        {
            // Ignore
        }

        public void contextInitialized(ServletContextEvent sce)
        {
            ClusteredImageCaptchaService captchaService = null;
            
            ExternalContentId captchaSettingsEid = new ExternalContentId(
                    CaptchaSettingsPolicy.CAPTCHA_SETTINGS_EXTERNAL_ID);
            
            try {
                when(policyCmServer.getPolicy(captchaSettingsEid)).thenReturn(captchaSettingsPolicy);
                when(captchaSettingsPolicy.getSecretKey()).thenReturn(KEY);
                when(captchaSettingsPolicy.isEnabled()).thenReturn(true);

                captchaService = new CookieImageCaptchaService(new DefaultCaptchaFactory(), policyCmServer);
                sce.getServletContext().setAttribute(CaptchaSettingsPolicy.CAPTCHA_SERVICE_SERVLET_CONTEXT_KEY, captchaService);
            } catch (CMException cme) {
                // Won't happen
            }
        }
        
    }
    
}
