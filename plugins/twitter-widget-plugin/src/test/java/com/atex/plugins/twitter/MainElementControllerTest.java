/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;

public class MainElementControllerTest {
    private MainElementController target;

    @Mock
    private RenderRequest request;
    @Mock
    private TopModel m;
    @Mock
    private ControllerContext context;
    @Mock
    private PolicyCMServer policyCmServer;
    @Mock
    private ModelWrite local;
    @Mock
    private TwitterWidgetPolicy policy;
    @Mock
    private CmClient cmClient;

    @Before
    public void setUp() throws CMException {
        MockitoAnnotations.initMocks(this);

        target = spy(new MainElementController() {
            @Override
            protected CmClient getCmClient(ControllerContext context) {
                return cmClient;
            }

            @Override
            protected TwitterWidgetPolicy getTwitterWidgetPolicy() {
                return policy;
            }
        });

        when(cmClient.getPolicyCMServer()).thenReturn(policyCmServer);
        when(m.getLocal()).thenReturn(local);

        when(policy.getShellBackgroundColor()).thenReturn("#ffffff");
        when(policy.getShellTextColor()).thenReturn("#ff0000");
        when(policy.getTweetBackgroundColor()).thenReturn("#ffff00");
        when(policy.getTweetTextColor()).thenReturn("#000000");
        when(policy.getTweetLinkColor()).thenReturn("#00ffff");
        when(policy.getHeight()).thenReturn("auto");
        when(policy.getWidth()).thenReturn("auto");
        when(policy.getAvatars()).thenReturn("true");
        when(policy.getScrollbar()).thenReturn("true");
        when(policy.getLoop()).thenReturn("true");
    }

    @Test
    public void testModelWriteAttribute() throws CMException {
        target.populateModelBeforeCacheKey(request, m, context);
        verify(local).setAttribute("shellBgColor", "#ffffff");
        verify(local).setAttribute("shellFgColor", "#ff0000");
        verify(local).setAttribute("tweetBgColor", "#ffff00");
        verify(local).setAttribute("tweetFgColor", "#000000");
        verify(local).setAttribute("tweetLinkColor", "#00ffff");
        verify(local).setAttribute("width", "auto");
        verify(local).setAttribute("height", "auto");
        verify(local).setAttribute("avatars", "true");
        verify(local).setAttribute("scrollbar", "true");
        verify(local).setAttribute("loop", "true");
    }
}
