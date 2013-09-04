package example.captcha;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.octo.captcha.service.CaptchaServiceException;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.util.LRUCache;

/**
 * The cookie HTTP image captcha service does not rely on storing the id/answer
 * locally, the generated challenge answer is ciphered and stored in a cookie to
 * be deciphered and validated. The rule is that a challenge can only be
 * validated once (burn-key after use), this to avoid spam.
 */
public class CookieImageCaptchaService
    implements ClusteredImageCaptchaService
{
    private static final Logger LOG = Logger.getLogger(CookieImageCaptchaService.class.getName());

    private static final int MAX_KEYS_IN_CAPTCHA_STORE = 1000;

    private final LRUCache store;
    private final CookieImageCaptchaFactory factory;
    
    private final AtomicBoolean enabled = new AtomicBoolean(false);
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    
    private final AtomicReference<DESCipher> cipher = new AtomicReference<DESCipher>();

    private final PolicyCMServer policyCmServer;
    
    public CookieImageCaptchaService(CookieImageCaptchaFactory factory,
                                                                PolicyCMServer policyCMServer)
    {
        this.factory = factory;
        this.policyCmServer = policyCMServer;
        this.store = new LRUCache(MAX_KEYS_IN_CAPTCHA_STORE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * example.captcha.ClusteredImageCaptchaService#getImageChallenge(javax.
     * servlet.http.HttpServletResponse)
     */
    public BufferedImage getImageChallenge(HttpServletResponse response)
            throws CaptchaServiceException
    {
        if (!isInitialized.get()) {
            initialize();
        }
        
        try {
            CookieImageCaptcha captcha = factory.getImageCaptcha();
            Cookie captchaCookie = captcha.getCaptchaCookie();
            response.addCookie(captchaCookie);
            
            return captcha.getImageChallenge();
        } catch (CipherException e) {
            throw new CaptchaServiceException(
                    "Failed to create encrypt challenge answer", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * example.captcha.ClusteredImageCaptchaService#validateAnswer(java.lang
     * .String, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    public boolean validateAnswer(String answer,
                                                          HttpServletRequest request,
                                                          HttpServletResponse response)
        throws CaptchaServiceException
    {
        if (!isInitialized.get()) {
            initialize();
        }
        
        boolean valid = false;

        if (answer != null && store.get(answer) == null) {
            CookieImageCaptcha captcha = new CookieImageCaptcha(cipher.get());
            valid = captcha.validateResponse(answer, request);
            
            if (valid) {
                // Burn the answer.
                store.put(answer, Boolean.TRUE);
                Cookie answerCookie = captcha.getCaptchaCookie(request);
                
                LOG.fine("Captcha answer '" + answer + "' was valid, clearing cookie.");
                
                answerCookie.setMaxAge(0);
                response.addCookie(answerCookie);
            }
            else {
                LOG.fine("Captcha answer '" + answer + "' was not valid.");
            }
        }
        else {
            LOG.fine("Captcha answer '" + answer + "' was already used on this front.");
        }
        
        return valid;
    }
    
    private synchronized void initialize()
    {
        if (isInitialized.get()) {
            return;
        }
        
        ExternalContentId captchaSettingsEid = new ExternalContentId(
                CaptchaSettingsPolicy.CAPTCHA_SETTINGS_EXTERNAL_ID);
        
        try {
            CaptchaSettingsPolicy captchaSettings = (CaptchaSettingsPolicy)
                policyCmServer.getPolicy(captchaSettingsEid);
            
            // Create cipher
            String secretKey = captchaSettings.getSecretKey();
            DESCipher desCipher = new DESCipher(secretKey);
            
            cipher.set(desCipher);
            factory.setCipher(desCipher);
            
            setEnabled(captchaSettings.isEnabled());
            isInitialized.set(true);
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Cannot initialize captcha service. Will use the default key instead.", e);
        } catch (CipherException ce) {
            LOG.log(Level.WARNING, "Cannot initialize captcha service. Will use the default key instead.", ce);
        } finally {
            if (!isInitialized.get()) {
                // Try to use the default key.
                DESCipher desCipher;
                
                try {
                    desCipher = new DESCipher(CaptchaSettingsPolicy.DEFAULT_SECRET_KEY);
                    
                    cipher.set(desCipher);
                    factory.setCipher(desCipher);
                    
                    setEnabled(true);
                    isInitialized.set(true);
                } catch (CipherException e) {
                    LOG.log(Level.WARNING, "Cannot initialize captcha service.", e);
                }
            }
        }
    }
    
    void setEnabled(boolean enabled)
    {
        this.enabled.set(enabled);
    }
    
    public boolean isEnabled()
    {
        if (!isInitialized.get()) {
            initialize();
        }
        
        return this.enabled.get();
    }
}
