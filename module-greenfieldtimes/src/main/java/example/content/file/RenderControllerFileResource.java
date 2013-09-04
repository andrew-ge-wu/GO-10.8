package example.content.file;

import javax.servlet.http.HttpServletRequest;

import com.atex.plugins.baseline.url.UrlResolver;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

/**
 * A controller is
 */
public class RenderControllerFileResource extends RenderControllerBase {
    
    @Override
    public void populateModelAfterCacheKey(RenderRequest request, TopModel m,
            CacheInfo cacheInfo, ControllerContext context) {
        super.populateModelAfterCacheKey(request, m, cacheInfo, context);

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        
        UrlResolver urlResolver = new FileURLResolver(httpServletRequest);
        FileResourcePolicy policy = (FileResourcePolicy) ModelPathUtil.getBean(context.getContentModel());
        m.getLocal().setAttribute("smallIconPath", policy.getSmallIconPath(urlResolver));
        m.getLocal().setAttribute("previewPath", policy.getPreviewPath(urlResolver));
    }
}