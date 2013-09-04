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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.atex.plugins.brightcove.util.ConfigurationUtil;
import com.brightcove.org.json.JSONArray;
import com.brightcove.org.json.JSONException;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.Model;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

/**
 *
 */
public class TvControllerTest {
    
    TvController target;
    
    @Mock
    FakeRenderRequest request;
    @Mock
    TopModel m;
    @Mock
    ModelWrite local;
    @Mock
    ControllerContext context;
    @Mock
    Model model;
    TVPolicy policy;
    @Mock
    BrightcoveVideoPolicy brightcoveVideoPolicy1;
    @Mock
    BrightcoveVideoPolicy brightcoveVideoPolicy2;
    @Mock
    BrightcoveVideoPolicy brightcoveVideoPolicy3;
    @Mock
    BrightcoveVideoPolicy brightcoveVideoPolicy4;
    
    @Mock
    PolicyCMServer cmServer;
    @Mock
    Content content;
    @Mock
    InputTemplate inputTemplate;
    @Mock
    Policy parent;
    @Mock
    CmClient cmClient;
    @Mock
    ConfigurationUtil configUtil;
    @Mock
    BrightcoveConfigPolicy configPolicy;
    @Mock
    ListElementPolicy listElementPolicy;
    @Mock
    BrightcoveVideoPolicy videoPolicy;
    @Mock
    ContentId id;

    @Before
    public void before() throws CMException {
        MockitoAnnotations.initMocks(this);
        target = spy(new TvController() {
            @Override
            public CmClient getCmClient(ControllerContext c) {
                return cmClient;
            }
        });
        policy = spy(new TVPolicy() {
            @Override
            protected void initSelf() {}
        });
        
        videoPolicy = spy(new BrightcoveVideoPolicy() {
            @Override
            protected void initSelf() {}
        });
        videoPolicy.init("PolicyName", new Content[] { content }, inputTemplate, parent, cmServer);

        policy.init("PolicyName", new Content[] { content }, inputTemplate, parent, cmServer);
        when(context.getContentModel()).thenReturn(model);
        when(model.getAttribute("_data")).thenReturn(policy);
        doReturn(Arrays.asList(brightcoveVideoPolicy1, brightcoveVideoPolicy2, brightcoveVideoPolicy3, brightcoveVideoPolicy4)).when(policy).getVideos();
        when(m.getLocal()).thenReturn(local);
        when(cmServer.getPolicy(BrightcoveConfigPolicy.CONTENT_ID)).thenReturn(configPolicy);
        doReturn(listElementPolicy).when(policy).getFirstBcListEle();
        doReturn(null).when(policy).getSelectedCategorizationOption();
        doReturn(null).when(policy).getRelatedElementId();
    }
    
    @Test
    public void shouldNotSetCoverWhenNoVidSet() throws CMException {
        doReturn("12345").when(target).getConfigPlayerId(context);
        doReturn(new JSONArray()).when(target).jsonItem(any(BrightcoveVideoPolicy.class));
        target.populateModelBeforeCacheKey(request, m, context);
        verify(request, never()).setAttribute("cover", null);
    }
    
    @Test
    public void shouldNotThrowErrorWhenInvalidContentStringIsUsed() {
        doReturn("12345").when(target).getConfigPlayerId(context);
        doReturn(new JSONArray()).when(target).jsonItem(any(BrightcoveVideoPolicy.class));
        doReturn(Arrays.asList(brightcoveVideoPolicy1, brightcoveVideoPolicy2, brightcoveVideoPolicy3)).when(policy).getVideos();
        when(request.getParameter("vid")).thenReturn("randomString");
        target.populateModelBeforeCacheKey(request, m, context);
        verify(request).setAttribute(eq("cover"), anyObject());
    }
    
    @Test
    public void shouldNotThrowErrorWhenFaildToFindContent() throws CMException {
        doReturn("12345").when(target).getConfigPlayerId(context);
        doReturn(cmServer).when(target).getPolicyCMServer(context);
        doReturn(new JSONArray()).when(target).jsonItem(any(BrightcoveVideoPolicy.class));
        when(request.getParameter("vid")).thenReturn("1.23");
        doThrow(new CMException("Unit test generated Exception")).when(cmServer).getPolicy(any(ContentId.class));
        target.populateModelBeforeCacheKey(request, m, context);
        verify(request).setAttribute(eq("cover"), anyObject());
    }

    @Test
    public void shouldNotSetCoverWhenVideoSizeZero() throws CMException {
        doReturn("12345").when(target).getConfigPlayerId(context);
        doReturn(cmServer).when(target).getPolicyCMServer(context);
        doReturn(Arrays.asList()).when(policy).getVideos();
        when(request.getParameter("vid")).thenReturn("1.23");
        doThrow(new CMException("Unit test generated Exception")).when(cmServer).getPolicy(any(ContentId.class));
        target.populateModelBeforeCacheKey(request, m, context);
        verify(request).setAttribute("cover", null);
    }

    @Test
    public void shouldNotIncludeCoverJsonItem() throws CMException {
        doReturn("12345").when(target).getConfigPlayerId(context);
        doReturn(cmServer).when(target).getPolicyCMServer(context);
        doReturn(Arrays.asList()).when(policy).getVideos();
        when(request.getParameter("vid")).thenReturn("1.23");
        doThrow(new CMException("Unit test generated Exception")).when(cmServer).getPolicy(any(ContentId.class));
        target.populateModelBeforeCacheKey(request, m, context);
        verify(local).setAttribute(eq("jsVideos"), any(JSONArray.class));
    }

    @Test
    public void shouldReturnJsonItem() throws JSONException {
        doReturn("shortId").when(target).getShortId(brightcoveVideoPolicy1);
        doReturn("videoName").when(target).getVideoName(brightcoveVideoPolicy1);
        when(brightcoveVideoPolicy1.getId()).thenReturn("videoId");
        JSONArray item = target.jsonItem(brightcoveVideoPolicy1);
        assertNotNull(item);
        assertEquals("shortId", item.get(0));
        assertEquals("videoName", item.get(1));
        assertEquals("videoId", item.get(2));
    }

    @Test
    public void shouldGetVideoName() throws CMException {
        when(brightcoveVideoPolicy1.getName()).thenReturn("videoName");
        assertEquals("videoName", target.getVideoName(brightcoveVideoPolicy1));
    }

    @Test
    public void shouldGetEmptyVideoNameCMException() throws CMException {
        when(brightcoveVideoPolicy1.getName()).thenThrow(new CMException("Unit test exception"));
        assertEquals("", target.getVideoName(brightcoveVideoPolicy1));
    }

    @Test
    public void shouldGetShortId() throws CMException {
        VersionedContentId vcId = new VersionedContentId(1,123, 4);
        when(videoPolicy.getContentId()).thenReturn(vcId);
        assertEquals("1.123", target.getShortId(videoPolicy));
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

    interface FakeRenderRequest extends HttpServletRequest, RenderRequest {
        
    }

}
