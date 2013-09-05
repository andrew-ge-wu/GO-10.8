/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.youtube;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.app.inbox.InboxFlags;
import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.client.CMException;
import com.polopoly.siteengine.standard.content.ContentBasePolicy;


public class YoutubeElementPolicy extends ContentBasePolicy {
    public static final String YID = "yid";
    public static final String TITLE = "title";
    public static final String HEIGHT = "videoheight";
    public static final String WIDTH = "videowidth";
    public static final String LINK = "youtubeLink";
    public static final String DESCRIPTION = "youtubeDescription";
    public static final String START_TIME = "startTime";
    public static final String PLAY_TIME = "playTimeLen";
    public static final String THUMBNAIL_URL = "thumbnailUrl";
    private static final Logger LOGGER = Logger.getLogger(YoutubeElementPolicy.class.getName());

    public String getYid() {
        return getChildValue(YID);
    }

    public void setYid(String yid) {
        setChidValue(YID, yid);
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return getChildValue(TITLE);
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        setChidValue(TITLE, title);
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return getChildValueAsInt(HEIGHT, 0);
    }

    /**
     * @param height
     *            the height to set
     */
    public void setHeight(String height) {
        setChidValue(HEIGHT, height);
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return getChildValueAsInt(WIDTH, 0);
    }

    /**
     * @param width
     *            the width to set
     */
    public void setWidth(String width) {
        setChidValue(WIDTH, width);
    }

    /**
     * @return the link
     */
    public String getLink() {
        return getChildValue(LINK);
    }

    /**
     * @param link
     *            the link to set
     */
    public void setLink(String link) {
        setChidValue(LINK, link);
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return getChildValue(DESCRIPTION);
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        setChidValue(DESCRIPTION, description);
    }

    public int getStartTime() {
        try {
            return Integer.valueOf(getChildValue(START_TIME));
        } catch (Exception e) {
            return -1;
        }
    }

    public void setStartTime(int startTime) {
        setChidValue(START_TIME, String.valueOf(startTime > 0 ? startTime : -1));
    }

    public String getThumbnailUrl() {
        return getChildValue(THUMBNAIL_URL);
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        setChidValue(THUMBNAIL_URL, thumbnailUrl);
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

    public int getPlayTime() {
        try {
            return Integer.valueOf(getChildValue(PLAY_TIME));
        } catch (Exception e) {
            return -1;
        }
    }

    public void setPlayTime(int playTime) {
        setChidValue(PLAY_TIME, String.valueOf(playTime > 0 ? playTime : -1));
    }

    @Override
    public void postCreateSelf() throws CMException {
        super.postCreateSelf();
        new InboxFlags().setShowInInbox(this, true);
    }

    protected int getChildValueAsInt(String child, int fallback) {
        try {
            return Integer.parseInt(getChildValue(child));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

}
