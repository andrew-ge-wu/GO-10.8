package example.membership;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.polopoly.siteengine.membership.UserDataManager.ServiceId;

/**
 * Handles reading service data from the "data"-cookie, see the implementing class.
 */
public interface ServiceDataCookieHandler
{
    JsonElement getCookieData(HttpServletRequest request,
                         ServiceId serviceId)
        throws ServiceDataCookieParseException;

    JsonObject getCookieData(HttpServletRequest request)
        throws ServiceDataCookieParseException,
               UnsupportedEncodingException;
    
    JsonObject getServiceDataByServiceDefinition(JsonObject serviceMap,
                                                 ServiceId serviceId);
}
