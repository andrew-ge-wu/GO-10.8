package example.membership;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mockito.Mock;

import com.google.gson.JsonArray;
import com.polopoly.siteengine.membership.UserDataConcurrentModificationException;
import com.polopoly.siteengine.membership.UserDataManager;
import com.polopoly.siteengine.membership.UserDataManager.ServiceId;
import com.polopoly.siteengine.membership.UserDataManager.ServiceState;
import com.polopoly.siteengine.membership.UserDataOperationFailedException;
import com.polopoly.user.server.User;
import com.polopoly.user.server.UserId;

import example.membership.mynewslist.MyNewsListData;
import example.membership.tools.JsonUtil;

public class PersistUserDataTest extends MembershipTestBase
{
    private ActionPersistUserData toTest;
    private MyNewsListData _bean;
    
    @Mock private UserHandler _userHandler;
    @Mock private ServiceDataCookieHandler _cookieHandler;
    @Mock private UserDataManager _userDataManager;
    
    @Mock private User _user;
    @Mock private UserId _userId;    
    
    @Mock private HttpServletRequest _request;
    @Mock private HttpServletResponse _response;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        toTest = new ActionPersistUserData(_userHandler,
                                           _cookieHandler,
                                           _userDataManager);
        
        _bean = new MyNewsListData();
    }

    public void testPerformServiceMNL()
        throws Exception
    {
        String serviceDefinitionId = "mnl";
        String serviceInstanceId = "7.132";
        ServiceId serviceId = new ServiceId(serviceDefinitionId, serviceInstanceId);

        JsonArray cookieData = new JsonUtil().toJson("[\"cookieData\"]").getAsJsonArray();
        
        when(_userHandler.getLoggedInUser(_request, _response)).thenReturn(_user);
        when(_user.getUserId()).thenReturn(_userId);
        when(_request.getParameter(ActionPersistUserData.PARAMETER_SERVICE_DEFINITION_ID)).thenReturn(serviceDefinitionId);        
        when(_request.getParameter(ActionPersistUserData.PARAMETER_SERVICE_INSTANCE_ID)).thenReturn(serviceInstanceId);
        
        when(_userDataManager.getServiceState(_userId, serviceId)).thenReturn(ServiceState.ENABLED);

        when(_userDataManager.getOrCreateServiceBean(_userId, serviceId)).thenReturn(_bean);
        when(_cookieHandler.getCookieData(_request, serviceId)).thenReturn(cookieData);
        
        toTest.perform(_request, _response);
        
        verify(_userHandler).getLoggedInUser(_request, _response);
        verify(_user).getUserId();
        verify(_userDataManager).getOrCreateServiceBean(_userId, serviceId);
        verify(_cookieHandler).getCookieData(_request, serviceId);
        verify(_userDataManager).commitServiceBean(_userId, serviceId, _bean);
        
        assertEquals("Bean data not same as expected.", gson.toJson(cookieData), _bean.getData());
    }
    
    public void testPerformServiceNotCookieBacked()
        throws Exception
    {
        String serviceDefinitionId = "mnl";
        String serviceInstanceId = "7.132";
        ServiceId serviceId = new ServiceId(serviceDefinitionId, serviceInstanceId);
        
        when(_userHandler.getLoggedInUser(_request, _response)).thenReturn(_user);
        when(_user.getUserId()).thenReturn(_userId);
        when(_request.getParameter(ActionPersistUserData.PARAMETER_SERVICE_DEFINITION_ID)).thenReturn(serviceDefinitionId);
        when(_request.getParameter(ActionPersistUserData.PARAMETER_SERVICE_INSTANCE_ID)).thenReturn(serviceInstanceId);
        when(_userDataManager.getOrCreateServiceBean(_userId, serviceId)).thenReturn(new NotCookieBackedBean());
        
        toTest.perform(_request, _response);
        
        verify(_userHandler).getLoggedInUser(_request, _response);
        verify(_user).getUserId();
        verify(_cookieHandler, never()).getCookieData(_request, serviceId);
        verify(_userDataManager, never()).commitServiceBean(_userId, serviceId, _bean);
    }
    
    public void testPerformServiceDisabled()
        throws Exception
    {
        String serviceDefinitionId = "mnl";
        String serviceInstanceId = "7.132";
        ServiceId serviceId = new ServiceId(serviceDefinitionId, serviceInstanceId);
        
        when(_userHandler.getLoggedInUser(_request, _response)).thenReturn(_user);
        when(_user.getUserId()).thenReturn(_userId);
        when(_request.getParameter(ActionPersistUserData.PARAMETER_SERVICE_DEFINITION_ID)).thenReturn(serviceDefinitionId);
        when(_request.getParameter(ActionPersistUserData.PARAMETER_SERVICE_INSTANCE_ID)).thenReturn(serviceInstanceId);
        
        when(_userDataManager.getServiceState(_userId, serviceId)).thenReturn(ServiceState.DISABLED);

        toTest.perform(_request, _response);

        verify(_userHandler).getLoggedInUser(_request, _response);
        verify(_user).getUserId();
        verify(_userDataManager, never()).getOrCreateServiceBean(_userId, serviceId);
    }
    
    public void testPerformServiceCookieParseError()
        throws Exception
    {
        String serviceDefinitionId = "mnl";
        String serviceInstanceId = "7.132";
        ServiceId serviceId = new ServiceId(serviceDefinitionId, serviceInstanceId);
        
        when(_userHandler.getLoggedInUser(_request, _response)).thenReturn(_user);
        when(_user.getUserId()).thenReturn(_userId);
        when(_request.getParameter(ActionPersistUserData.PARAMETER_SERVICE_DEFINITION_ID)).thenReturn(serviceDefinitionId);
        when(_request.getParameter(ActionPersistUserData.PARAMETER_SERVICE_INSTANCE_ID)).thenReturn(serviceInstanceId);
        
        when(_userDataManager.getServiceState(_userId, serviceId)).thenReturn(ServiceState.ENABLED);
        
        when(_userDataManager.getOrCreateServiceBean(_userId, serviceId)).thenReturn(_bean);
        when(_cookieHandler.getCookieData(_request, serviceId)).thenThrow(new ServiceDataCookieParseException());
        
        toTest.perform(_request, _response);
        
        verify(_userHandler).getLoggedInUser(_request, _response);
        verify(_user).getUserId();
        verify(_userDataManager).getOrCreateServiceBean(_userId, serviceId);
        verify(_cookieHandler).getCookieData(_request, serviceId);
        verify(_userDataManager, never()).commitServiceBean(_userId, serviceId, _bean);
        verify(_response).sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
    
    public void testPerformPersistError()
        throws Exception
    {
        String serviceDefinitionId = "mnl";
        String serviceInstanceId = "7.132";
        ServiceId serviceId = new ServiceId(serviceDefinitionId, serviceInstanceId);
        
        when(_userHandler.getLoggedInUser(_request, _response)).thenReturn(_user);
        when(_user.getUserId()).thenReturn(_userId);
        when(_request.getParameter(ActionPersistUserData.PARAMETER_SERVICE_DEFINITION_ID)).thenReturn(serviceDefinitionId);
        when(_request.getParameter(ActionPersistUserData.PARAMETER_SERVICE_INSTANCE_ID)).thenReturn(serviceInstanceId);
        
        when(_userDataManager.getServiceState(_userId, serviceId)).thenReturn(ServiceState.ENABLED);

        when(_userDataManager.getOrCreateServiceBean(_userId, serviceId)).thenThrow(new UserDataOperationFailedException(""));
        when(_cookieHandler.getCookieData(_request, serviceId)).thenThrow(new ServiceDataCookieParseException());
        
        toTest.perform(_request, _response);
        
        verify(_userHandler).getLoggedInUser(_request, _response);
        verify(_user).getUserId();
        verify(_userDataManager).getOrCreateServiceBean(_userId, serviceId);
        verify(_cookieHandler, never()).getCookieData(_request, serviceId);
        verify(_userDataManager, never()).commitServiceBean(_userId, serviceId, _bean);
        verify(_response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    
    public void testPerformIllegalArgumentException()
        throws Exception
    {
        String serviceDefinitionId = "mnl";
        String serviceInstanceId = "7.132";
        ServiceId serviceId = new ServiceId(serviceDefinitionId, serviceInstanceId);
        
        when(_userHandler.getLoggedInUser(_request, _response)).thenReturn(_user);
        when(_user.getUserId()).thenReturn(_userId);
        when(_request.getParameter(ActionPersistUserData.PARAMETER_SERVICE_DEFINITION_ID)).thenReturn("mnl-illegal");
        when(_request.getParameter(ActionPersistUserData.PARAMETER_SERVICE_INSTANCE_ID)).thenReturn(serviceInstanceId);

        toTest.perform(_request, _response);
        
        verify(_userHandler).getLoggedInUser(_request, _response);
        verify(_user).getUserId();
        verify(_userDataManager, never()).getOrCreateServiceBean(_userId, serviceId);
        verify(_cookieHandler, never()).getCookieData(_request, serviceId);
        verify(_userDataManager, never()).commitServiceBean(_userId, serviceId, _bean);
        verify(_response).sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
    
    public void testCommitBeanInstantRetry() throws Exception
    {
        String serviceDefinitionId = "mnl";
        String serviceInstanceId = "7.132";
        ServiceId serviceId = new ServiceId(serviceDefinitionId, serviceInstanceId);
        
        JsonArray cookieData = new JsonUtil().toJson("[\"cookieData\"]").getAsJsonArray();
        
        when(_userHandler.getLoggedInUser(_request, _response)).thenReturn(_user);
        when(_user.getUserId()).thenReturn(_userId);
        when(_request.getParameter(ActionPersistUserData.PARAMETER_SERVICE_DEFINITION_ID)).thenReturn(serviceDefinitionId);
        when(_request.getParameter(ActionPersistUserData.PARAMETER_SERVICE_INSTANCE_ID)).thenReturn(serviceInstanceId);
        
        when(_userDataManager.getServiceState(_userId, serviceId)).thenReturn(ServiceState.ENABLED);

        when(_userDataManager.getOrCreateServiceBean(_userId, serviceId)).thenReturn(_bean);
        when(_cookieHandler.getCookieData(_request, serviceId)).thenReturn(cookieData);
        
        doThrow(new UserDataConcurrentModificationException("")).when(_userDataManager).commitServiceBean(_userId, serviceId, _bean);
        
        toTest.perform(_request, _response);
        
        verify(_userHandler).getLoggedInUser(_request, _response);
        verify(_user).getUserId();
        verify(_userDataManager, times(2)).getServiceState(_userId, serviceId);
        verify(_userDataManager, times(2)).getOrCreateServiceBean(_userId, serviceId);
        verify(_cookieHandler, times(2)).getCookieData(_request, serviceId);
        verify(_userDataManager, times(2)).commitServiceBean(_userId, serviceId, _bean);
        verify(_response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    
    private class NotCookieBackedBean
    {
        
    }
}
