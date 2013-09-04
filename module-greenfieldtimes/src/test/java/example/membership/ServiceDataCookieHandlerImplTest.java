package example.membership;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.mockito.Mock;

import com.polopoly.siteengine.membership.UserDataManager.ServiceId;

import example.MockitoBase;

public class ServiceDataCookieHandlerImplTest extends MockitoBase
{
    @Mock private HttpServletRequest _request;
    
    @Mock private Cookie _cookie;
    @Mock private Cookie _cookie2;
    
    private ServiceDataCookieHandlerImpl toTest;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        toTest = new ServiceDataCookieHandlerImpl();
    }

    public void testGetCookieData()
        throws Exception
    {
        ServiceId serviceId = new ServiceId("mnl", "7.132");
        Cookie[] cookies = { _cookie, _cookie2 };
        
        when(_request.getCookies()).thenReturn(cookies);
        when(_cookie.getName()).thenReturn(ActionPersistUserData.COOKIE_NAME_DATA);
        
        when(_cookie.getValue()).thenReturn("eyJtbmwiOnsiNy4xMzIiOiJbXCJkYXRhXCJdIn19");
        
        String cookieData = toTest.getCookieData(_request, serviceId).getAsString();                
        
        verify(_cookie2, never()).getName();
        
        assertEquals("[\"data\"]", cookieData);
    }
    
    public void testGetCookieDataParseException()
        throws Exception
    {
        ServiceId serviceId = new ServiceId("mnl", "7.128");
        Cookie[] cookies = { _cookie, _cookie2 };
        
        when(_request.getCookies()).thenReturn(cookies);
        when(_cookie.getName()).thenReturn(ActionPersistUserData.COOKIE_NAME_DATA);
        
        when(_cookie.getValue()).thenReturn("jytgjyujhhjgjg"); // random string
        
        try {
            toTest.getCookieData(_request, serviceId);
            fail("Should have thrown a parse exception.");
        } catch (ServiceDataCookieParseException e) {
            // Ignore
        }
    }

    public void testGetCookieDataCookieNotFound()
        throws Exception
    {
        ServiceId serviceId = new ServiceId("mnl", "7.128");
        Cookie[] cookies = { _cookie };

        when(_request.getCookies()).thenReturn(cookies);
        when(_cookie.getName()).thenReturn("unknownCookie");
       
        Object cookieData = toTest.getCookieData(_request, serviceId);
        
        verify(_request).getCookies();
        verify(_cookie).getName();
        
        assertNull(cookieData);
    }
    
    public void testGetCookieDataNoCookiesAtAll()
        throws Exception
    {
        when(_request.getCookies()).thenReturn(null);
        
        Object jsonObject = toTest.getCookieData(_request);
        assertNotNull(jsonObject);
    }
}
