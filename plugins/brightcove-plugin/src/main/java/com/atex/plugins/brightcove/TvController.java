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
import com.brightcove.org.json.JSONArray;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.ModelWrite;
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
public class TvController extends RenderControllerBase {
    
    private static final Logger LOGGER = Logger.getLogger(TvController.class.getName());

    @Override
    public void populateModelBeforeCacheKey(RenderRequest request, TopModel m, ControllerContext context) {
        super.populateModelBeforeCacheKey(request, m, context);
        JSONArray jsVideos = new JSONArray();
        List<List<BrightcoveVideoPolicy>>  videos = new ArrayList<List<BrightcoveVideoPolicy>>();
        TVPolicy tv = (TVPolicy)context.getContentModel().getAttribute("_data");
        List<BrightcoveVideoPolicy> rawVideos = tv.getVideos();
        List<BrightcoveVideoPolicy> rowVideos = new ArrayList<BrightcoveVideoPolicy>();
        int count = 1;
        for(BrightcoveVideoPolicy video: rawVideos) {
            rowVideos.add(video);
            if(count %3 == 0) {
                videos.add(rowVideos);
                rowVideos = new ArrayList<BrightcoveVideoPolicy>();
            }
            count++;
        }
        
        if(!rowVideos.isEmpty()) {
            videos.add(rowVideos);
        }
        BrightcoveVideoPolicy cover = null;
        String id = request.getParameter("vid");
        if(id != null) {
            try {
                ContentId vid = ContentIdFactory.createContentId(id);
                cover = (BrightcoveVideoPolicy)tv.getCMServer().getPolicy(vid);
            } catch (CMException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
        
        if(cover == null && videos.size() > 0 && videos.get(0).size() > 0) {
            cover = videos.get(0).get(0);
        }
        request.setAttribute("cover", cover);
        request.setAttribute("items", videos);
        String playerId = getConfigPlayerId(context);
        ModelWrite local = m.getLocal();
        if (id!=null && cover!=null) {
            jsVideos.put(jsonItem(cover));
        }
        for (List<BrightcoveVideoPolicy> list: videos) {
            for (BrightcoveVideoPolicy item: list) {
                jsVideos.put(jsonItem(item));
            }
        }
        local.setAttribute("jsVideos", jsVideos);
        local.setAttribute("playerId", playerId);
        local.setAttribute("firstBcListEle", tv.getFirstBcListEle());
        local.setAttribute("categorizationOpt", tv.getSelectedCategorizationOption());
        local.setAttribute("relatedElementId", tv.getRelatedElementId());
        
    }

    protected JSONArray jsonItem(BrightcoveVideoPolicy policy) {
        JSONArray obj = new JSONArray();
        obj.put(getShortId(policy));
        obj.put(getVideoName(policy));
        obj.put(policy.getId());
        return obj;
    }

    protected String getShortId(BrightcoveVideoPolicy policy) {
        VersionedContentId vcId = policy.getContentId();
        ContentId cId = vcId.getContentId();
        return cId.getContentIdString();
    }

    protected String getVideoName(BrightcoveVideoPolicy policy) {
        try {
            return policy.getName();
        } catch (CMException e) {
            return "";
        }
    }

    protected String getConfigPlayerId(ControllerContext context)  {
        PolicyCMServer cmServer = getPolicyCMServer(context);
        ConfigurationUtil config = getConfiguration(cmServer);
        ContentId id = context.getContentId();
        return config.getConfigPlayerId(id);
    }

    public PolicyCMServer getPolicyCMServer(ControllerContext context) {
        return getCmClient(context).getPolicyCMServer();
    }

    public ConfigurationUtil getConfiguration(PolicyCMServer cmServer){
        return new ConfigurationUtil(cmServer);
    }
}
