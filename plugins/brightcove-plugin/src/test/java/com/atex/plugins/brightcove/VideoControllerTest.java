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
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.atex.plugins.brightcove.util.ConfigurationUtil;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

/**
 *
 */
public class VideoControllerTest {

    VideoController target;
    @Mock
    RenderRequest request;
    @Mock
    TopModel m;
    @Mock
    ModelWrite local;
    @Mock
    ControllerContext context;
    @Mock
    CmClient cmClient;
    @Mock
    PolicyCMServer cmServer;
    @Mock
    BrightcoveConfigPolicy configPolicy;
    @Mock
    ContentRead contentRead;
    @Mock
    ConfigurationUtil configUtil;
    @Mock
    ContentId id;

    public static final String PREVIEW_PLAYER = "12345";

    @Before
    public void before() throws CMException {
        MockitoAnnotations.initMocks(this);
        target = spy(new VideoController() {
            @Override
            public CmClient getCmClient(ControllerContext c) {
                return cmClient;
            }
        });
        when(m.getLocal()).thenReturn(local);
    }

    @Test
    public void shouldSetPlayerIdIntoModel() {
        doReturn(PREVIEW_PLAYER).when(target).getConfigPlayerId(context);
        target.populateModelBeforeCacheKey(request, m, context);
        verify(local).setAttribute(VideoController.PLAYER_ID, PREVIEW_PLAYER);
    }

//    @Test
//    public void shouldReturnNullPlayerIdWhenHaveException() throws CMException {
//        doReturn(cmServer).when(target).getPolicyCMServer(context);
//        doThrow(new CMException("Unit Test generated Exception, ignore this!")).when(cmServer).getPolicy(BrightcoveConfigPolicy.CONTENT_ID);
//        target.populateModelBeforeCacheKey(request, m, context);
//        verify(local).setAttribute(VideoController.PLAYER_ID, null);
//    }
    @Test
    public void shouldGetConfigPlayerId(){
        String expected = "12345";
        doReturn(cmServer).when(target).getPolicyCMServer(context);
        doReturn(configUtil).when(target).getConfiguration(cmServer);
        when(context.getContentId()).thenReturn(id);
        when(configUtil.getConfigPlayerId(id)).thenReturn(expected);
        assertEquals(expected, target.getConfigPlayerId(context));
    }

    @Test
    public void shouldGetPolicyCMServer() {
        when(cmClient.getPolicyCMServer()).thenReturn(cmServer);
        assertEquals(cmServer, target.getPolicyCMServer(context));
    }
}
