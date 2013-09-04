package example.captcha;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

public class CookieImageCaptchaFactoryTest extends TestCase {

    private CookieImageCaptchaFactory toTest;
    
    private String secretKey;
    
    private Cipher cipher;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        secretKey = DESCipher.generateSecretKey();
        cipher = new DESCipher(secretKey);
        toTest = new DefaultCaptchaFactory();
        toTest.setCipher(cipher);
    }
    
    public void testGetAndValidateCaptcha() throws Exception {

        // Test to get captcha
        CookieImageCaptcha captcha = toTest.getImageCaptcha();
        Cookie captchaCookie = captcha.getCaptchaCookie();
        assertNotNull("Expected to get challenge image", captcha.getImageChallenge());
        assertNotNull("Expected to get image cookie", captchaCookie);
        
        String answer = cipher.decrypt(captchaCookie.getValue());    
        
        captcha = new CookieImageCaptcha(cipher);
        HttpServletRequest request = getMockedRequest(captchaCookie);
        boolean valid = captcha.validateResponse(answer, request);
        assertTrue("Expected challenge answer to be valid", valid);
        verify(request, atLeastOnce()).getCookies();
    }
 
    private HttpServletRequest getMockedRequest(Cookie captchaCookie) {
        
        Cookie[] cookies = new Cookie[] { captchaCookie };
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(cookies);
        return request;
    }
}
