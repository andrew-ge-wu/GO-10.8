/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter;

import java.util.HashMap;

import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.app.policy.CheckboxPolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class TwitterWidgetPolicyTest {
    private TwitterWidgetPolicy target;

    @Mock
    private HashMap<String, Policy> children;
    @Mock
    private PolicyCMServer cmServer;
    @Mock
    private Content content;
    @Mock
    private InputTemplate inputTemplate;
    @Mock
    private Policy parent;
    @Mock
    private SingleValuePolicy shellBgColor;
    @Mock
    private SingleValuePolicy shellFgColor;
    @Mock
    private SingleValuePolicy tweetBgColor;
    @Mock
    private SingleValuePolicy tweetFgColor;
    @Mock
    private SingleValuePolicy tweetLinkColor;
    @Mock
    private SingleValuePolicy width;
    @Mock
    private SingleValuePolicy height;
    @Mock
    private CheckboxPolicy avatars;
    @Mock
    private CheckboxPolicy scrollbar;
    @Mock
    private CheckboxPolicy loop;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        target = new TwitterWidgetPolicy() {
            @Override
            protected synchronized void initChildPolicies() throws CMException {
                this.childPolicies = children;
            }
        };
        target.init("policy", new Content[] { content }, inputTemplate, parent, cmServer);

        when(children.get("shellBgColor")).thenReturn(shellBgColor);
        when(children.get("shellFgColor")).thenReturn(shellFgColor);
        when(children.get("tweetBgColor")).thenReturn(tweetBgColor);
        when(children.get("tweetFgColor")).thenReturn(tweetFgColor);
        when(children.get("tweetLinkColor")).thenReturn(tweetLinkColor);
        when(children.get("width")).thenReturn(width);
        when(children.get("height")).thenReturn(height);
        when(children.get("avatars")).thenReturn(avatars);
        when(children.get("scrollbar")).thenReturn(scrollbar);
        when(children.get("loop")).thenReturn(loop);
    }

    @Test
    public void testGetShellBackgroundColor() throws CMException {
        when(shellBgColor.getValue()).thenReturn("#00ffff");
        assertNotNull(target.getShellBackgroundColor());
        assertEquals("#00ffff", target.getShellBackgroundColor());
    }

    @Test
    public void testSetShellBackgroundColor() throws CMException {
        target.setShellBackgroundColor("#00ffff");
        verify(shellBgColor).setValue("#00ffff");
    }

    @Test
    public void testGetShellTextColor() throws CMException {
        when(shellFgColor.getValue()).thenReturn("#eeddff");
        assertNotNull(target.getShellTextColor());
        assertEquals("#eeddff", target.getShellTextColor());
    }

    @Test
    public void testSetShellTextColor() throws CMException {
        target.setShellTextColor("#eeddff");
        verify(shellFgColor).setValue("#eeddff");
    }

    @Test
    public void testGetTweetBackgroundColor() throws CMException {
        when(tweetBgColor.getValue()).thenReturn("#00eeff");
        assertNotNull(target.getTweetBackgroundColor());
        assertEquals("#00eeff", target.getTweetBackgroundColor());
    }

    @Test
    public void testSetTweetBackgroundColor() throws CMException {
        target.setTweetBackgroundColor("#00eeff");
        verify(tweetBgColor).setValue("#00eeff");
    }

    @Test
    public void testGetTweetTextColor() throws CMException {
        when(tweetFgColor.getValue()).thenReturn("#ffffff");
        assertNotNull(target.getTweetTextColor());
        assertEquals("#ffffff", target.getTweetTextColor());
    }

    @Test
    public void testSetTweetTextColor() throws CMException {
        target.setTweetTextColor("#ffffff");
        verify(tweetFgColor).setValue("#ffffff");
    }

    @Test
    public void testGetTweetLinkColor() throws CMException {
        when(tweetLinkColor.getValue()).thenReturn("#000000");
        assertNotNull(target.getTweetLinkColor());
        assertEquals("#000000", target.getTweetLinkColor());
    }

    @Test
    public void testSetTweetLinkColor() throws CMException {
        target.setTweetLinkColor("#000000");
        verify(tweetLinkColor).setValue("#000000");
    }

    @Test
    public void testGetHeight() throws CMException {
        when(height.getValue()).thenReturn("auto");
        assertNotNull(target.getHeight());
        assertEquals("auto", target.getHeight());
    }

    @Test
    public void testSetHeight() throws CMException {
        target.setHeight("500");
        verify(height).setValue("500");
    }

    @Test
    public void testGetWidth() throws CMException {
        when(width.getValue()).thenReturn("auto");
        assertNotNull(target.getWidth());
        assertEquals("auto", target.getWidth());
    }

    @Test
    public void testSetWidth() throws CMException {
        target.setWidth("300");
        verify(width).setValue("300");
    }

    @Test
    public void testGetAvatars() throws CMException {
        when(avatars.getValue()).thenReturn("true");
        assertNotNull(target.getAvatars());
        assertEquals("true", target.getAvatars());
    }

    @Test
    public void testSetAvatars() throws CMException {
        target.setAvatars(true);
        verify(avatars).setChecked(true);
    }

    @Test
    public void testGetScrollbar() throws CMException {
        when(scrollbar.getValue()).thenReturn("true");
        assertNotNull(target.getScrollbar());
        assertEquals("true", target.getScrollbar());
    }

    @Test
    public void testSetScrollbar() throws CMException {
        target.setScrollbar(true);
        verify(scrollbar).setChecked(true);
    }

    @Test
    public void testGetLoop() throws CMException {
        when(loop.getValue()).thenReturn("true");
        assertNotNull(target.getLoop());
        assertEquals("true", target.getLoop());
    }

    @Test
    public void testSetLoop() throws CMException {
        target.setLoop(true);
        verify(loop).setChecked(true);
    }
}
