/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove.widget;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.atex.plugins.brightcove.BrightcoveConfigPolicy;
import com.atex.plugins.brightcove.BrightcoveVideoPolicy;
import com.atex.plugins.brightcove.util.ConfigurationUtil;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.ContentSession;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.impl.OrchidContextImpl;
import com.polopoly.orchid.session.impl.FrameStateImpl;
import com.polopoly.orchid.widget.OTextOutput;
import com.polopoly.siteengine.standard.content.ContentBasePolicy;

public class OBrightCoveVideoViewerTest {

    @Mock
    private OrchidContextImpl oc;

    @Mock
    private Device device;

    private OBrightCoveVideoViewer target;

    private ContentBasePolicy fieldPolicy;

    private static final String videoId = "1234567890";

    private static final String playerId = "0987654321";

    @Mock
    FrameStateImpl frameState;

    @Mock
    private Content fieldContent;

    @Mock
    InputTemplate fieldInputTemplate;

    @Mock
    PolicyCMServer cmServer;

    @Mock
    private BrightcoveVideoPolicy parentPolicy;

    @Mock
    BrightcoveConfigPolicy configPolicy;
    @Mock
    ConfigurationUtil configUtil;

    @Mock
    ContentSession contentSession;

    String siteId = "siteId";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(oc.getDevice()).thenReturn(device);
        when(oc.getFrameState()).thenReturn(frameState);
        when(contentSession.getPolicyCMServer()).thenReturn(cmServer);
        when(cmServer.getPolicy(new ExternalContentId(OBrightCoveVideoViewer.BRIGHTCOVE_CONFIG))).thenReturn(configPolicy);
        when(configPolicy.getPreviewPlayerId()).thenReturn(playerId);

        fieldPolicy = new ContentBasePolicy() {
            @Override
            protected void initSelf() {
            }
        };
        fieldPolicy.init("PolicyName", new Content[] { fieldContent }, fieldInputTemplate, parentPolicy, cmServer);

        target = spy(new OBrightCoveVideoViewer());
        doReturn(fieldPolicy).when(target).getPolicy();
        doReturn(contentSession).when(target).getContentSession();
        target.initSelf(oc);
    }

    @Test
    public void testShouldSuccess() throws CMException, OrchidException, IOException {
        doReturn(videoId).when(target).getVideoId();
        doReturn(playerId).when(target).getPlayerId();
        target.localRender(oc);
        assertEquals(videoId, target.bcVideoId);
        assertEquals(playerId, target.bcPlayerId);
        assertFalse(target.showNothing);
    }

    @Test
    public void testGetElementIdWithException() throws CMException, OrchidException, IOException {
        doReturn(videoId).when(target).getVideoId();
        doReturn(playerId).when(target).getPlayerId();
        target.localRender(oc);
    }

    @Test
    public void testShowNothingTrue() throws CMException, OrchidException, IOException {
        doThrow(new CMException("Test")).when(target).getVideoId();
        doThrow(new CMException("Test")).when(target).getPlayerId();
        OTextOutput message = mock(OTextOutput.class);
        target.setMessage(message);
        target.localRender(oc);
        assertTrue(target.showNothing);
        verify(message).render(oc);
    }

    @Test
    public void shouldGetVideoParent() throws CMException {
        Policy policy = mock(Policy.class);
        doReturn(policy).when(target).getPolicy();
        when(policy.getParentPolicy()).thenReturn(parentPolicy);
        assertEquals(parentPolicy, target.getVideoParent());
    }

    @Test
    public void shouldGetVideoId() throws CMException {
        String expected = "videoId";
        doReturn(parentPolicy).when(target).getVideoParent();
        when(parentPolicy.getId()).thenReturn(expected);
        assertEquals(expected, target.getVideoId());
    }

    @Test
    public void shouldGetPlayerId() throws CMException {
        doReturn(cmServer).when(target).getCmServer();
        when(cmServer.getPolicy(any(ExternalContentId.class))).thenReturn(configPolicy);
        doReturn(siteId).when(target).getCurrentRootSiteId();
        when(configPolicy.getPreviewPlayerId(siteId)).thenReturn(playerId);
        assertEquals(playerId, target.getPlayerId());
    }

    @Test
    public void shouldGetCmServer() throws CMException {
        Policy fieldPolicy = mock(Policy.class);
        doReturn(fieldPolicy).when(target).getPolicy();
        when(fieldPolicy.getCMServer()).thenReturn(cmServer);
        assertEquals(cmServer, target.getCmServer());
    }

    @Test
    public void shouldGetCurrentRootSiteId() throws CMException {
        doReturn(cmServer).when(target).getCmServer();
        doReturn(configUtil).when(target).getConfiguration(cmServer);
        Policy topPolicy = mock(Policy.class);
        VersionedContentId id = mock(VersionedContentId.class);
        when(contentSession.getTopPolicy()).thenReturn(topPolicy);
        when(topPolicy.getContentId()).thenReturn(id);
        when(configUtil.getCurrentRootSiteId(id)).thenReturn(siteId);
        assertEquals(siteId, target.getCurrentRootSiteId());
    }
}
