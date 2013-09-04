package example.content;

import com.polopoly.application.Application;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.CmClientBase;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.pear.ApplicationFactory;
import com.polopoly.pear.PolopolyApplication;
import com.polopoly.siteengine.dispatcher.ControllerContext;

public class PolicyCMServerProviderFromContext {

    public PolicyCMServer getPolicyCMServer(ControllerContext context) {
        // Try to get policy cm server using the Polopoly Application Framework.
        Application app = context.getApplication();
    
        if (app != null) {
            CmClient client =
                    (CmClient) app.getApplicationComponent(CmClientBase.DEFAULT_COMPOUND_NAME);
            
            return client.getPolicyCMServer();
        }
    
        // Try to get policy cm server using PEAR.
        PolopolyApplication polopolyApplication =
                ((PolopolyApplication) ApplicationFactory.getApplication());
        
        return polopolyApplication.getPolicyCMServer();
    }

}
