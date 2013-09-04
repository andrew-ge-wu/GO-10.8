package example.content.video;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.cm.app.imagemanager.Image;
import com.polopoly.cm.app.imagemanager.ImageSet;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;
import com.polopoly.siteengine.standard.image.ImageResource;

import example.content.file.FileURLResolver;
import example.layout.image.ImageFormatSetup;

public class RenderControllerVideo
    extends RenderControllerBase
{
    private static final Logger LOG =
        Logger.getLogger(RenderControllerVideo.class.getName());

    @Override
    public void populateModelAfterCacheKey(RenderRequest request, TopModel m,
                    CacheInfo cacheInfo, ControllerContext context)
    {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        FileURLResolver urlResolver = new FileURLResolver(httpServletRequest);
        VideoResourcePolicy policy =
            (VideoResourcePolicy) ModelPathUtil.getBean(context.getContentModel());
        try {
            ModelPathUtil.set(m.getLocal(), "videoPath", policy.getVideoPath(urlResolver));
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Could not get video resource URL", e);
        }
        super.populateModelAfterCacheKey(request, m, cacheInfo, context);
    }

    @Override
    public void populateModelBeforeCacheKey(RenderRequest request,
                                            TopModel m,
                                            ControllerContext context)
    {
        Model contentModel = context.getContentModel();

        ImageResource image = (ImageResource) ModelPathUtil.getBean(contentModel);

        ImageSet imageSet = image.getImageSet();
        if (imageSet != null) {
            String derivativeKey = new ImageFormatSetup().setupImageDerivativeKeyInModel(m, "preview", imageSet);
            if (derivativeKey != null) {
                try {
                    Image imageDerivative = imageSet.getImage(derivativeKey);
                    ModelPathUtil.set(m.getLocal(), "previewImageHeight", imageDerivative.getHeight());
                    ModelPathUtil.set(m.getLocal(), "previewImagePath", imageDerivative.getPath());
                }
                catch (CMException e) {
                    throw new CMRuntimeException(e);
                }
                catch (IOException e) {
                    throw new RuntimeException("Could not get preview image for video resource '"
                                               + contentModel.getAttribute("contentId") + "'.", e);
                }
            }
        }
        super.populateModelBeforeCacheKey(request, m, context);
    }

 }
