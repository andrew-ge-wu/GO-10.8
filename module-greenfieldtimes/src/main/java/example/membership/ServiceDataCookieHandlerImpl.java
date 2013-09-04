package example.membership;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.polopoly.siteengine.membership.UserDataManager.ServiceId;

import example.membership.tools.Base64Util;
import example.membership.tools.JsonUtil;

/**
 * Handles reading of service data from the "data"-cookie.
 */
public class ServiceDataCookieHandlerImpl
    implements ServiceDataCookieHandler
{
    private final Base64Util base64Util = new Base64Util();
    private final JsonUtil jsonUtil = new JsonUtil();

    /**
     * Get the contents of the "data" cookie as a JSONObject.
     */
    public JsonObject getCookieData(HttpServletRequest request)
        throws ServiceDataCookieParseException
    {
        try {
            Cookie[] cookies = request.getCookies();
            
            String jsonString = null;
            
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if (ActionPersistUserData.COOKIE_NAME_DATA.equals(c.getName())) {
                        jsonString = c.getValue();
                        break;
                    }
                }
            }
            
            if (jsonString == null) {
                return new JsonObject();
            }
            
            JsonElement json = jsonUtil.toJson(base64Util.decode(jsonString));
            if (json.isJsonObject()) {
                return json.getAsJsonObject();
            }
            throw new ServiceDataCookieParseException("Expected JSON Object but got " + json.toString());
        } catch (UnsupportedEncodingException e) {
            throw new ServiceDataCookieParseException(e);
        } catch (JsonSyntaxException e) {
            throw new ServiceDataCookieParseException(e);
        }
    }
    
    /**
     * Get the sub part of the "data" cookie for a single serviceId.
     * 
     * @return A JSON object with the data, or null if not exists (JSON has a bad
     * type hierarchy with no common base class)
     */
    public JsonElement getCookieData(HttpServletRequest request,
                                ServiceId serviceId)
        throws ServiceDataCookieParseException
    {
        JsonObject cookieData = getCookieData(request);
        
        JsonObject data = getServiceDataByServiceDefinition(cookieData, serviceId);
        
        if (data != null) {
            return getServiceDataByServiceInstance(data, serviceId);
        }
        
        return null;
    }
    
    public JsonObject getServiceDataByServiceDefinition(JsonObject serviceMap,
                                                        ServiceId serviceId)
    {
        return serviceMap.getAsJsonObject(serviceId.getServiceDefinitionId());
    }
    
    private JsonElement getServiceDataByServiceInstance(JsonObject instanceMap,
                                                        ServiceId serviceId)
    {
        return instanceMap.get(serviceId.getServiceInstanceId());
    }
}