package example.layout.element.code;

import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;

import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

/**
 * Element code controller. Populates a model with custom code.
 * 
 */
public class RenderControllerCodeElement extends RenderControllerBase {

    @Override
    public void populateModelAfterCacheKey(RenderRequest request, TopModel m,
            CacheInfo cacheInfo, ControllerContext context) {
        super.populateModelAfterCacheKey(request, m, cacheInfo, context);

        StringResourceRepository vsRepository = StringResourceLoader.getRepository();

        String myTemplateName = "code";

        String myTemplateBody = (String) ModelPathUtil.get(m.getLocal(), "this/code/value");

        vsRepository.putStringResource(myTemplateName, myTemplateBody);
    }
}
