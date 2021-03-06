/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove;

import com.atex.plugins.brightcove.util.ConfigurationUtil;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

public class MainElementController extends RenderControllerBase {
    @Override
    public void populateModelBeforeCacheKey(RenderRequest request, TopModel m, ControllerContext context) {
        super.populateModelBeforeCacheKey(request, m, context);

        ModelWrite local = m.getLocal();
        BrightCoveElementPolicy policy = getBrightCoveElementPolicy(context);
        
        String playerId = policy.getPlayerId();

        if (playerId == null || playerId.isEmpty()) {
            playerId = getConfigPlayerId(context);
            local.setAttribute("playerId", playerId);
        }
    }

    protected String getConfigPlayerId(ControllerContext context)  {
        PolicyCMServer cmServer = getPolicyCMServer(context);
        ConfigurationUtil config = getConfiguration(cmServer);
        ContentId id = context.getContentId();
        return config.getConfigPlayerId(id);
    }

    public BrightCoveElementPolicy getBrightCoveElementPolicy(ControllerContext context) {
        return (BrightCoveElementPolicy) context.getContentModel().getAttribute("_data");
    }

    public PolicyCMServer getPolicyCMServer(ControllerContext context) {
        return getCmClient(context).getPolicyCMServer();
    }

    public ConfigurationUtil getConfiguration(PolicyCMServer cmServer){
        return new ConfigurationUtil(cmServer);
    }
}