package example.filter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.polopoly.util.StringUtil;

/**
 * This class can be used to find forms on the site that lacks CSRF token validation 
 * When activated it will print warnings in the webapp log whenever the site
 * posts a form that doesn't contain a csrf token. This can be useful for
 * finding potentially vulnerable forms.
 * 
 * This filter assumes that the form field name of the token is "csrf_token"  
 * 
 * To activate the filter, map it in web.xml as such:
 *
 * <filter>
 *  <filter-name>csrftokenverificationfilter</filter-name>
 *  <filter-class>example.filter.CSRFTokenVerificationFilter</filter-class>
 * </filter>
 *
 *
 *
 * <filter-mapping>
 *   <filter-name>csrftokenverificationfilter</filter-name>
 *   <url-pattern>/*</url-pattern>
 *   <dispatcher>REQUEST</dispatcher>
 *   <dispatcher>FORWARD</dispatcher>
 * </filter-mapping>
 *
 */

public class CSRFTokenVerificationFilter
    implements Filter
{
    private static final String CSRF_TOKEN_NAME = "csrf_token";
	private static final Logger LOG =
            Logger.getLogger(CSRFTokenVerificationFilter.class.getName());

    public void init(FilterConfig filterConfig)
        throws ServletException
    {

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if ("POST".equals(httpRequest.getMethod())) {
            String csrfToken = httpRequest.getParameter(CSRF_TOKEN_NAME);
            if (StringUtil.isEmpty(csrfToken)) {
                String requestURI = httpRequest.getRequestURI();
                LOG.log(Level.SEVERE, String.format("CSRF token missing for '%s'.", requestURI));
            }
        }

        chain.doFilter(request, response);
    }

    public void destroy()
    {

    }
}
