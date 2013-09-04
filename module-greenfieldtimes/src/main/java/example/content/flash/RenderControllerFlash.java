package example.content.flash;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.application.ApplicationNotRunningException;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

import example.content.RenderControllerExtended;
import example.content.file.FileURLResolver;

public class RenderControllerFlash extends RenderControllerExtended {

    private static final Logger LOG = Logger.getLogger(RenderControllerFlash.class.getName());

    @Override
    public void populateModelAfterCacheKey(RenderRequest request, TopModel m,
            CacheInfo cacheInfo, ControllerContext context) {
        super.populateModelAfterCacheKey(request, m, cacheInfo, context);

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        ContentId cidFlash = context.getContentId();
        try {
            Policy policy = getCmClient(context).getPolicyCMServer().getPolicy(cidFlash);
            if (policy instanceof FlashResourcePolicy) {
                FlashResourcePolicy policyFlash = (FlashResourcePolicy) policy;
                String filename = policyFlash.getPreviewPath(new FileURLResolver(httpServletRequest));
                m.getLocal().setAttribute("flashFile", filename);
                m.getLocal().setAttribute("flashWidth", policyFlash.getWidth());
                m.getLocal().setAttribute("flashHeight", policyFlash.getHeight());
                m.getLocal().setAttribute("flashParameterMap", policyFlash.getParameters());
            }
        } catch (CMException e) {
            LOG.info("Could not get flash content " + cidFlash.getContentIdString());
        } catch (ApplicationNotRunningException e) {
            LOG.warning("Application not started; Could not get flash content " + cidFlash.getContentIdString());
        }
        
    }
    
    
}
