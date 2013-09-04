package example.captcha;

import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.octo.captcha.CaptchaException;

/**
 * A cookie based implementation of a clustered image captcha. A generated
 * challenge has a corresponding ciphered cookie containing the answer.
 */
public class CookieImageCaptcha implements ClusteredImageCaptcha {

    private static final Logger LOG = Logger.getLogger(CookieImageCaptcha.class.getName());

    private static final String ANSWER_COOKIE_NAME = "captcha";
    private static final int ANSWER_COOKIE_MAX_AGE = 10 * 60;
    private static final String ANSWER_COOKIE_PATH = "/";

    protected BufferedImage challenge;

    private String response;

    private final Cipher cipher;

    public CookieImageCaptcha(Cipher cipher) {
        this.cipher = cipher;
    }

    public CookieImageCaptcha(BufferedImage challenge,
                                                    String response,
                                                    Cipher cipher)
    {
        this(cipher);
        
        this.challenge = challenge;
        this.response = response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see example.captcha.ClusteredImageCaptcha#getImageChallenge()
     */
    public BufferedImage getImageChallenge()
        throws CaptchaException
    {
        return challenge;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * example.captcha.ClusteredImageCaptcha#validateResponse(java.lang.String,
     * javax.servlet.http.HttpServletRequest)
     */
    public boolean validateResponse(String responseString,
                                    HttpServletRequest request)
        throws CaptchaException
    {
        String answer = null;
        
        try {
            answer = getCipheredCookieAnswer(request);
            LOG.fine("Valid string from cookie: '" + answer + "'");
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to decrypt captcha cookie", e);
        }
        
        return (answer != null && answer.equals(responseString)) ? true : false;
    }

    /**
     * Returns a ciphered cookie containing the answer for this challenge.
     * 
     * @return Cookie the cookie, or <code>null</code> if no challenge has been generated.
     * 
     * @throws CipherException if failed to cipher the cookie value
     */
    public Cookie getCaptchaCookie()
        throws CipherException
    {
        String cipheredAnswer = cipher.encrypt(response);
        Cookie cookie = new Cookie(ANSWER_COOKIE_NAME, cipheredAnswer);
        cookie.setMaxAge(ANSWER_COOKIE_MAX_AGE);
        cookie.setPath(ANSWER_COOKIE_PATH);
        
        return cookie;
    }

    /**
     * Returns the ciphered cookie from the request.
     * 
     * @param request the HTTP request to get the cookie from
     * 
     * @return The ciphered cookie, or <code>null</code> if none exist.
     */
    public Cookie getCaptchaCookie(HttpServletRequest request)
    {
        Cookie captchaCookie = null;
        Cookie[] cookies = request.getCookies();
        
        for (int i = 0; cookies != null && i < cookies.length; i++) {
            String name = cookies[i].getName();
            if (name.equals(ANSWER_COOKIE_NAME)) {
                captchaCookie = cookies[i];
                break;
            }
        }
        
        return captchaCookie;
    }

    private String getCipheredCookieAnswer(HttpServletRequest request)
        throws Exception
    {
        String answer = null;
        
        Cookie captchaCookie = getCaptchaCookie(request);
        if (captchaCookie != null) {
            String cipheredAnswer = captchaCookie.getValue();
            LOG.fine("Chipered valid string from cookie: '" + cipheredAnswer + "'");
            answer = cipher.decrypt(cipheredAnswer);
        }
        else {
            LOG.fine("Captcha cookie was null.");
        }
        
        return answer;
    }
    
}
