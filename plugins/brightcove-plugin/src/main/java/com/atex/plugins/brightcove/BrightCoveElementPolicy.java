/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListProvider;
import com.polopoly.cm.policy.Policy;
import com.polopoly.model.ModelTypeDescription;
import com.polopoly.siteengine.standard.content.ContentBasePolicy;

public class BrightCoveElementPolicy extends ContentBasePolicy 
    implements ModelTypeDescription {

    public static final String POLICY_PLAYER_ID = "playerId";
    public static final String POLICY_VIDEO_ID = "playerType/video/video";
    public static final String POLICY_VIDEO_LIST = "videoContainer";
    private static final Logger LOGGER = Logger.getLogger(BrightCoveElementPolicy.class.getName());

    /**
     * @return the Brightcove Player Id for this element
     */
    public String getPlayerId() {
        return getChildValue(POLICY_PLAYER_ID);
    }

    /**
     * @param value
     *      Brightcove Player Id to be set
     */
    public void setPlayerId(String value) {
        setChidValue(POLICY_PLAYER_ID, value);
    }

    /**
     * @return the Brightcove Video Id for this element
     */
    public String getVideoId() {
        Policy policy;
        try {
            policy = getChildPolicy(POLICY_VIDEO_ID);
            return ((SingleValuePolicy) policy).getValue();
        } catch (CMException e) {
            LOGGER.log(Level.INFO, "Unable to get video id");
            return "";
        }
    }

    /**
     * @param value
     *      Brightcove Video Id to be set
     */
    public void setVideoId(String value) {
        setChidValue(POLICY_VIDEO_ID, value);
    }

    public Collection<ContentId> getRepresentedContent() {
        Collection<ContentId> results = new ArrayList<ContentId>();
        ContentList contentList = null;
        try {
            contentList = getContentList(POLICY_VIDEO_LIST);
            for (int contentIndex = 0; contentIndex < contentList.size(); contentIndex++) {
                ContentReference reference = contentList.getEntry(contentIndex);
                results.add(reference.getReferredContentId());
            }
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
        return results;
    }

    /**
     * @param id ContentId of the sources/resources 
     * @return List of content id of brightcove video resources
     */
    protected List<ContentId> drillInExtractBcVideo(ContentId id) {
        List<ContentId> contentIds = new ArrayList<ContentId>();
        if (isBrightcoveVideoResource(id)) {
            contentIds.add(id);
        } else if (isPublishingQueue(id)) {
            try {
                Policy policy = getCMServer().getPolicy(id);
                ContentListProvider providerPolicy = (ContentListProvider) policy;
                ContentList contentList = providerPolicy.getContentList();
                for (int i = 0; contentList != null && i < contentList.size(); i++) {
                    contentIds.addAll(drillInExtractBcVideo(contentList.getEntry(i).getReferredContentId()));
                }
            } catch (CMException e) {
                LOGGER.log(Level.WARNING, "Error occured when retriving content list" );
            }
        }
        return contentIds;
    }

    /**
     * @param id Content Id of the resource to be checked
     * @return True value if it's a Brightcove video resources
     */
    protected boolean isBrightcoveVideoResource(ContentId id) {
        Policy policy;
        try {
            policy = getCMServer().getPolicy(id);
            if (policy instanceof BrightcoveVideoPolicy) {
                return true;
            }
        } catch (CMException e) {
            return false;
        }
        return false;
    }

    /**
     * @param id Content Id of the resource to be checked
     * @return True value if it's a publishing queue
     */
    protected boolean isPublishingQueue(ContentId id) {
        Policy policy;
        try {
            policy = getCMServer().getPolicy(id);
            if (policy instanceof ContentListProvider) {
                return true;
            }
        } catch (CMException e) {
            return false;
        }
        return false;
    }

    /**
     * @return List of Content Id which only consist of brightcove video resources
     * @throws CMException
     */
    public List<ContentId> getPureVideoList() throws CMException {
        Collection<ContentId> contentIds = getRepresentedContent();
        List<ContentId> videoIds = new ArrayList<ContentId>();
        for (ContentId contentId : contentIds) {
            videoIds.addAll(drillInExtractBcVideo(contentId));
        }
        return videoIds;
    }

    /**
     * @return the Element Id without dot
     * @throws CMException
     */
    public String getElementId() throws CMException{
        return String.valueOf(getContentId().getMajor()) + String.valueOf(getContentId().getMinor());
    }

    protected void setChidValue(String name, String value) {
        try {
            SingleValued sv = (SingleValued) getChildPolicy(name);
            if (sv != null) {
                sv.setValue(value);
            }
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Failed to find child policy: " + name, e);
        }
    }
}
