package example.content;

import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.siteengine.mvc.RenderControllerBase;

/**
 * Extended render controller providing easier access to
 * {@link PolicyCMServer} and {@link CmClient}.
 */
public class RenderControllerExtended extends RenderControllerBase {

    protected PolicyCMServerProviderFromContext _policyCMServerProvider =
        new PolicyCMServerProviderFromContext();

}
