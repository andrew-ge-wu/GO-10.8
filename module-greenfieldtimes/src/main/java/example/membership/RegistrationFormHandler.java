package example.membership;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;

/**
 * An interface for classes that can validate a site registration form and write
 * data from a successful registration to the site user data content.
 */
public interface RegistrationFormHandler
{
    public static final String ERROR_BAD_SCREENNAME = "er5";
    public static final String ERROR_EMPTY_SCREENNAME = "er0";
    public static final String ERROR_EMPTY_FIELD = "er0";
    public static final String ERROR_INVALID_EMAIL = "er1";
    public static final String ERROR_PASSWORD_TOO_SHORT = "er3";
    public static final String ERROR_KEY_NOT_UNIQUE = "er2";
    public static final String ERROR_INVALID_PASSWORD = "er3";
    public static final String ERROR_BAD_CAPTCHA = "er6";

    /**
     * Returns an entry in the map for each parameter that fails to
     * validate. The key should be the name of the parameter, the
     * value should be the error message.
     */
    public Map<String, String> validateFormData(HttpServletRequest request);

    /**
     * Saves registration data to the versioned user data. Commit of
     * the data is not done here, it should be done by the caller.
     */
    public void writeFormData(ContentPolicy userData, HttpServletRequest request)
        throws CMException;
    
    /**
     * Returns an entry in the map for each parameter that should be
     * echoed back to the browser.
     * The key should be the name of the parameter and the value should
     * be the corresponding value from the request that should be
     * returned to the browser.
     */
    public Map<String, String> getEchoRequestData(HttpServletRequest request);
    
}