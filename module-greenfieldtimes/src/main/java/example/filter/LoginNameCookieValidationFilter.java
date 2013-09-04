package example.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.polopoly.user.server.User;

import example.membership.ActionLogin;
import example.membership.UserHandler;
import example.membership.UserHandlerImpl;
import example.membership.tools.Base64Util;
import example.membership.tools.SitePrefixUtil;

/**
 * Implements soft validation of the loginName cookie.
 * 
 * This is not for security reasons, rather it is used to ensure that only one
 * view of the session status exists, without polling the user server overly often.
 * 
 * In essence, the filter checks if the user carries both a loginName cookie
 * and a session. If so, it checks if the session user name matches the loginName.
 * If not, the loginName cookie is cleared.
 * 
 * This works in concord with the ordinary request preparator filter, which does a
 * generic lightweight session validation earlier in the filter stack.
 *
 */
public class LoginNameCookieValidationFilter implements Filter
{
    private static final Logger LOG =
        Logger.getLogger(LoginNameCookieValidationFilter.class.getName());
    
    private final Base64Util _base64Util = new Base64Util();
    private final SitePrefixUtil _sitePrefixUtil = new SitePrefixUtil();
    
    private UserHandler _userHandler = new UserHandlerImpl();
    
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
        throws IOException,
               ServletException
    {
        try {
            HttpServletRequest httpRequest   = (HttpServletRequest)  request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            Cookie[] cookies                 = httpRequest.getCookies();

            Cookie loginCookie  = getCookie(cookies, ActionLogin.COOKIE_NAME_LOGIN);

            if (loginCookie != null) {
                String userLoginName  = getUserLoginName(httpRequest, httpResponse);
                if (!isLoginNameCorrect(loginCookie, userLoginName)) {
                    clearCookie(httpRequest, httpResponse, ActionLogin.COOKIE_NAME_LOGIN);
                }
            }

        } catch (Exception e) {
            LOG.log(Level.FINE, "Couldn't get login name for filtering", e);
        }
        chain.doFilter(request, response);
    }
    
    private String getUserLoginName(HttpServletRequest request,
                                    HttpServletResponse response)
        throws ServletException, RemoteException
    {
        User user = _userHandler.getUserIfPresent(request, response);
        if (user != null) {
            return user.getLoginName();
        }
        return null;
    }

    private boolean isLoginNameCorrect(Cookie loginCookie, String userLoginName)
        throws UnsupportedEncodingException
    {
        String cookieLoginName = _base64Util.decode(loginCookie.getValue());
        return cookieLoginName != null
                   ? cookieLoginName.equalsIgnoreCase(_sitePrefixUtil.stripPrefix(userLoginName))
                   : false;
    }

    private Cookie getCookie(Cookie[] cookies, String cookieName)
    {
        if (cookies != null) {
            for (Cookie cookie: cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    private void clearCookie(HttpServletRequest httpRequest,
                             HttpServletResponse httpResponse,
                             String cookieName)
    {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        httpResponse.addCookie(cookie);
    }

    public void destroy()
    {
        
    }

    public void init(FilterConfig filterConfig) throws ServletException
    {
        
    }
    
    void setUserHandler(UserHandler userHandler)
    {
        _userHandler = userHandler;
    }
}
