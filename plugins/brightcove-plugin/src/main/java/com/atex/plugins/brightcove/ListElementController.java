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
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class ListElementController extends RenderControllerBase {

    private static final Logger LOGGER = Logger.getLogger(ListElementPolicy.class.getName());

    @Override
    public void populateModelBeforeCacheKey(RenderRequest request, TopModel m, ControllerContext context) {
        super.populateModelBeforeCacheKey(request, m, context);
        List<List<BrightcoveVideoPolicy>> videos = new ArrayList<List<BrightcoveVideoPolicy>>();
        ListElementPolicy policy = (ListElementPolicy) context.getContentModel().getAttribute("_data");
        List<BrightcoveVideoPolicy> rawVideos = policy.getVideos();
        List<BrightcoveVideoPolicy> rowVideos = new ArrayList<BrightcoveVideoPolicy>();

        String display = policy.getDisplayLocation();
        // if display is null, video will render under own home department
        display = (display == null) ? "ownDept" : display;

        ContentId deptContentId = null;
        if(display.equalsIgnoreCase("webtvDept")) {
            deptContentId = getConfigWebTVDepartment(context);
        } else if(display.equalsIgnoreCase("selectedDept")) {
            deptContentId = policy.getSelectedDepartment();
        }

        int count = 1;

        // if maxItem not set, default value is 3
        String maxItem = policy.getMaxItem();
        maxItem = (maxItem == null || maxItem.isEmpty()) ? "3" : maxItem;

        for (BrightcoveVideoPolicy video : rawVideos) {
            rowVideos.add(video);
            if (count % Integer.parseInt(maxItem) == 0) {
                videos.add(rowVideos);
                rowVideos = new ArrayList<BrightcoveVideoPolicy>();
            }
            count++;
        }

        if (!rowVideos.isEmpty()) {
            videos.add(rowVideos);
        }

        BrightcoveVideoPolicy cover = null;
        String id = request.getParameter("vid");
        if (id != null) {
            try {
                ContentId vid = ContentIdFactory.createContentId(id);
                cover = (BrightcoveVideoPolicy) policy.getCMServer().getPolicy(vid);
            } catch (CMException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }

        if (cover == null && videos.size() > 0 && videos.get(0).size() > 0) {
            cover = videos.get(0).get(0);
        }

        request.setAttribute("cover", cover);
        request.setAttribute("items", videos);
        request.setAttribute("rawVideos", rawVideos);
        request.setAttribute("display", display);
        request.setAttribute("id", "brightcove_" + context.getRenderId());

        if(deptContentId != null) {
            request.setAttribute("department", deptContentId);
        }
    }

    public ContentId getConfigWebTVDepartment(ControllerContext context) {
        PolicyCMServer cmServer = getPolicyCMServer(context);
        ConfigurationUtil config = getConfiguration(cmServer);
        ContentId id = context.getContentId();
        return config.getConfigWebTVDepartment(id);
    }

    public PolicyCMServer getPolicyCMServer(ControllerContext context) {
        return getCmClient(context).getPolicyCMServer();
    }

    public ConfigurationUtil getConfiguration(PolicyCMServer cmServer){
        return new ConfigurationUtil(cmServer);
    }
}
