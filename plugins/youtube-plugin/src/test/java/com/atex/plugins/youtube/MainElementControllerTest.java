/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.youtube;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.polopoly.model.ModelPathUtil;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

public class MainElementControllerTest {

    private MainElementController target;

    @Mock
    RenderRequest request;
    @Mock
    TopModel m;
    @Mock
    CacheInfo cacheInfo;
    @Mock
    ControllerContext context;

    @Mock
    ModelWrite stack;
    @Mock
    ModelWrite model;
    @Mock
    ModelWrite content;
    @Mock
    YoutubeElementPolicy policy;
    @Mock
    ModelPathUtil modelPathUtil;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        target = new MainElementController();
    }

    @Test
    public void testYoutubeControllerConstruction() throws Exception {
        Assert.assertNotNull("controller failed initialization", target);
    }

    @Test
    public void testControllerPopulateModelAfterCacheKey() {
        populateModelAfterCacheKey("250", 0, 0, 250D, 187.5D);
    }

    @Test
    public void testControllerPopulateModelAfterCacheKey2() {
        populateModelAfterCacheKey("250", 300, 0, 250D, 187.5D);
    }

    @Test
    public void testControllerPopulateModelAfterCacheKey3() {
        populateModelAfterCacheKey("250", 300, 300, 250D, 300D);
    }

    private void populateModelAfterCacheKey(String colwidth, int width, int height, double expectedWidth, double expectedHeight) {
        when(policy.getWidth()).thenReturn(width);
        when(policy.getHeight()).thenReturn(height);
        when(stack.getAttribute("colwidth")).thenReturn(colwidth);

        when(m.getStack()).thenReturn(stack);
        when(m.getLocal()).thenReturn(model);

        when(model.getAttribute("content")).thenReturn(content);
        when(content.getAttribute("_data")).thenReturn(policy);

        target.populateModelAfterCacheKey(request, m, cacheInfo, context);

        verify(model).setAttribute("widthInRatio", expectedWidth);
        verify(model).setAttribute("heightInRatio", expectedHeight);
    }
}
