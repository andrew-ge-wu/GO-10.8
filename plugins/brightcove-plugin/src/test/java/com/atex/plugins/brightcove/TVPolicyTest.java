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
import java.util.List;
import java.util.ListIterator;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.app.policy.SelectableSubFieldPolicy;
import com.polopoly.cm.app.search.categorization.CategorizationProvider;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListProvider;
import com.polopoly.cm.policy.ContentListWrapper;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

/**
 *
 */
public class TVPolicyTest {
    
    TVPolicy target;
    
    @Mock
    PolicyCMServer cmServer;
    @Mock
    ContentListWrapper contentListWrapper;
    @Mock
    ContentList publishingQueues;
    @Mock
    ContentReference publishingQueueReference1;
    @Mock
    ContentId publishingQueues1;
    @Mock
    FakeContentListProvider contentList;
    @Mock
    ContentList videos;
    @Mock
    Content content;
    @Mock
    InputTemplate inputTemplate;
    @Mock
    Policy parent;
    @Mock
    ContentList list;
    @Mock
    ContentReference listElementReference;
    @Mock
    ContentId listElementId;
    @Mock
    BrightcoveVideoPolicy brightcoveVideoPolicy;
    @Mock
    ContentList videoList;
    @Mock
    ListElementPolicy listElementPolicy;
    @Mock
    SelectableSubFieldPolicy selectablePolicy;
    @Mock
    ContentList relatedElements;
    @Mock
    ListIterator<ContentReference> iterator;
    @Mock
    ContentReference relatedCf;
    @Mock
    ContentId relatedId;

    @Before
    public void before() throws CMException {
        MockitoAnnotations.initMocks(this);

        when(contentListWrapper.getWrappedContentList()).thenReturn(publishingQueues);
        List<BrightcoveVideoPolicy> videoList = new ArrayList<BrightcoveVideoPolicy>();
        videoList.add(brightcoveVideoPolicy);
        when(listElementPolicy.getVideos()).thenReturn(videoList);
        target = spy(new TVPolicy() {
            @Override
            protected void initSelf() {}
        });
        target.init("PolicyName", new Content[] { content }, inputTemplate, parent, cmServer);
    }
    
    
    @Test
    public void shouldAbleGetVideos() {
        doReturn(listElementPolicy).when(target).getFirstBcListEle();
         List<BrightcoveVideoPolicy> c = target.getVideos();
         assertEquals(1, c.size());
    }

    @Test
    public void shouldNotGetVideos() {
        doReturn(null).when(target).getFirstBcListEle();
         List<BrightcoveVideoPolicy> c = target.getVideos();
         assertEquals(0, c.size());
    }

    @Test
    public void shouldNullGetFirstBcListEleEmpty() throws CMException {
        doReturn(videoList).when(target).getContentList(TVPolicy.CONTENT_LIST_NAME);
        when(videoList.size()).thenReturn(0);
        assertNull(target.getFirstBcListEle());
    }

    @Test
    public void shouldNullGetFirstBcListEleNotInstance() throws CMException {
        doReturn(videoList).when(target).getContentList(TVPolicy.CONTENT_LIST_NAME);
        when(videoList.size()).thenReturn(1);
        when(videoList.getEntry(0)).thenReturn(listElementReference);
        when(cmServer.getPolicy(listElementId)).thenReturn(null);
        assertNull(target.getFirstBcListEle());
    }

    @Test
    public void shouldGetFirstBcListEle() throws CMException {
        doReturn(videoList).when(target).getContentList(TVPolicy.CONTENT_LIST_NAME);
        when(videoList.size()).thenReturn(1);
        when(videoList.getEntry(0)).thenReturn(listElementReference);
        when(listElementReference.getReferredContentId()).thenReturn(listElementId);
        when(cmServer.getPolicy(listElementId)).thenReturn(listElementPolicy);
        assertNotNull(target.getFirstBcListEle());
    }

    @Test
    public void shouldCMExceptionGetFirstBcListEle() throws CMException {
        doThrow(new CMException("Unit Test Exception")).when(target).getContentList(TVPolicy.CONTENT_LIST_NAME);
        assertNull(target.getFirstBcListEle());
    }

    @Test
    public void shouldNullGetSelectedCategorizationOption() throws CMException {
        doThrow(new CMException("Unit Test Exception")).when(target).getChildPolicy("categorizationOption");
        assertNull(target.getSelectedCategorizationOption());
        doReturn(null).when(target).getChildPolicy("categorizationOption");
        assertNull(target.getSelectedCategorizationOption());
    }

    @Test
    public void shouldGetSelectedCategorizationOption() throws CMException {
        String expected = "selectedOption";
        doReturn(selectablePolicy).when(target).getChildPolicy("categorizationOption");
        when(selectablePolicy.getSelectedSubFieldName()).thenReturn(expected);
        assertNotNull(target.getSelectedCategorizationOption());
        assertEquals(expected, target.getSelectedCategorizationOption());
    }

    @Test
    public void shouldGetCategorization() throws CMException {
        CategorizationProvider cateProvider = mock(CategorizationProvider.class);
        doReturn(cateProvider).when(target).getCategorizationProvider();
        assertNull(target.getCategorization());
    }

    @Test
    public void shouldGetRelatedElementId() throws CMException {
        doReturn(relatedElements).when(target).getContentList(TVPolicy.SLOT_ELEMENTS);
        when(relatedElements.getListIterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true);
        when(iterator.next()).thenReturn(relatedCf);
        when(relatedCf.getReferredContentId()).thenReturn(relatedId);
        assertEquals(relatedId, target.getRelatedElementId());
    }

    @Test
    public void shouldNullGetRelatedElementId() throws CMException {
        doThrow(new CMException("Unit Test Exception")).when(target).getContentList(TVPolicy.SLOT_ELEMENTS);
        assertNull(target.getRelatedElementId());
        doReturn(relatedElements).when(target).getContentList(TVPolicy.SLOT_ELEMENTS);
        when(relatedElements.getListIterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(false);
        assertNull(target.getRelatedElementId());
    }

    class FakeContentListProvider extends BrightcoveVideoPolicy implements Policy, ContentListProvider {
        
    }

}
