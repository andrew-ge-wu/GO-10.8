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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.policy.ContentListWrapperPolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListRead;
import com.polopoly.cm.policy.ContentListWrapperDefinition;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

public class ExtendedConfigurableContentListWrapperTest {

    private ExtendedConfigurableContentListWrapper target;

    @Mock
    PolicyCMServer cmServer;

    @Mock
    ContentListWrapperDefinition wrapperDefinition;

    @Mock
    ContentId referredContentId;

    @Mock
    ContentRead inputTemplateContent;

    ExternalContentId templateExternalId;

    static final String VIDEORESOURCE_TEMPLATE_EXTID = "com.atex.plugins.brightcove.videoresource";

    @Before
    public void setUp() throws Exception {
        templateExternalId = new ExternalContentId(VIDEORESOURCE_TEMPLATE_EXTID);
        MockitoAnnotations.initMocks(this);
        target = spy(new ExtendedConfigurableContentListWrapper() {
        });
        doReturn(inputTemplateContent).when(target).getContent(any(ContentId.class));
        when(inputTemplateContent.getExternalId()).thenReturn(templateExternalId);
        doReturn(wrapperDefinition).when(target).getWrapperDefinition();
        when(wrapperDefinition.getPolicyCMServer()).thenReturn(cmServer);
        target.initSelf();
    }

    @Test
    public void shouldAddAllow() throws CMException {
        ContentId contentItId = mock(ContentId.class);
        ContentId itId = mock(ContentId.class);
        when(inputTemplateContent.getInputTemplateId()).thenReturn(contentItId);
        ContentListRead externalIds = mock(ContentListRead.class);
        ContentReference cf = mock(ContentReference.class);
        doReturn(externalIds).when(target).getAllowedInputTemplates();
        when(externalIds.size()).thenReturn(1);
        when(externalIds.getEntry(0)).thenReturn(cf);
        when(cf.getReferredContentId()).thenReturn(itId);
        ContentRead inputTemplate = mock(ContentRead.class);
        when(cmServer.getContent(itId)).thenReturn(inputTemplate);
        ExternalContentId extId = new ExternalContentId(VIDEORESOURCE_TEMPLATE_EXTID);
        when(inputTemplate.getExternalId()).thenReturn(extId);
        assertTrue(target.isAddAllowed(referredContentId));
    }

    @Test
    public void shouldNotAddAllowedNotInList() throws CMException {
        ContentId contentItId = mock(ContentId.class);
        ContentId itId = mock(ContentId.class);
        when(inputTemplateContent.getInputTemplateId()).thenReturn(contentItId);
        ContentListRead externalIds = mock(ContentListRead.class);
        ContentReference cf = mock(ContentReference.class);
        doReturn(externalIds).when(target).getAllowedInputTemplates();
        when(externalIds.size()).thenReturn(1);
        when(externalIds.getEntry(0)).thenReturn(cf);
        when(cf.getReferredContentId()).thenReturn(itId);
        ContentRead inputTemplate = mock(ContentRead.class);
        when(cmServer.getContent(itId)).thenReturn(inputTemplate);
        ExternalContentId extId = new ExternalContentId("com.atex.plugins.something.else");
        when(inputTemplate.getExternalId()).thenReturn(extId);
        assertFalse(target.isAddAllowed(referredContentId));
    }

    @Test
    public void shouldNotAddAllowedEmptyList() throws CMException {
        ContentId contentItId = mock(ContentId.class);
        when(inputTemplateContent.getInputTemplateId()).thenReturn(contentItId);
        ContentListRead externalIds = mock(ContentListRead.class);
        doReturn(externalIds).when(target).getAllowedInputTemplates();
        assertFalse(target.isAddAllowed(referredContentId));
    }

    @Test
    public void shouldNotAddAllowedNullList() throws CMException {
        ContentId contentItId = mock(ContentId.class);
        when(inputTemplateContent.getInputTemplateId()).thenReturn(contentItId);
        doReturn(null).when(target).getAllowedInputTemplates();
        assertFalse(target.isAddAllowed(referredContentId));
    }

    @Test
    public void shouldGetAllowedInputTemplates() {
        ContentListRead cl = mock(ContentListRead.class);
        doReturn(cl).when(target).getAllAllowedIT();
        assertNotNull(target.getAllowedInputTemplates());
    }

    @Test
    public void shouldGetPqListEntryExtId() {
        String expected = "com.atex.plugins.publishingqueue.extid" ;
        ExternalContentId extId = new ExternalContentId(expected);
        assertEquals(expected, target.getPqListEntryExtId(extId));
    }

    @Test
    public void shouldGetPqListEntryExtIdBlankString() {
        ExternalContentId extId = new ExternalContentId("");
        assertNull(target.getPqListEntryExtId(extId));
    }

    @Test
    public void shouldGetPqListEntryExtIdNull() {
        String expected = null ;
        ExternalContentId extId = new ExternalContentId(expected);
        assertNull(target.getPqListEntryExtId(null));
        assertNull(target.getPqListEntryExtId(extId));
    }

    @Test
    public void shouldGetAllAvailablePqs() throws CMException {
        ExternalContentId pqTopExtId = new ExternalContentId(ExtendedConfigurableContentListWrapper.TOP_PQ_LIST_EXT_ID);
        Policy topPqPolicy = mock(Policy.class);
        ContentListWrapperPolicy clWrapperPolicy = mock(ContentListWrapperPolicy.class);
        ContentList pqList = mock(ContentList.class);
        ContentReference cRef = mock(ContentReference.class);
        Policy entryPolicy = mock(Policy.class);
        Content entryContent = mock(Content.class);
        ExternalContentId entryExtId = new ExternalContentId("com.atex.plugins.publishingqueue.anypq");
        when(cmServer.getPolicy(pqTopExtId)).thenReturn(topPqPolicy);
        when(topPqPolicy.getChildPolicy("templateList")).thenReturn(clWrapperPolicy);
        when(clWrapperPolicy.getContentList()).thenReturn(pqList);
        when(pqList.size()).thenReturn(1);
        when(pqList.getEntry(0)).thenReturn(cRef);
        when(cRef.getReferredContentId()).thenReturn(entryExtId.getContentId());
        when(cmServer.getPolicy(entryExtId.getContentId())).thenReturn(entryPolicy);
        when(entryPolicy.getContent()).thenReturn(entryContent);
        when(entryContent.getExternalId()).thenReturn(entryExtId);
        assertEquals(entryExtId, target.getAvailablePqs().get(0));
        when(entryContent.getExternalId()).thenReturn(null);
        assertEquals(0, target.getAvailablePqs().size());
    }

    @Test
    public void shouldEmptyGetAllAvailablePqs() throws CMException {
        ExternalContentId pqTopExtId = new ExternalContentId("p.siteengine.PublishingQueueContentRepositoryTemplates");
        Policy topPqPolicy = mock(Policy.class);
        ContentListWrapperPolicy clWrapperPolicy = mock(ContentListWrapperPolicy.class);
        ContentList pqList = mock(ContentList.class);
        when(cmServer.getPolicy(pqTopExtId)).thenReturn(topPqPolicy);
        when(topPqPolicy.getChildPolicy("templateList")).thenReturn(clWrapperPolicy);
        when(clWrapperPolicy.getContentList()).thenReturn(null);
        when(pqList.size()).thenReturn(0);
        assertEquals(0, target.getAvailablePqs().size());
        when(clWrapperPolicy.getContentList()).thenReturn(pqList);
        when(pqList.size()).thenReturn(0);
        assertEquals(0, target.getAvailablePqs().size());
    }

    @Test
    public void shouldEmptyGetAllAvailablePqsCMException() throws CMException {
        ExternalContentId pqTopExtId = new ExternalContentId(ExtendedConfigurableContentListWrapper.TOP_PQ_LIST_EXT_ID);
        when(cmServer.getPolicy(pqTopExtId)).thenThrow(new CMException("Unit Test CMException"));
        assertEquals(0, target.getAvailablePqs().size());
    }

    @Test
    public void shouldGetAllAllowedIT() throws CMException{
        ContentId parentEntryId = mock(ContentId.class);
        ContentListRead parentAllowedPqs = mock(ContentListRead.class);
        ContentReference cRef = mock(ContentReference.class);
        when(parentAllowedPqs.size()).thenReturn(1);
        when(parentAllowedPqs.getEntry(0)).thenReturn(cRef);
        when(cRef.getReferredContentId()).thenReturn(parentEntryId);
        doReturn(parentAllowedPqs).when(target).getParentAllowedITList();
        ContentId pqId = mock(ContentId.class);
        List<ContentId> allowedPqs = new ArrayList<ContentId>();
        allowedPqs.add(pqId);
        doReturn(allowedPqs).when(target).getAvailablePqs();
        assertEquals(2, target.getAllAllowedIT().size());
    }

    @Test
    public void shouldEmptyGetAllAllowedITCMException() throws CMException {
        ContentListRead parentAllowedPqs = mock(ContentListRead.class);
        doReturn(parentAllowedPqs).when(target).getParentAllowedITList();
        when(parentAllowedPqs.size()).thenReturn(1);
        when(parentAllowedPqs.getEntry(0)).thenThrow(new CMException("Unit test CMException"));
        doReturn(new ArrayList<ContentId>()).when(target).getAvailablePqs();
        assertEquals(0, target.getAllAllowedIT().size());
    }

    @Test
    public void shouldEmptyGetAllAllowedIT() {
        doReturn(new ArrayList<ContentId>()).when(target).getAvailablePqs();
        assertEquals(0, target.getAllAllowedIT().size());
    }

    @Test
    public void shouldGetContent() throws CMException {
        ContentRead cRead = mock(ContentRead.class);
        ContentId cid = mock(ContentId.class);
        when(cmServer.getContent(cid)).thenReturn(cRead);
        assertNotNull(target.getContent(cid));
    }

}