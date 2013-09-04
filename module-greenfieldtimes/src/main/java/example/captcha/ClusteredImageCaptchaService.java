package example.captcha;

import java.awt.image.BufferedImage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.octo.captcha.service.CaptchaServiceException;

/**
 * Used by client applications to expose Captchas challenges in a clustered
 * environment (e.g. multiple, load balanced HTTP servers not using sticky
 * sessions).
 */
public interface ClusteredImageCaptchaService {

    /**
     * Method to retrieve the image challenge. As a side effect a ciphered
     * answer cookie will be set containing the answer for the returned
     * challenge.
     * 
     * @param response the response to set the ciphered answer cookie to.
     * @return the image challenge
     * 
     * @throws CaptchaServiceException if failed to generate a challenge
     */
    BufferedImage getImageChallenge(HttpServletResponse response)
            throws CaptchaServiceException;

    /**
     * Method to validate an answer. The request is assumed to contain the
     * answer cookie. As a side effect the cookie (is existing) will be
     * invalidated (from the response).
     * 
     * @param answer the answer to validate
     * @param request the request to get the ciphered answer cookie from.
     * @param response the response to invalidate the answer cookie (if valid answer)
     * 
     * @return true if the response is correct, false otherwise.
     * 
     * @throws CaptchaServiceException if failed to validate the answer
     */
    boolean validateAnswer(String answer, HttpServletRequest request,
            HttpServletResponse response) throws CaptchaServiceException;
    
    /**
     * Returns true if this service is enabled, else false.
     * 
     * @return true if enabled, else false
     */
    boolean isEnabled();
    
}
