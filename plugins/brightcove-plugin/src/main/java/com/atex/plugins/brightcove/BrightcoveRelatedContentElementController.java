/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.search.categorization.Categorization;
import com.polopoly.cm.app.search.categorization.CategorizationProvider;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

/**
*
*/
public class BrightcoveRelatedContentElementController extends RenderControllerBase {

    @Override
    public void populateModelBeforeCacheKey(RenderRequest request, TopModel m,
        ControllerContext context) {
        super.populateModelBeforeCacheKey(request, m, context);
        Categorization categorization = Categorization.EMPTY_CATEGORIZATION;
        Model contentModel = context.getContentModel();
        try {
            Policy policy = (Policy) ModelPathUtil.getBean(contentModel);
            PolicyCMServer cmServer = policy.getCMServer();
            while (policy != null && !(policy instanceof CategorizationProvider)) {
                ContentId securityParentId = policy.getContent().getSecurityParentId();
                if (securityParentId != null) {
                    policy = cmServer.getPolicy(securityParentId);
                }
                else {
                    policy = null;
                    break;
                }
            }
            if (policy instanceof CategorizationProvider) {
                categorization = ((CategorizationProvider) policy).getCategorization();
            }
        }
        catch (CMException e) {
            // We can't get categorization, so we just set an empty categorization
        }
        m.getLocal().setAttribute("categorization", categorization);
    }
}
