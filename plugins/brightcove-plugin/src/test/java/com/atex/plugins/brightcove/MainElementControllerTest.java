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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.atex.plugins.brightcove.util.ConfigurationUtil;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.Model;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

import static org.mockito.Mockito.*;

public class MainElementControllerTest {

    private MainElementController target;
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
    BrightCoveElementPolicy brightCoveElementPolicy;
    @Mock
    Model model;
    @Mock
    ConfigurationUtil configUtil;
    @Mock
    ContentId id;

    @Before
    public void before() throws CMException {
        MockitoAnnotations.initMocks(this);
        target = spy(new MainElementController() {
            @Override
            public CmClient getCmClient(ControllerContext c) {
                return cmClient;
            }
        });
        when(m.getLocal()).thenReturn(local);
    }

    @After
    public void after() throws CMException {
        target = null;
    }

    @Test
    public void testPopulateModelBeforeCacheKey () {
        String expectedString = "01234567890";

        doReturn(brightCoveElementPolicy).when(target).getBrightCoveElementPolicy(context);
        doReturn(configUtil).when(target).getConfiguration(cmServer);
        doReturn(expectedString).when(brightCoveElementPolicy).getPlayerId();

        target.populateModelBeforeCacheKey(request, m, context);
        verify(local, never()).setAttribute("playerId", expectedString);
    }

    @Test
    public void testPopulateModelBeforeCacheKeyBranchNullValue () {
        String expectedString = "01234567890";
        doReturn(cmServer).when(target).getPolicyCMServer(context);
        doReturn(brightCoveElementPolicy).when(target).getBrightCoveElementPolicy(context);
        doReturn(configUtil).when(target).getConfiguration(cmServer);
        doReturn(null).when(brightCoveElementPolicy).getPlayerId();
        doReturn(expectedString).when(configUtil).getConfigPlayerId(any(ContentId.class));

        target.populateModelBeforeCacheKey(request, m, context);
        verify(local).setAttribute("playerId", expectedString);
    }

    @Test
    public void testPopulateModelBeforeCacheKeyBranchEmptyString () {
        String expectedString = "01234567890";
        doReturn(cmServer).when(target).getPolicyCMServer(context);
        doReturn(brightCoveElementPolicy).when(target).getBrightCoveElementPolicy(context);
        doReturn(configUtil).when(target).getConfiguration(cmServer);
        doReturn("").when(brightCoveElementPolicy).getPlayerId();
        doReturn(expectedString).when(configUtil).getConfigPlayerId(any(ContentId.class));

        target.populateModelBeforeCacheKey(request, m, context);
        verify(local).setAttribute("playerId", expectedString);
    }

    @Test
    public void shouldGetBrightcoveElementPolicy() {
        when(context.getContentModel()).thenReturn(model);
        when(model.getAttribute("_data")).thenReturn(brightCoveElementPolicy);
        assertEquals(brightCoveElementPolicy ,target.getBrightCoveElementPolicy(context));
    }

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
