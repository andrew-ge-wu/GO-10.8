package example.captcha;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.app.policy.CheckboxPolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;

/**
 * Policy for captcha settings.
 */
public class CaptchaSettingsPolicy extends ContentPolicy
{
    public static final String CAPTCHA_SERVICE_SERVLET_CONTEXT_KEY = "capchaService";
    public static final String CAPTCHA_SETTINGS_EXTERNAL_ID = "example.CaptchaSettingsConfig";

    public static final String DEFAULT_SECRET_KEY = "HFfxScsQXUA=";
        
    private static final Logger LOG = Logger.getLogger(CaptchaSettingsPolicy.class.getName());
    
    public void preCommitSelf()
        throws CMException
    {
        if (DEFAULT_SECRET_KEY.equals(getSecretKey())) {
            try {
                String secretKey = DESCipher.generateSecretKey();
                setComponent("secretKey", "value", secretKey);
            } catch (CipherException e) {
                LOG.log(Level.WARNING, "Could not generate secret key for captcha, will use default.", e);
            }
        }
    }

    public String getSecretKey()
        throws CMException
    {
        return getComponentNotNull("secretKey", "value", DEFAULT_SECRET_KEY);
    }
    
    public boolean isEnabled()
        throws CMException
    {
        return ((CheckboxPolicy) getChildPolicy("enabled")).getChecked();
    }
    
}
