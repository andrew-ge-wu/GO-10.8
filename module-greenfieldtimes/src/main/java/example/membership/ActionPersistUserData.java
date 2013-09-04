package example.membership;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonElement;
import com.polopoly.siteengine.membership.UserDataConcurrentModificationException;
import com.polopoly.siteengine.membership.UserDataManager;
import com.polopoly.siteengine.membership.UserDataManager.ServiceId;
import com.polopoly.siteengine.membership.UserDataManager.ServiceState;
import com.polopoly.siteengine.membership.UserDataOperationFailedException;
import com.polopoly.user.server.User;
import com.polopoly.user.server.UserId;

/**
 * This action is used to persist user data on content via the UserDataManager.
 * The data that should be persisted should be stored in the 'data'-cookie before
 * posting to this action. This post action takes two parameters. The first parameter is
 * sdid (service data id), the "type" of service, for example "mnl" (for my news list).
 * The second parameter siid (service instance id), this is the instance of service. In the
 * my news list example every my news list element on the site has it's own instance id (the
 * content id of the element).
 */
public class ActionPersistUserData implements Action
{
    private static final String CLASS = ActionPersistUserData.class.getName();
    private static final Logger LOG = Logger.getLogger(CLASS);
        
    public static final String PARAMETER_SERVICE_DEFINITION_ID = "sdid";
    public static final String PARAMETER_SERVICE_INSTANCE_ID = "siid";
    
    public static final String COOKIE_NAME_DATA = "data";
    
    private final UserHandler _userHandler;
    private final ServiceDataCookieHandler _serviceDataCookieHandler;
    private final UserDataManager _userDataManager;
    
    public ActionPersistUserData(UserHandler userHandler,
                                 ServiceDataCookieHandler serviceDataCookieHandler,
                                 UserDataManager userDataManager)
    {
        _userHandler = userHandler;
        _serviceDataCookieHandler = serviceDataCookieHandler;
        _userDataManager = userDataManager;
    }

    public void perform(HttpServletRequest request,
                        HttpServletResponse response)
        throws IOException, ServletException
    {
        User user = _userHandler.getLoggedInUser(request, response);
        if (user == null) {
            LOG.log(Level.FINE, "User must be logged in to persist data."
                              + " Might also suggest disconnected mode.");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            // Should actually be SC_INTERNAL_SERVER_ERROR if disconnected.
            return;
        }
        
        UserId userId = user.getUserId();
        
        String serviceDefinitionId = request.getParameter(PARAMETER_SERVICE_DEFINITION_ID);
        String serviceInstanceId = request.getParameter(PARAMETER_SERVICE_INSTANCE_ID);

        try {
            ServiceId serviceId = new ServiceId(serviceDefinitionId, serviceInstanceId);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE,
                        "Storing service bean for user: " + userId
                        + ", serviceId: " + serviceId);
            }

            try {
                storeServiceData(userId, serviceId, request);
            } catch (UserDataConcurrentModificationException instantRetryMarker) {
                // Store based on old data, try again.
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE,
                            "Service bean for user: " + userId
                            + ", serviceId: " + serviceId + " updated"
                            + " by another client, doing an instant retry.");
                }
            
                storeServiceData(userId, serviceId, request);
            }
        } catch (IllegalArgumentException e) {
            LOG.log(Level.WARNING, "Invalid service id in request.", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (ServiceDataCookieParseException e) {
            LOG.log(Level.WARNING, "Error while parsing data cookie.", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (UserDataConcurrentModificationException e) {
            LOG.log(Level.WARNING, "Failed to persist user data.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (UserDataOperationFailedException e) {
            LOG.log(Level.WARNING, "User data operation failed.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void storeServiceData(UserId userId, ServiceId serviceId, HttpServletRequest request)
        throws UserDataOperationFailedException,
               ServiceDataCookieParseException,
               UserDataConcurrentModificationException
    {
        ServiceState serviceState = _userDataManager.getServiceState(userId, serviceId);

        if (serviceState == ServiceState.ENABLED) {
            Object object = _userDataManager.getOrCreateServiceBean(userId, serviceId);

            if (object instanceof CookieBackedServiceData) {
                JsonElement jsonData = _serviceDataCookieHandler.getCookieData(request, serviceId);
                
                CookieBackedServiceData bean = (CookieBackedServiceData) object;
                bean.storeFromJsonCookie(jsonData);
                _userDataManager.commitServiceBean(userId, serviceId, bean);
            }
        }
    }
}
