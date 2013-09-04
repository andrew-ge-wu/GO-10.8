/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.atex.plugins.brightcove.BrightcoveConfigPolicy;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.siteengine.structure.PagePolicy;

public class ConfigurationUtilTest {

    ConfigurationUtil target;

    @Mock
    PolicyCMServer cmServer;
    @Mock
    BrightcoveConfigPolicy configPolicy;
    @Mock
    ContentId contentId;
    @Mock
    ContentRead contentRead;

    String siteId = "siteId";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        target = spy(new ConfigurationUtil(cmServer));
    }

    @Test
    public void shouldGetConfigReadToken() throws CMException {
        String expected = "token";
        doReturn(configPolicy).when(target).getBrightcoveConfigPolicy();
        doReturn(siteId).when(target).getCurrentRootSiteId(contentId);
        when(configPolicy.getReadToken(siteId)).thenReturn(expected);
        assertEquals(expected, target.getConfigReadToken(contentId));
        doThrow(new CMException("Unit test CME")).when(target).getBrightcoveConfigPolicy();
        assertNull(target.getConfigReadToken(contentId));
    }

    @Test
    public void shouldGetConfigPlayerIdToken() throws CMException {
        String expected = "playId";
        doReturn(configPolicy).when(target).getBrightcoveConfigPolicy();
        doReturn(siteId).when(target).getCurrentRootSiteId(contentId);
        when(configPolicy.getPreviewPlayerId(siteId)).thenReturn(expected);
        assertEquals(expected, target.getConfigPlayerId(contentId));
        doThrow(new CMException("Unit test CME")).when(target).getBrightcoveConfigPolicy();
        assertNull(target.getConfigPlayerId(contentId));
    }

    @Test
    public void shouldGetConfigWebTVDepartment() throws CMException {
            ContentId expected = mock(ContentId.class);
            doReturn(configPolicy).when(target).getBrightcoveConfigPolicy();
            doReturn(siteId).when(target).getCurrentRootSiteId(contentId);
            when(configPolicy.getDepartment(siteId)).thenReturn(expected);
            assertEquals(expected, target.getConfigWebTVDepartment(contentId));
            doThrow(new CMException("Unit test CME")).when(target).getBrightcoveConfigPolicy();
            assertNull(target.getConfigWebTVDepartment(contentId));

    }

    @Test
    public void shouldGetCurrentRootSiteId() throws CMException {
        ContentId expectedId = new ContentId(2, 789);
        ContentId parentId = new ContentId(2, 543);
        PagePolicy pagePolicy = mock(PagePolicy.class);
        ContentId[] emptyIds = {};
        when(cmServer.getContent(contentId)).thenReturn(contentRead);
        when(contentRead.getSecurityParentId()).thenReturn(parentId);
        assertNull(target.getCurrentRootSiteId(contentId));
        when(cmServer.getPolicy(parentId)).thenReturn(pagePolicy);
        when(pagePolicy.getParentIds()).thenReturn(emptyIds);
        assertEquals("2.543", target.getCurrentRootSiteId(contentId));
        ContentId[] parentIds = { expectedId, parentId };
        when(pagePolicy.getParentIds()).thenReturn(parentIds);
        assertEquals("2.789", target.getCurrentRootSiteId(contentId));
    }

    @Test
    public void shouldGetNullCurrentRootSiteIdWithCME() throws CMException {
        when(cmServer.getContent(contentId)).thenThrow(new CMException("Unit test CME"));
        assertNull(target.getCurrentRootSiteId(contentId));
    }

    @Test
    public void shouldGetBrightcoveConfigPolicy() throws CMException {
        when(cmServer.getPolicy(BrightcoveConfigPolicy.CONTENT_ID)).thenReturn(configPolicy);
        assertEquals(configPolicy, target.getBrightcoveConfigPolicy());
    }
}
