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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.atex.plugins.brightcove.BrightCoveElementPolicy;
import com.atex.plugins.brightcove.BrightcoveConfigPolicy;
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

public class OBrightCoveViewerTest {

    @Mock
    private OrchidContextImpl oc;

    @Mock
    private Device device;

    private OBrightCoveViewer target;

    private BrightCoveElementPolicy fieldPolicy;

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
    private BrightCoveElementPolicy parentPolicy;

    @Mock
    ContentSession contentSession;
    @Mock
    ConfigurationUtil configUtil;
    @Mock
    BrightcoveConfigPolicy configPolicy;

    String siteId = "siteId";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(oc.getDevice()).thenReturn(device);
        when(oc.getFrameState()).thenReturn(frameState);

        fieldPolicy = new BrightCoveElementPolicy() {
            @Override
            protected void initSelf() {
            
            }

        };
        fieldPolicy.init("PolicyName", new Content[] { fieldContent }, fieldInputTemplate, parentPolicy, cmServer);

        target = spy(new OBrightCoveViewer());
        doReturn(fieldPolicy).when(target).getPolicy();
        target.initSelf(oc);
        doReturn(contentSession).when(target).getContentSession();
    }

    @Test
    public void testShouldSuccess() throws CMException, OrchidException, IOException {
        when(target.getVideoId()).thenReturn(videoId);
        doReturn(playerId).when(target).getPlayerId();
        when(target.getElementId()).thenReturn("");
        target.localRender(oc);
        assertEquals(videoId, target.bcVideoId);
        assertEquals(playerId, target.bcPlayerId);
        assertFalse(target.showNothing);
    }

    @Test
    public void testGetElementIdWithException() throws CMException, OrchidException, IOException {
        when(target.getVideoId()).thenReturn(videoId);
        doReturn(playerId).when(target).getPlayerId();
        doThrow(new CMException("Test")).when(target).getElementId();
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
    public void shouldGetPlayerId() throws CMException {
        doReturn(parentPolicy).when(target).getElementParent();
        doReturn(cmServer).when(target).getCmServer();
        when(cmServer.getPolicy(any(ExternalContentId.class))).thenReturn(configPolicy);
        doReturn(siteId).when(target).getCurrentRootSiteId();
        when(configPolicy.getPreviewPlayerId(siteId)).thenReturn(playerId);
        assertEquals(playerId, target.getPlayerId());
        when(parentPolicy.getPlayerId()).thenReturn("someId");
        assertEquals("someId", target.getPlayerId());
    }

    @Test
    public void shouldGetCmServer() throws CMException {
        Policy policy = mock(Policy.class);
        doReturn(policy).when(target).getPolicy();
        when(policy.getCMServer()).thenReturn(cmServer);
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
