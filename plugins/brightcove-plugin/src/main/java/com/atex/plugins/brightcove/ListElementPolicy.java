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
import com.polopoly.cm.app.policy.SingleReference;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListProvider;
import com.polopoly.cm.policy.ContentListWrapper;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.siteengine.layout.ContentRepresentative;
import com.polopoly.siteengine.standard.content.ContentBasePolicy;

public class ListElementPolicy extends ContentBasePolicy implements VideoListModelTypeDescription, ContentRepresentative {

    private static final Logger LOGGER = Logger.getLogger(ListElementPolicy.class.getName());

    public static final String CONTENT_LIST_NAME = "videos";
    public static final String MAX_ITEM = "mode/slide/size";
    public static final String MAX_VIDEO = "maxVideo";
    public static final String DISPLAY = "display";
    public static final String SUB_FIELD = "subField";
    public static final String SELECTED_DEPT = "display/selectedDept";


    public Collection<ContentId> getRepresentedContent() {
        Collection<ContentId> results = new ArrayList<ContentId>();
        ContentList publishingQueues = null;
        try {
            publishingQueues = ((ContentListWrapper) getContentList(CONTENT_LIST_NAME)).getWrappedContentList();

            for (int contentIndex = 0; contentIndex < publishingQueues.size(); contentIndex++) {
                ContentReference reference = publishingQueues.getEntry(contentIndex);
                ContentList policy = ((ContentListProvider) getCMServer().getPolicy(reference.getReferredContentId()))
                        .getContentList();
                for (int i = 0; i < policy.size(); i++) {
                    ContentReference videoReference = policy.getEntry(i);
                    results.add(videoReference.getReferredContentId());
                }
            }
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
        return results;
    }

    public List<BrightcoveVideoPolicy> getVideos() {
        Collection<ContentId> videoIds = getRepresentedContent();
        List<BrightcoveVideoPolicy> results = new ArrayList<BrightcoveVideoPolicy>();
        int max = getMax();
        int count = 0;
        PolicyCMServer server = getCMServer();
        for (ContentId imageId : videoIds) {
            try {
                results.add((BrightcoveVideoPolicy) server.getPolicy(imageId));
                count++;
                if(count >= max) {
                    break;
                }
            } catch (CMException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return results;
    }

    public String getMaxItem() {
        return getChildValue(MAX_ITEM);
    }

    public String getMaxVideo() {
        return getChildValue(MAX_VIDEO);
    }

    public int getMax() {
        return Integer.parseInt(getChildValue(MAX_VIDEO));
    }

    public String getDisplayLocation() {
        String location = null;
        try {
            location = getChildPolicy(DISPLAY).getComponent(SUB_FIELD);
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
        return location;
    }

    public ContentId getSelectedDepartment() {
        ContentId contentId = null;
        try {
            contentId = ((SingleReference) getChildPolicy(SELECTED_DEPT)).getReference();
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
        return contentId;
    }

}
