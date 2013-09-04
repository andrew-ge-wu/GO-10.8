/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

public class BrightcoveConfigPolicyTest {

    BrightcoveConfigPolicy target;

    private HashMap<String, Policy> children = new HashMap<String, Policy>();

    @Mock
    Content content;
    @Mock
    InputTemplate inputTemplate;
    @Mock
    Policy parent;
    @Mock
    PolicyCMServer cmServer;

    @Mock
    BrightcoveSiteConfigPolicy bcSiteConfigPolicy;
    @Mock
    ContentList contentList;
    @Mock
    ContentReference contentRef;
    @Mock
    ContentId cfgContentId;
    @Mock
    ContentId siteContentId;

    Map<String, SiteConfigPolicy> configs ;
    List<String> siteIds ;
    String siteId = "siteId";
    String siteNoConfig = "noSuchConfig";

    @Before
    public void setUp() throws CMException {
        MockitoAnnotations.initMocks(this);
        target = spy(new BrightcoveConfigPolicy() {
            @Override
            protected synchronized void initChildPolicies() {
                this.childPolicies = children;
            }
        });
        target.init("policy", new Content[] { content }, inputTemplate, parent, cmServer);
        doReturn(bcSiteConfigPolicy).when(target).getConfigPolicy(siteId);
        doReturn(null).when(target).getConfigPolicy(siteNoConfig);
        siteIds = new ArrayList<String>();
        configs = new LinkedHashMap<String, SiteConfigPolicy>();
    }

    @Test
    public void shouldGetSiteId() throws CMException {
        doReturn(siteIds).when(target).getSiteIds();
        assertNull(target.getSiteId());
        siteIds.add(siteId);
        assertEquals(siteId, target.getSiteId());
    }

    @Test
    public void shouldGetSiteIds() throws CMException {
        String siteId1 = "siteId1";
        String siteId2 = "siteId2";
        configs.put(siteId1, bcSiteConfigPolicy);
        configs.put(siteId2, bcSiteConfigPolicy);
        doReturn(configs).when(target).getConfigPolicies();
        assertEquals(siteId1, target.getSiteIds().get(0));
        assertEquals(siteId2, target.getSiteIds().get(1));
    }

    @Test
    public void shouldGetConfigPolicies() throws CMException {
        doThrow(new CMException("Unit test CME")).when(target).getContentList(BrightcoveConfigPolicy.CONFIG_LIST);
        assertTrue(target.getConfigPolicies().isEmpty());
        doReturn(contentList).when(target).getContentList(BrightcoveConfigPolicy.CONFIG_LIST);
        assertTrue(target.getConfigPolicies().isEmpty());
        when(contentList.size()).thenReturn(1);
        when(contentList.getEntry(0)).thenReturn(contentRef);
        when(contentRef.getReferredContentId()).thenReturn(cfgContentId);
        when(cmServer.getPolicy(cfgContentId)).thenReturn(bcSiteConfigPolicy);
        when(bcSiteConfigPolicy.getSite()).thenReturn(siteContentId);
        assertEquals(1, target.getConfigPolicies().size());
        assertEquals(bcSiteConfigPolicy, target.getConfigPolicies().get(siteContentId.getContentIdString()));
    }

    @Test
    public void shouldGetConfigPolicy() throws CMException {
        doReturn(siteId).when(target).getSiteId();
        assertEquals(bcSiteConfigPolicy, target.getConfigPolicy());
    }

    @Test
    public void shouldGetConfigPolicyWithArg() {
        doReturn(configs).when(target).getConfigPolicies();
        assertNull(target.getConfigPolicy(""));
        configs.put("siteId2", bcSiteConfigPolicy);
        doReturn("siteId2").when(target).getSiteId();
        assertEquals(bcSiteConfigPolicy, target.getConfigPolicy("notFound"));
        doReturn("siteId2").when(target).getSiteId();
        assertEquals(bcSiteConfigPolicy, target.getConfigPolicy(null));
    }

    @Test
    public void shouldGetPublisherId() throws CMException {
        String expected = "publisherId";
        when(bcSiteConfigPolicy.getPublisherId()).thenReturn(expected);
        doReturn(siteId).when(target).getSiteId();
        assertEquals(expected, target.getPublisherId());
        assertEquals("", target.getPublisherId(siteNoConfig));
    }

    @Test
    public void shouldGetReadToken() throws CMException {
        String expected = "readToken";
        when(bcSiteConfigPolicy.getReadToken()).thenReturn(expected);
        doReturn(siteId).when(target).getSiteId();
        assertEquals(expected, target.getReadToken());
        assertEquals("", target.getReadToken(siteNoConfig));
    }

    @Test
    public void shouldGetReadTokenUrl() throws CMException {
        String expected = "readTokenUrl";
        when(bcSiteConfigPolicy.getReadTokenUrl()).thenReturn(expected);
        doReturn(siteId).when(target).getSiteId();
        assertEquals(expected, target.getReadTokenUrl());
        assertEquals("", target.getReadTokenUrl(siteNoConfig));
    }

    @Test
    public void shouldGetWriteToken() throws CMException {
        String expected = "writeToken";
        when(bcSiteConfigPolicy.getWriteToken()).thenReturn(expected);
        doReturn(siteId).when(target).getSiteId();
        assertEquals(expected, target.getWriteToken());
        assertEquals("", target.getWriteToken(siteNoConfig));
    }

    @Test
    public void shouldGetPreviewPlayerId() throws CMException {
        String expected = "previewPlayerId";
        when(bcSiteConfigPolicy.getPreviewPlayerId()).thenReturn(expected);
        doReturn(siteId).when(target).getSiteId();
        assertEquals(expected, target.getPreviewPlayerId());
        assertEquals("", target.getPreviewPlayerId(siteNoConfig));
    }

    @Test
    public void shouldGetDepartment() throws CMException {
        ContentId expected = mock(ContentId.class);
        when(bcSiteConfigPolicy.getDepartment()).thenReturn(expected);
        doReturn(siteId).when(target).getSiteId();
        assertEquals(expected, target.getDepartment());
        assertNull(target.getDepartment(siteNoConfig));
    }

    @Test
    public void shouldReturnMappingWhenIsEnabled() throws CMException {
        Map<String, String> mapping = new LinkedHashMap<String, String>();
        when(bcSiteConfigPolicy.getMappings()).thenReturn(mapping);
        doReturn(siteId).when(target).getSiteId();
        assertEquals(mapping, target.getMappings());
        assertEquals(mapping, target.getMappings(siteNoConfig));
    }

}
