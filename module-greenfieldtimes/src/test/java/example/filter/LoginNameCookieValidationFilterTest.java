package example.filter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mockito.ArgumentMatcher;
import org.mockito.Mock;

import com.polopoly.user.server.User;

import example.MockitoBase;
import example.membership.ActionLogin;
import example.membership.UserHandler;
import example.membership.tools.Base64Util;

public class LoginNameCookieValidationFilterTest extends MockitoBase
{    
    @Mock HttpServletRequest  request;
    @Mock HttpServletResponse response;
    @Mock FilterChain         chain;
    @Mock UserHandler         userHandler;
    @Mock User                user;
    
    Cookie[] validCookieArray;
    Cookie[] emptyCookieArray;
    Cookie[] otherCookieArray;
    
    private LoginNameCookieValidationFilter toTest;
    
    private final Base64Util base64Util = new Base64Util();
    
    protected void setUp() throws Exception
    {
        super.setUp();
        toTest = new LoginNameCookieValidationFilter();
        String validUserName = base64Util.encode("adrian@greenfieldtimes.com");
        emptyCookieArray = new Cookie[] {};
        validCookieArray = new Cookie[] {
                new Cookie(ActionLogin.COOKIE_NAME_LOGIN, 
                           validUserName) };
        otherCookieArray = new Cookie[] {
                new Cookie("other", "value") };
    }
    
    public void testValidSessionKey() 
        throws Exception 
    {
        when(request.getCookies()).thenReturn(validCookieArray);
        when(userHandler.getUserIfPresent(request, response)).thenReturn(user);
        when(user.getLoginName()).thenReturn("2.157_adrian@greenfieldtimes.com");
        
        toTest.setUserHandler(userHandler);
        toTest.doFilter(request, response, chain);
        
        verify(response, never()).addCookie(any(Cookie.class));
        verify(userHandler).getUserIfPresent(any(HttpServletRequest.class),
                                             any(HttpServletResponse.class));
    }
        
    public void testValidSessionKeyWithMixedCaps() 
        throws Exception 
    {
        when(request.getCookies()).thenReturn(validCookieArray);
        when(userHandler.getUserIfPresent(request, response)).thenReturn(user);
        when(user.getLoginName()).thenReturn("2.157_ADriAn@greenfieldtImes.COM");
        
        toTest.setUserHandler(userHandler);
        toTest.doFilter(request, response, chain);
        
        verify(response, never()).addCookie(any(Cookie.class));
        verify(userHandler).getUserIfPresent(any(HttpServletRequest.class),
                                             any(HttpServletResponse.class));
    }

    public void testInvalidSessionKey() 
        throws Exception 
    {
        when(request.getCookies()).thenReturn(validCookieArray);
        when(userHandler.getUserIfPresent(request, response)).thenReturn(user);
        when(user.getLoginName()).thenReturn("sysadmin");
        
        toTest.setUserHandler(userHandler);
        toTest.doFilter(request, response, chain);
    
        verify(response, times(1)).addCookie(
               argThat(new IsValidCookie(ActionLogin.COOKIE_NAME_LOGIN, "")));
        verify(userHandler).getUserIfPresent(any(HttpServletRequest.class),
                                             any(HttpServletResponse.class));
    }
    
    public void testNoSessionKey() 
        throws Exception 
    {
        when(request.getCookies()).thenReturn(validCookieArray);
        when(userHandler.getUserIfPresent(request, response)).thenReturn(user);
        when(user.getLoginName()).thenReturn(null);
        
        toTest.setUserHandler(userHandler);
        toTest.doFilter(request, response, chain);
    
        verify(response).addCookie(any(Cookie.class));
        verify(userHandler).getUserIfPresent(any(HttpServletRequest.class),
                                             any(HttpServletResponse.class));
    }
    
    public void testSlightlyInvalidSessionKey() 
        throws Exception 
    {
        when(request.getCookies()).thenReturn(validCookieArray);
        when(userHandler.getUserIfPresent(request, response)).thenReturn(user);
        when(user.getLoginName()).thenReturn("adrian@greenfieldtimes.com");
        
        toTest.setUserHandler(userHandler);
        toTest.doFilter(request, response, chain);
        
        verify(response, times(1)).addCookie(
               argThat(new IsValidCookie(ActionLogin.COOKIE_NAME_LOGIN, "")));
        verify(userHandler).getUserIfPresent(any(HttpServletRequest.class),
                                             any(HttpServletResponse.class));
    }
    
    public void testEmptyCookieArray() 
        throws Exception 
    {
        when(request.getCookies()).thenReturn(emptyCookieArray);
        when(userHandler.getUserIfPresent(request, response)).thenReturn(user);
        when(user.getLoginName()).thenReturn("2.157_adrian@greenfieldtimes.com");
        
        toTest.setUserHandler(userHandler);
        toTest.doFilter(request, response, chain);
        
        verify(response, never()).addCookie(any(Cookie.class));
        verify(userHandler, never()).getUserIfPresent(any(HttpServletRequest.class),
                                                      any(HttpServletResponse.class));
    }
    
    public void testNullCookies() throws Exception
    {
        when(request.getCookies()).thenReturn(null);
        when(userHandler.getUserIfPresent(request, response)).thenReturn(user);
        when(user.getLoginName()).thenReturn("2.157_adrian@greenfieldtimes.com");
        
        toTest.setUserHandler(userHandler);        
        toTest.doFilter(request, response, chain);
        
        verify(response, never()).addCookie(any(Cookie.class));
        verify(userHandler, never()).getUserIfPresent(any(HttpServletRequest.class),
                                                      any(HttpServletResponse.class));
    }
    
    public void testNoLoginNameCookie() throws Exception
    {
        when(request.getCookies()).thenReturn(otherCookieArray);
        when(userHandler.getUserIfPresent(request, response)).thenReturn(user);
        when(user.getLoginName()).thenReturn("2.157_adrian@greenfieldtimes.com");
        
        toTest.setUserHandler(userHandler);        
        toTest.doFilter(request, response, chain);
        
        verify(response, never()).addCookie(any(Cookie.class));
        verify(userHandler, never()).getUserIfPresent(any(HttpServletRequest.class),
                                                      any(HttpServletResponse.class));
    }
    
    class IsValidCookie extends ArgumentMatcher<Cookie> {
        
        String _name;
        String _value;
        
        public IsValidCookie(String name, String value) {
            _name  = name;
            _value = value;
        }
        
        public boolean matches(Object c) {
            Cookie cookie = (Cookie) c;
            return cookie.getName().equals(_name) &&
                    cookie.getValue().equals(_value);
        }
     }
}
