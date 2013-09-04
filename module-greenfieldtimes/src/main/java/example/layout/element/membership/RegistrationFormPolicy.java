package example.layout.element.membership;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.util.StringUtil;

import example.layout.element.ElementPolicy;
import example.membership.ActionLogin;
import example.membership.ActionRegister;
import example.membership.RegistrationFormHandler;
import example.util.InputValidator;

public class RegistrationFormPolicy extends ElementPolicy
    implements RegistrationFormHandler
{
    private static final InputValidator VALIDATOR = new InputValidator();
    
    public Map<String, String> validateFormData(HttpServletRequest request)
    {
        Map<String, String> errors = new HashMap<String, String>();
        
        String screenName =
            request.getParameter(ActionRegister.PARAMETER_SCREEN_NAME);
        
        if (StringUtil.isEmpty(screenName)) {
            errors.put(ActionRegister.PARAMETER_SCREEN_NAME,
                       RegistrationFormHandler.ERROR_EMPTY_SCREENNAME);
        } else if (!VALIDATOR.isValidScreenName(screenName)) {
            errors.put(ActionRegister.PARAMETER_SCREEN_NAME,
                       RegistrationFormHandler.ERROR_BAD_SCREENNAME);
        }
        
        return errors;
    }

    public void writeFormData(ContentPolicy userData,
                              HttpServletRequest request)
        throws CMException
    {
        /*
         * Custom attributes goes here.
         * Login name & password are validated in @see ActionRegister.
         */
        String screenName =
            request.getParameter(ActionRegister.PARAMETER_SCREEN_NAME);
        
        if (screenName != null) {
        ((SingleValued) userData.getChildPolicy(ActionLogin.CHILD_POLICY_SCREEN_NAME)).setValue(screenName.trim());
    }
}

    public Map<String, String> getEchoRequestData(HttpServletRequest request)
    {
        HashMap<String, String> map = new HashMap<String, String>();
        
        String screenName =
            request.getParameter(ActionRegister.PARAMETER_SCREEN_NAME);
        
        if (!StringUtil.isEmpty(screenName)) {
            map.put(ActionRegister.PARAMETER_SCREEN_NAME, screenName);
}
        
        return map;
    }
}
