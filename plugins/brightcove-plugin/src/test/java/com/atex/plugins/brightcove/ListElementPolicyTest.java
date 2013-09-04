/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove;


import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.app.policy.SingleReference;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListProvider;
import com.polopoly.cm.policy.ContentListWrapper;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

/**
 *
 */
public class ListElementPolicyTest {
    
    ListElementPolicy target;

    @Mock
    HashMap<String, Policy> children = new HashMap<String, Policy>();
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
    ContentReference videoReference1;
    @Mock
    ContentId videoId1;
    @Mock
    ContentReference videoReference2;
    @Mock
    ContentId videoId2;
    @Mock
    BrightcoveVideoPolicy brightcoveVideoPolicy;
    @Mock
    SingleValuePolicy maxVideoChildPolicy;
    @Mock
    FakeSingleReference departmentChildPolicy;
    
    
    @Before
    public void before() throws CMException {
        MockitoAnnotations.initMocks(this);
        when(contentListWrapper.getWrappedContentList()).thenReturn(publishingQueues);
        when(publishingQueues.size()).thenReturn(1);
        when(publishingQueues.getEntry(0)).thenReturn(publishingQueueReference1);
        when(publishingQueueReference1.getReferredContentId()).thenReturn(publishingQueues1);
        when(cmServer.getPolicy(publishingQueues1)).thenReturn(contentList);
        when(contentList.getContentList()).thenReturn(list);
        when(list.size()).thenReturn(2);
        when(list.getEntry(0)).thenReturn(videoReference1);
        when(videoReference1.getReferredContentId()).thenReturn(videoId1);
        when(list.getEntry(1)).thenReturn(videoReference2);
        when(videoReference2.getReferredContentId()).thenReturn(videoId2);
        target = spy(new ListElementPolicy() {
            @Override
            protected void initChildPolicies() {
                this.childPolicies = children;
            }
            @Override
            protected void initSelf() {}
        });
        target.init("PolicyName", new Content[] { content }, inputTemplate, parent, cmServer);
        doReturn(contentListWrapper).when(target).getContentList(TVPolicy.CONTENT_LIST_NAME);
        when(children.get(ListElementPolicy.MAX_VIDEO)).thenReturn(maxVideoChildPolicy);
        when(maxVideoChildPolicy.getValue()).thenReturn("2");
        when(children.get(ListElementPolicy.SELECTED_DEPT)).thenReturn(departmentChildPolicy);
    }
    
    
    @Test
    public void shouldAbleReturnAllVideo() {
         List<BrightcoveVideoPolicy> c = target.getVideos();
         assertEquals(2, c.size());
    }

    @Test
    public void shouldAbleReturnOneVideoWhichIsLimitByMax() throws CMException {
         when(maxVideoChildPolicy.getValue()).thenReturn("1");
         List<BrightcoveVideoPolicy> c = target.getVideos();
         assertEquals(1, c.size());
    }

    @Test
    public void shouldReturnNullWhenFailedToGetDisplayLocation() throws CMException {
        doThrow(new CMException("Unit Test generated Exception, ignore it!")).when(target).getChildPolicy(ListElementPolicy.DISPLAY);
        assertNull(target.getDisplayLocation());
    }

    @Test
    public void shouldReturnNullWhenFailedToGetSelectedDepartment() throws CMException {
        doThrow(new CMException("Unit Test generated Exception, ignore it!")).when(departmentChildPolicy).getReference();
        assertNull(target.getSelectedDepartment());
    }
    
    class FakeContentListProvider extends BrightcoveVideoPolicy implements Policy, ContentListProvider {
        
    }

    interface FakeSingleReference extends SingleReference, Policy {

    }

}
