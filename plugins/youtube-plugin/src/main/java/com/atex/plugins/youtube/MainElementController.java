/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.youtube;

import com.polopoly.model.ModelPathUtil;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

/**
 * This is Controller for Youtube player
 */
public class MainElementController extends RenderControllerBase {
    @Override
    public void populateModelAfterCacheKey(RenderRequest request, TopModel m, CacheInfo cacheInfo, ControllerContext context) {

        // calculate ratio
        Float maxWidth = Float.valueOf(m.getStack().getAttribute("colwidth").toString());
        ModelWrite model = m.getLocal();
        YoutubeElementPolicy policy = (YoutubeElementPolicy) ModelPathUtil.get(model, "content/_data");
        double width = policy.getWidth();
        double height = policy.getHeight();

        if (width == 0 || width > maxWidth) {
            width = maxWidth;
        }

        // if height is not specified
        if (height == 0) {
            height = width * 3 / 4;
        }

        model.setAttribute("widthInRatio", width);
        model.setAttribute("heightInRatio", height);
    }
}