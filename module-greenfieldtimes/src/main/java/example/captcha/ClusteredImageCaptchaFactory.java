package example.captcha;

import com.octo.captcha.CaptchaException;

/**
 * Class implementing this interface has the responsibility to build image
 * captcha for a clustered environment (e.g. multiple, load balanced HTTP
 * servers not using sticky sessions).
 */
public interface ClusteredImageCaptchaFactory {

    /**
     * Builds a image captcha.
     * 
     * @return a image captcha
     * @throws CaptchaException
     *             if unable to generate image captcha
     */
    public ClusteredImageCaptcha getImageCaptcha() throws CaptchaException;
}
