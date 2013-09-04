package example.util;

import java.util.regex.Pattern;

import com.polopoly.util.StringUtil;

public class InputValidator
{
    private static final Pattern SCREEN_NAME_VALID_CHARS_REGEXP =
        Pattern.compile("^[a-zA-Z0-9\u00e5\u00e4\u00f6\u00c5\u00c4\u00d6 \\-\\_]+$");
    
    private static final int SCREEN_NAME_MAX_LENGTH = 128;
    
    public boolean isValidScreenName(String screenName)
    {
        return !StringUtil.isEmpty(screenName)
               && screenName.length() <= SCREEN_NAME_MAX_LENGTH
               && SCREEN_NAME_VALID_CHARS_REGEXP.matcher(screenName).matches();
    }
}
