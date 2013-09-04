package example.captcha;

import java.awt.image.BufferedImage;

import javax.servlet.http.HttpServletRequest;

import com.octo.captcha.CaptchaException;

/**
 * This interface represent a image captcha for a clustered environment.
 */
public interface ClusteredImageCaptcha {

    /**
     * Accessor for the questioned challenge.
     * 
     * @return the image challenge.
     * @throws CaptchaException
     *             if failed to generate an image challenge
     */
    BufferedImage getImageChallenge() throws CaptchaException;

    /**
     * Validation routine for the response.
     * 
     * @param response
     *            to the question concerning the challenge
     * @param request
     *            the HTTP request related to the response
     * 
     * @return true if the answer is correct, false otherwise.
     * @throws CaptchaException
     *             if failed validate response
     */
    boolean validateResponse(String response, HttpServletRequest request)
            throws CaptchaException;
}
