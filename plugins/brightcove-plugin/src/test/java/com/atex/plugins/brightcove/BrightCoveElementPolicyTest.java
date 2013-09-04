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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListProvider;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

public class BrightCoveElementPolicyTest {

    private BrightCoveElementPolicy target ;

    private HashMap<String, Policy> children = new HashMap<String, Policy>();

    @Mock
    Content content;
    @Mock
    InputTemplate inputTemplate;
    @Mock
    Policy parent;
    @Mock
    PolicyCMServer cmServer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        children.put(BrightCoveElementPolicy.POLICY_VIDEO_ID, new SingleValuePolicyMock());
        children.put(BrightCoveElementPolicy.POLICY_PLAYER_ID, new SingleValuePolicyMock());

        target = spy(new BrightCoveElementPolicy() {
            @Override
            protected void initChildPolicies() {
                this.childPolicies = children;
            }

            @Override
            protected void initSelf() {
            }
        });
        target.init("PolicyName", new Content[] { content }, inputTemplate, parent, cmServer);
    }

    @Test
    public void testVideoId() throws CMException {
        String expected = "1234567890";
        target.setVideoId(expected);
        assertEquals(expected, target.getVideoId());
    }

    @Test
    public void testPlayerId() throws CMException {
        String expected = "1234567890";
        target.setPlayerId(expected);
        assertEquals(expected, target.getPlayerId());
    }

    @Test
    public void testGetVideoIdWithException() throws CMException {
        when(target.getChildPolicy(BrightCoveElementPolicy.POLICY_VIDEO_ID)).thenThrow(mock(CMException.class));
        target.setVideoId("");
        assertEquals("", target.getVideoId());
    }

    @Test
    public void testNullValue() throws CMException {
        children.put(BrightCoveElementPolicy.POLICY_VIDEO_ID, null);
        when(target.getChildPolicy(BrightCoveElementPolicy.POLICY_VIDEO_ID)).thenReturn(null);
        target.setVideoId("");
    }

    @Test
    public void testElementId() throws CMException {
        String expected = "7345";
        VersionedContentId vid = new VersionedContentId(new ContentId(7, 345), 0);
        when(target.getContentId()).thenReturn(vid);
        assertEquals(expected, target.getElementId());
    }

    @Test
    public void shouldGetPureVideoList() throws CMException {
        Collection<ContentId> ids = new ArrayList<ContentId>();
        ContentId id = mock(ContentId.class);
        ids.add(id);
        doReturn(ids).when(target).getRepresentedContent();
        assertEquals(0, target.getPureVideoList().size());
    }

    @Test
    public void shoudGetRepresentedContent() throws CMException {
        ContentReference cf = mock(ContentReference.class);
        ContentList cl = mock(ContentList.class);
        when(cl.size()).thenReturn(1);
        when(cl.getEntry(0)).thenReturn(cf);
        doReturn(cl).when(target).getContentList(BrightCoveElementPolicy.POLICY_VIDEO_LIST);
        assertEquals(1, target.getRepresentedContent().size());
    }

    @Test
    public void shouldGetEmptyRepresentedContent() throws CMException {
        doThrow(new CMException("Unit Test CMException")).when(target).getContentList(BrightCoveElementPolicy.POLICY_VIDEO_LIST);
        assertEquals(0, target.getRepresentedContent().size());
    }

    @Test
    public void testDrillInContentList() throws CMException {
        ContentReference cf = mock(ContentReference.class);
        ContentList cl = mock(ContentList.class);
        ContentId id = mock(ContentId.class);
        Policy policy = spy(new DummyContentListProvider());
        ContentListProvider providerPolicy = (ContentListProvider) policy;
        doReturn(false).when(target).isBrightcoveVideoResource(id);
        doReturn(true).when(target).isPublishingQueue(id);
        when(cmServer.getPolicy(id)).thenReturn(policy);
        when(cl.size()).thenReturn(1);
        when(cl.getEntry(0)).thenReturn(cf);
        doReturn(cl).when(providerPolicy).getContentList();
        assertEquals(0, target.drillInExtractBcVideo(id).size());
    }

    @Test
    public void testDrillInNullContentList() throws CMException {
        ContentId id = mock(ContentId.class);
        Policy policy = spy(new DummyContentListProvider());
        ContentListProvider providerPolicy = (ContentListProvider) policy;
        doReturn(false).when(target).isBrightcoveVideoResource(id);
        doReturn(true).when(target).isPublishingQueue(id);
        when(cmServer.getPolicy(id)).thenReturn(policy);
        doReturn(null).when(providerPolicy).getContentList();
        assertEquals(0, target.drillInExtractBcVideo(id).size());
    }

    @Test 
    public void testDrillInCMException() throws CMException {
        ContentId id = mock(ContentId.class);
        Policy policy = spy(new DummyContentListProvider());
        ContentListProvider providerPolicy = (ContentListProvider) policy;
        doReturn(false).when(target).isBrightcoveVideoResource(id);
        doReturn(true).when(target).isPublishingQueue(id);
        when(cmServer.getPolicy(id)).thenReturn(policy);
        doThrow(new CMException("Unit Test CMException")).when(providerPolicy).getContentList();
        assertEquals(0, target.drillInExtractBcVideo(id).size());
    }

    @Test
    public void testDrillInVideoResource() {
        ContentId vrId = mock(ContentId.class);
        doReturn(true).when(target).isBrightcoveVideoResource(vrId);
        assertEquals(1, target.drillInExtractBcVideo(vrId).size());
    }

    @Test
    public void shouldBeVideoResource() throws CMException {
        ContentId id = mock(ContentId.class);
        Policy policy = mock(BrightcoveVideoPolicy.class);
        when(cmServer.getPolicy(id)).thenReturn(policy);
        assertTrue(target.isBrightcoveVideoResource(id));
    }

    @Test
    public void shouldBePublishingQueue() throws CMException {
        ContentId id = mock(ContentId.class);
        Policy policy = new DummyContentListProvider();
        when(cmServer.getPolicy(id)).thenReturn(policy);
        assertTrue(target.isPublishingQueue(id));
    }

    @Test
    public void shouldBeOther() throws CMException {
        ContentId id = mock(ContentId.class);
        Policy policy = mock(Policy.class);
        when(cmServer.getPolicy(id)).thenReturn(policy);
        assertFalse(target.isBrightcoveVideoResource(id));
        assertFalse(target.isPublishingQueue(id));
    }

    @Test
    public void shouldBeOtherCMException() throws CMException {
        ContentId id = mock(ContentId.class);
        when(cmServer.getPolicy(id)).thenThrow(new CMException("Unit test CMException"));
        assertFalse(target.isBrightcoveVideoResource(id));
        assertFalse(target.isPublishingQueue(id));
    }

    private class SingleValuePolicyMock extends SingleValuePolicy {
        private String value;

        @Override
        public void setValue(String value) throws CMException {
            this.value = value;
        }

        @Override
        public String getValue() throws CMException {
            return value;
        }
    }

    class DummyContentListProvider extends ContentPolicy implements ContentListProvider{
    };
}
