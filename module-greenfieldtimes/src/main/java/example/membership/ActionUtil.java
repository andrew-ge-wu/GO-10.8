package example.membership;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;

import com.google.gson.JsonObject;

import example.membership.tools.Base64Util;
import example.util.UrlUtil;

public class ActionUtil
{
    private static final String CLASS = ActionUtil.class.getName();
    private static final Logger LOG = Logger.getLogger(CLASS);

    private final UrlUtil _urlUtil = new UrlUtil();
    private final Base64Util base64Util = new Base64Util();
    
    private final String _parameterFormError;
    
    public ActionUtil(String parameterFormError)
    {
        _parameterFormError = parameterFormError;
    }
    
    public String logAndFail(String failureUrl,
                             String errorMsg,
                             Exception e,
                             String status)
    {
        return logAndFail(failureUrl, Level.FINE, errorMsg, e, status);
    }

    public String logAndFail(String failureUrl,
                             Level level,
                             String errorMsg,
                             Exception e,
                             String status)
    {
        LOG.log(level, errorMsg, e);
        return appendErrorMsg(failureUrl, status);
    }
    
    public String appendErrorMsg(final String redirectUrl,
                                 final String errorStatus)
    {
        return _urlUtil.appendQueryParam(redirectUrl, _parameterFormError, errorStatus);
    }
    
    public Cookie createMessageCookieFromJsonObject(String cookieKey, JsonObject cookieMap)
    {
        String bytes = null;
        
        if (cookieMap != null) {
            String json = cookieMap.toString();
                        
            try {
                bytes = base64Util.encode(json);
            } catch (UnsupportedEncodingException e) {
                LOG.log(Level.WARNING, "Unable to create UTF-8 encoded JSON cookie", e);
            }
        }
        
        Cookie cookie = new Cookie(cookieKey, bytes);
        cookie.setPath("/");
        
        return cookie;
    }
    
    public Cookie createMessageCookie(String cookieKey, Map<String, String> valueMap)
    {
        JsonObject jsonObject = new JsonObject();

        if (valueMap != null) {
            for (Map.Entry<String, String> e : valueMap.entrySet()) {
                jsonObject.addProperty(e.getKey(), e.getValue());
            }
        }

        return createMessageCookieFromJsonObject(cookieKey, jsonObject);
    }

    public Cookie createMessageCookie(String cookieKey,
                                      String key,
                                      String message)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put(key, message);
        
        return createMessageCookie(cookieKey, map);
    }
    
}