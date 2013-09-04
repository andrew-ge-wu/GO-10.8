/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove;

import java.util.HashMap;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.policy.CheckboxPolicy;
import com.polopoly.cm.app.policy.ContentTreeSelectPolicy;
import com.polopoly.cm.app.policy.NameValuePolicy;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BrightcoveSiteConfigPolicyTest {
    @Mock
    private PolicyCMServer cmServer;
    @Mock
    private Content content;
    @Mock
    private InputTemplate inputTemplate;
    @Mock
    private Policy parent;
    @Mock
    private HashMap<String, Policy> children;

    @Mock
    private ContentTreeSelectPolicy site;
    @Mock
    private SingleValuePolicy publisherId;
    @Mock
    private SingleValuePolicy readToken;
    @Mock
    private SingleValuePolicy readTokenUrl;
    @Mock
    private SingleValuePolicy writeToken;
    @Mock
    private SingleValuePolicy previewPlayerId;
    @Mock
    private ContentTreeSelectPolicy department;
    @Mock
    private CheckboxPolicy mapCustomFields;
    @Mock
    private NameValuePolicy customFields;

    private BrightcoveSiteConfigPolicy target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        target = new BrightcoveSiteConfigPolicy() {
            @Override
            protected synchronized void initChildPolicies() throws CMException {
                this.childPolicies = children;
            }
        };
        target.init("policy", new Content[] { content }, inputTemplate, parent, cmServer);

        when(children.get(BrightcoveSiteConfigPolicy.SITE)).thenReturn(site);
        when(children.get(BrightcoveSiteConfigPolicy.PUBLISHER_ID)).thenReturn(publisherId);
        when(children.get(BrightcoveSiteConfigPolicy.READ_TOKEN)).thenReturn(readToken);
        when(children.get(BrightcoveSiteConfigPolicy.READ_TOKEN_URL)).thenReturn(readTokenUrl);
        when(children.get(BrightcoveSiteConfigPolicy.WRITE_TOKEN)).thenReturn(writeToken);
        when(children.get(BrightcoveSiteConfigPolicy.PREVIEW_PLAYER_ID)).thenReturn(previewPlayerId);
        when(children.get(BrightcoveSiteConfigPolicy.DEPARTMENT)).thenReturn(department);
        when(children.get(BrightcoveSiteConfigPolicy.MAP_CUSTOM_FIELD)).thenReturn(mapCustomFields);
        when(children.get(BrightcoveSiteConfigPolicy.CUSTOM_FIELDS)).thenReturn(customFields);
    }

    @After
    public void tearDown() throws Exception {
        target = null;
    }

    @Test
    public void testGetSite() throws CMException {
        ContentId expected = mock(ContentId.class);
        when(site.getReference()).thenReturn(expected);
        assertNotNull(target.getSite());
        assertEquals(expected, target.getSite());
    }

    @Test
    public void testGetPublisherId() throws CMException {
        String expected = "PublishID000001";
        when(publisherId.getValue()).thenReturn(expected);
        assertNotNull(target.getPublisherId());
        assertEquals(expected, target.getPublisherId());
    }

    @Test
    public void testReadToken() throws CMException {
        String expected = "ReadToken000001";
        when(readToken.getValue()).thenReturn(expected);
        assertNotNull(target.getReadToken());
        assertEquals(expected, target.getReadToken());
    }

    @Test
    public void testGetReadTokenUrl() throws CMException {
        String expected = "ReadTokenUrl000001";
        when(readTokenUrl.getValue()).thenReturn(expected);
        assertNotNull(target.getReadTokenUrl());
        assertEquals(expected, target.getReadTokenUrl());
    }

    @Test
    public void testGetWriteToken() throws CMException {
        String expected = "WriteToken000001";
        when(writeToken.getValue()).thenReturn(expected);
        assertNotNull(target.getWriteToken());
        assertEquals(expected, target.getWriteToken());
    }

    @Test
    public void testGetPreviewPlayerId() throws CMException {
        String expected = "prvPlayerId000001";
        when(previewPlayerId.getValue()).thenReturn(expected);
        assertNotNull(target.getPreviewPlayerId());
        assertEquals(expected, target.getPreviewPlayerId());
    }

    @Test
    public void testGetDepartment() throws CMException {
        ContentId expected = mock(ContentId.class);
        when(department.getReference()).thenReturn(expected);
        assertNotNull(target.getDepartment());
        assertEquals(expected, target.getDepartment());
    }

    @Test
    public void testSetPublisherId() throws CMException {
        String value = "PublishID000001";
        target.setPublisherId(value);
        verify(publisherId).setValue(value);
    }

    @Test
    public void testSetReadToken() throws CMException {
        String value = "ReadToken000001";
        target.setReadToken(value);
        verify(readToken).setValue(value);
    }

    @Test
    public void testSetReadTokenUrl() throws CMException {
        String value = "ReadTokenUrl000001";
        target.setReadTokenUrl(value);
        verify(readTokenUrl).setValue(value);
    }

    @Test
    public void testSetWriteToken() throws CMException {
        String value = "WriteToken000001";
        target.setWriteToken(value);
        verify(writeToken).setValue(value);
    }

    @Test
    public void shouldReturnEmptyMappingWhenIsNotEnabled() throws CMException {
        when(mapCustomFields.getChecked()).thenReturn(Boolean.FALSE);
        assertEquals(0, target.getMappings().size());
        verify(customFields, never()).getNamesAndValues();
    }

    @Test
    public void shouldReturnMappingWhenIsEnabled() throws CMException {
        when(mapCustomFields.getChecked()).thenReturn(Boolean.TRUE);
        assertEquals(0, target.getMappings().size());
        verify(customFields).getNamesAndValues();
    }
}
