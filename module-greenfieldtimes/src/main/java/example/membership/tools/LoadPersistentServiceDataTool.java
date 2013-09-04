package example.membership.tools;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.polopoly.siteengine.membership.UserDataManager;
import com.polopoly.siteengine.membership.UserDataOperationFailedException;
import com.polopoly.siteengine.membership.UserDataManager.ServiceId;
import com.polopoly.siteengine.membership.UserDataManager.ServiceInfo;
import com.polopoly.siteengine.membership.UserDataManager.ServiceState;
import com.polopoly.user.server.User;
import com.polopoly.user.server.UserId;

import example.membership.CookieBackedServiceData;
import example.membership.ServiceDataCookieHandler;

/**
 * This class is used to load data stored on a user's content into a JSON map
 * for caching of that data in the browser as a cookie.
 */
public class LoadPersistentServiceDataTool
{
    private static final String CLASS = LoadPersistentServiceDataTool.class.getName();
    private static final Logger LOG = Logger.getLogger(CLASS);

    public final ServiceDataCookieHandler _serviceDataCookieHandler;
    public final UserDataManager _userDataManager;

    public LoadPersistentServiceDataTool(ServiceDataCookieHandler serviceDataCookieHandler,
                                         UserDataManager userDataManager)
    {
        _serviceDataCookieHandler = serviceDataCookieHandler;
        _userDataManager = userDataManager;
    }

    /**
     * Load the user's all service data stored on content into a JSON object that can
     * be stored in a cookie.
     */
    public JsonObject loadPersistentServiceData(final User user)
    {
        JsonObject cookieMap = new JsonObject();

        try {
            UserId userId = user.getUserId();

            ServiceInfo[] serviceInfos = _userDataManager.getServiceInfos(userId);
            
            for (ServiceInfo serviceInfo : serviceInfos) {
                ServiceId serviceId = serviceInfo.getServiceId();

                Object serviceBean = _userDataManager.getOrCreateServiceBean(userId, serviceId);

                if (serviceBean instanceof CookieBackedServiceData) {
                    CookieBackedServiceData bean = (CookieBackedServiceData) serviceBean;

                    JsonObject definitionDataMap =
                        _serviceDataCookieHandler.getServiceDataByServiceDefinition(cookieMap, serviceId);

                    if (serviceInfo.getServiceState() == ServiceState.ENABLED) {
                        addServiceToCookie(cookieMap, serviceId, bean, definitionDataMap);
                    }
                }
            }
        } catch (RemoteException e) {
            LOG.log(Level.WARNING, "Failed to get user id for logged in user.", e);
        } catch (JsonSyntaxException e) {
            LOG.log(Level.WARNING, "Failed to parse persistent service data.", e);
        } catch (UserDataOperationFailedException e) {
            LOG.log(Level.WARNING, "User data operation failed.", e);
        }

        return cookieMap;
    }

    private void addServiceToCookie(JsonObject cookieMap,
                                    ServiceId serviceId,
                                    CookieBackedServiceData bean,
                                    JsonObject definitionMap)
    {
        if (definitionMap == null) {
            definitionMap = new JsonObject();
            cookieMap.add(serviceId.getServiceDefinitionId(), definitionMap);
        }

        definitionMap.add(serviceId.getServiceInstanceId(), bean.loadToJsonCookie());
    }
}