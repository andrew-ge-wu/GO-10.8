package example.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import example.captcha.CaptchaSettingsPolicy;
import example.captcha.ClusteredImageCaptchaService;

/**
 * Validates any captcha challenges if the request parameter valid_captcha,
 * together with the captcha cookie, is given.
 */
public class CaptchaValidationFilter implements Filter
{
    public static final String IS_VALID_CAPTCHA_ATTRIBUTE = "valid_captcha";
    public static final String CAPTCHA_COOKIE_NAME = "captcha";
    public static final String CAPTCHA_RESPONSE_FIELD_NAME = "captcha_response";
    
    private ClusteredImageCaptchaService _captchaService;
    
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
        throws IOException,
               ServletException
    {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String captchaResponse = httpRequest.getParameter(CAPTCHA_RESPONSE_FIELD_NAME);
        
        if (_captchaService != null && captchaResponse != null) {
            for(Cookie cookie: httpRequest.getCookies()) {
                if (cookie.getName().equals(CAPTCHA_COOKIE_NAME)) {
                    validateCaptcha(captchaResponse, httpRequest, httpResponse);
                    break;
                }
            }
        }
        
        chain.doFilter(request, response);
    }

    private void validateCaptcha(String captchaResponse,
            HttpServletRequest request, HttpServletResponse response) {
        boolean validCaptcha = _captchaService.validateAnswer(captchaResponse, request, response);
        request.setAttribute(IS_VALID_CAPTCHA_ATTRIBUTE, new Boolean(validCaptcha));
    }

    public void destroy()
    {
        
    }

    public void init(FilterConfig config) throws ServletException
    {
        _captchaService = (ClusteredImageCaptchaService) config.getServletContext().
                            getAttribute(CaptchaSettingsPolicy.CAPTCHA_SERVICE_SERVLET_CONTEXT_KEY);
    }
    
}
