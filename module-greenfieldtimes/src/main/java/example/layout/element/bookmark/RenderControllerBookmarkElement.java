package example.layout.element.bookmark;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.model.request.ContentPath;

import example.content.RenderControllerExtended;

/**
 * Render controller for the bookmark element. Prepares
 * the local model with the name of the article which the
 * element will create links for.
 */
public class RenderControllerBookmarkElement extends RenderControllerExtended {

    private final static Logger LOG = Logger.getLogger(RenderControllerBookmarkElement.class.getName());

    @Override
    public void populateModelAfterCacheKey(RenderRequest request, TopModel m,
            CacheInfo cacheInfo, ControllerContext context) {
        super.populateModelAfterCacheKey(request, m, cacheInfo, context);

        try {
            ModelWrite localModel = m.getLocal();
            ContentPath pathAfterPage = m.getContext().getPage().getPathAfterPage();
            
            if (pathAfterPage != null) {
                ContentId articleId = pathAfterPage.getLast();
                
                if (articleId != null) {
                    PolicyCMServer cmServer = _policyCMServerProvider.getPolicyCMServer(context);
                    Policy articlePolicy = cmServer.getPolicy(articleId);
                    
                    localModel.setAttribute("articleName",
                                            articlePolicy.getContent().getName());
                }
            }
        } catch (CMException cme) {
            LOG.log(Level.WARNING, "Error while preparing bookmark element information.", cme);
        }
    }
}
