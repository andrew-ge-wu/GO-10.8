package example.captcha;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.octo.captcha.CaptchaException;
import com.octo.captcha.service.CaptchaServiceException;

/**
 * The image captcha servlet that generates images challenges.
 */
public class ImageCaptchaServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(ImageCaptchaServlet.class.getName());
    private static final long serialVersionUID = 3933987345899838368L;

    private ClusteredImageCaptchaService captchaService = null;

    public void init(ServletConfig servletConfig)
        throws ServletException
    {
        super.init(servletConfig);
        
        captchaService = (ClusteredImageCaptchaService) servletConfig.getServletContext().getAttribute(
                CaptchaSettingsPolicy.CAPTCHA_SERVICE_SERVLET_CONTEXT_KEY);
    }

    protected void doGet(HttpServletRequest httpServletRequest,
                                          HttpServletResponse httpServletResponse)
        throws ServletException, IOException
    {
        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();

        try {
            BufferedImage challenge;

            try {
                challenge = captchaService.getImageChallenge(httpServletResponse);
            } catch (CaptchaException e) {
                // Probable cause: "word is too long: try to use less letters,
                //                  smaller font or bigger background"
                LOG.log(Level.FINE,
                        "ImageCaptchaServlet: instant retry after" +
                        " captcha service error.", e);
                
                // Instant retry
                challenge = captchaService.getImageChallenge(httpServletResponse);
            }
            
            ImageIO.write(challenge, "JPEG", jpegOutputStream);
        } catch (IllegalArgumentException e) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            LOG.log(Level.FINE, "ImageCaptchaServlet: Illegal argument,"
                                + " return with response code 404.", e);
            return;
        } catch (CaptchaServiceException e) {
            httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            LOG.log(Level.FINE,
                    "ImageCaptchaServlet: Internal server error,"
                    + " return with response code 500.", e);
            return;
        }

        byte[] captchaChallengeAsJpeg = jpegOutputStream.toByteArray();

        // Set no-cache headers.
        httpServletResponse.setHeader("Cache-Control",
                                      "no-store, no-cache");
        httpServletResponse.addHeader("Cache-Control",
                                      "must-revalidate");
        httpServletResponse.addHeader("Cache-Control",
                                       "max-age=0, s-max-age=0");
        httpServletResponse.addHeader("Cache-Control",
                                      "post-check=0, pre-check=0");

        httpServletResponse.setHeader("Pragma", "no-cache");
        httpServletResponse.setDateHeader("Expires", 0);
        httpServletResponse.setContentType("image/jpeg");

        // Flush the response.
        ServletOutputStream responseOutputStream =
            httpServletResponse.getOutputStream();

        responseOutputStream.write(captchaChallengeAsJpeg);
        responseOutputStream.flush();
        responseOutputStream.close();
    }
}