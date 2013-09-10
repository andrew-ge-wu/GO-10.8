/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.app.policy.CheckboxPolicy;
import com.polopoly.cm.app.policy.SingleValuePolicy;

/**
 * Twitter Widget Policy
 */
public class TwitterWidgetPolicy extends ContentPolicy {
    private final static String SHELL_BACKGROUND_COLOR = "shellBgColor";
    private final static String SHELL_TEXT_COLOR = "shellFgColor";
    private final static String TWEET_BACKGROUND_COLOR = "tweetBgColor";
    private final static String TWEET_TEXT_COLOR = "tweetFgColor";
    private final static String TWEET_LINK_COLOR = "tweetLinkColor";
    private final static String WIDTH = "width";
    private final static String HEIGHT = "height";
    private final static String AVATARS = "avatars";
    private final static String SCROLL_BAR = "scrollbar";
    private final static String LOOP = "loop";

    /**
     * Get shell background color
     * @throws com.polopoly.cm.client.CMException
     */
    public String getShellBackgroundColor() throws CMException {
        return ((SingleValuePolicy) getChildPolicy(SHELL_BACKGROUND_COLOR)).getValue();
    }

    /**
     * Set shell background color
     * @throws com.polopoly.cm.client.CMException
     */
    public void setShellBackgroundColor(String value) throws CMException {
        ((SingleValuePolicy) getChildPolicy(SHELL_BACKGROUND_COLOR)).setValue(value);
    }

    /**
     * Get shell text color
     * @throws com.polopoly.cm.client.CMException
     */
    public String getShellTextColor() throws CMException {
        return ((SingleValuePolicy) getChildPolicy(SHELL_TEXT_COLOR)).getValue();
    }

    /**
     * Set shell text color
     * @throws com.polopoly.cm.client.CMException
     */
    public void setShellTextColor(String value) throws CMException {
        ((SingleValuePolicy) getChildPolicy(SHELL_TEXT_COLOR)).setValue(value);
    }

    /**
     * Get tweet background color
     * @throws com.polopoly.cm.client.CMException
     */
    public String getTweetBackgroundColor() throws CMException {
        return ((SingleValuePolicy) getChildPolicy(TWEET_BACKGROUND_COLOR)).getValue();
    }

    /**
     * Set tweet background color
     * @throws com.polopoly.cm.client.CMException
     */
    public void setTweetBackgroundColor(String value) throws CMException {
        ((SingleValuePolicy) getChildPolicy(TWEET_BACKGROUND_COLOR)).setValue(value);
    }

    /**
     * Get tweet text color
     * @throws com.polopoly.cm.client.CMException
     */
    public String getTweetTextColor() throws CMException {
        return ((SingleValuePolicy) getChildPolicy(TWEET_TEXT_COLOR)).getValue();
    }

    /**
     * Set tweet text color
     * @throws com.polopoly.cm.client.CMException
     */
    public void setTweetTextColor(String value) throws CMException {
        ((SingleValuePolicy) getChildPolicy(TWEET_TEXT_COLOR)).setValue(value);
    }

    /**
     * Get tweet link color
     * @throws com.polopoly.cm.client.CMException
     */
    public String getTweetLinkColor() throws CMException {
        return ((SingleValuePolicy) getChildPolicy(TWEET_LINK_COLOR)).getValue();
    }

    /**
     * Set tweet link color
     * @throws com.polopoly.cm.client.CMException
     */
    public void setTweetLinkColor(String value) throws CMException {
        ((SingleValuePolicy) getChildPolicy(TWEET_LINK_COLOR)).setValue(value);
    }

    /**
     * Get widget height
     * @throws com.polopoly.cm.client.CMException
     */
    public String getHeight() throws CMException {
        return ((SingleValuePolicy) getChildPolicy(HEIGHT)).getValue();
    }

    /**
     * Set widget height
     * @throws com.polopoly.cm.client.CMException
     */
    public void setHeight(String value) throws CMException {
        ((SingleValuePolicy) getChildPolicy(HEIGHT)).setValue(value);
    }

    /**
     * Get widget width
     * @throws com.polopoly.cm.client.CMException
     */
    public String getWidth() throws CMException {
        return ((SingleValuePolicy) getChildPolicy(WIDTH)).getValue();
    }

    /**
     * Set widget width
     * @throws com.polopoly.cm.client.CMException
     */
    public void setWidth(String value) throws CMException {
        ((SingleValuePolicy) getChildPolicy(WIDTH)).setValue(value);
    }

    /**
     * Get widget avatars setting
     * @throws com.polopoly.cm.client.CMException
     */
    public boolean getAvatars() throws CMException {
        return ((CheckboxPolicy) getChildPolicy(AVATARS)).getChecked();
    }

    /**
     * Set widget avatars setting
     * @throws com.polopoly.cm.client.CMException
     */
    public void setAvatars(boolean checked) throws CMException {
        ((CheckboxPolicy) getChildPolicy(AVATARS)).setChecked(checked);
    }

    /**
     * Get widget scrollbar setting
     * @throws com.polopoly.cm.client.CMException
     */
    public boolean getScrollbar() throws CMException {
        return ((CheckboxPolicy) getChildPolicy(SCROLL_BAR)).getChecked();
    }

    /**
     * Set widget scrollbar setting
     * @throws com.polopoly.cm.client.CMException
     */
    public void setScrollbar(boolean checked) throws CMException {
        ((CheckboxPolicy) getChildPolicy(SCROLL_BAR)).setChecked(checked);
    }

    /**
     * Get widget loop setting
     * @throws com.polopoly.cm.client.CMException
     */
    public boolean getLoop() throws CMException {
        return ((CheckboxPolicy) getChildPolicy(LOOP)).getChecked();
    }

    /**
     * Set widget loop setting
     * @throws com.polopoly.cm.client.CMException
     */
    public void setLoop(boolean checked) throws CMException {
        ((CheckboxPolicy) getChildPolicy(LOOP)).setChecked(checked);
    }
}
