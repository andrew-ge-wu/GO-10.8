/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.common.logging.LogUtil;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

/**
 * This controller will be auto injected into the output template since the name
 * of the class is the same as the name of the template.
 */
public class MainElementController extends RenderControllerBase {

    private final static String TWITTER_WIDGET_CONFIG = "com.atex.plugins.twitter.TwitterWidgetConfigHome";
    private final static Logger LOG = LogUtil.getLog(MainElementController.class);

    private PolicyCMServer policyCmServer;
    private ModelWrite local;
    private TwitterWidgetPolicy policy;

    @Override
    public void populateModelBeforeCacheKey(RenderRequest request, TopModel m, ControllerContext context) {

        super.populateModelBeforeCacheKey(request, m, context);

        policyCmServer = getCmClient(context).getPolicyCMServer();
        local = m.getLocal();
        policy = getTwitterWidgetPolicy();

        try {
            local.setAttribute("shellBgColor", policy.getShellBackgroundColor());
            local.setAttribute("shellFgColor", policy.getShellTextColor());
            local.setAttribute("tweetBgColor", policy.getTweetBackgroundColor());
            local.setAttribute("tweetFgColor", policy.getTweetTextColor());
            local.setAttribute("tweetLinkColor", policy.getTweetLinkColor());
            local.setAttribute("width", policy.getWidth());
            local.setAttribute("height", policy.getHeight());
            local.setAttribute("avatars", policy.getAvatars());
            local.setAttribute("scrollbar", policy.getScrollbar());
            local.setAttribute("loop", policy.getLoop());
        } catch (CMException e) {
            LOG.log(Level.WARNING, e.getLocalizedMessage());
        }
    }

    protected TwitterWidgetPolicy getTwitterWidgetPolicy() {
        TwitterWidgetPolicy policy = null;
        try {
            policy = (TwitterWidgetPolicy) policyCmServer.getPolicy(new ExternalContentId(TWITTER_WIDGET_CONFIG));
        } catch (CMException e) {
            LOG.log(Level.WARNING, e.getLocalizedMessage());
        }
        return policy;
    }
}