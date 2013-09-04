package example.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

public class CSRFUtil
{
    private static final Logger LOG = Logger.getLogger(CSRFUtil.class.getName());
    public static final String CSRF_PARAMETER_NAME = "csrf_token";

    public boolean validate(String expectedToken, HttpServletRequest request)
    {
        if (expectedToken == null) {
            return true;
        }

        boolean valid =
                stripPadding(expectedToken).equals(stripQuotationMarks(request.getParameter(CSRF_PARAMETER_NAME)));

        if (!valid) {
            LOG.log(Level.WARNING, "Invalid form data, the CSRF token doesn't match the session key.");
        }

        return valid;
    }

    private String stripPadding(String value)
    {
        int padding = value.indexOf('=');
        if (padding != -1) {
            value = value.substring(0, padding);
        }
        return value;
    }

    private String stripQuotationMarks(String value)
    {
        if (value != null) {
            value = value.replaceAll("\"", "");
        }
        return value;
    }
}
