/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove;

import com.atex.plugins.brightcove.util.ConfigurationUtil;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.policy.SingleReference;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.Model;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 *
 */
public class ListElementControllerTest {

    ListElementController target;

    @Mock
    FakeRenderRequest request;
    @Mock
    HashMap<String, Policy> children = new HashMap<String, Policy>();
    @Mock
    TopModel m;
    @Mock
    ControllerContext context;
    @Mock
    Model model;
    ListElementPolicy policy;
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
    CmClient cmClient;
    @Mock
    Content content;
    @Mock
    InputTemplate inputTemplate;
    @Mock
    Policy parent;
    @Mock
    SingleValuePolicy maxVideoChildPolicy;
    @Mock
    FakeSingleReference departmentChildPolicy;
    @Mock
    Policy displayChildPolicy;
    @Mock
    BrightcoveConfigPolicy configPolicy;
    @Mock
    ConfigurationUtil configUtil;
    @Mock
    ContentId id;

    @Before
    public void before() throws CMException {
        MockitoAnnotations.initMocks(this);
        target = spy(new ListElementController() {
            @Override
            protected CmClient getCmClient(ControllerContext context) {
                return cmClient;
            }
        });
        policy = spy(new ListElementPolicy() {
            @Override
            protected void initChildPolicies() {
                this.childPolicies = children;
            }
            @Override
            protected void initSelf() {}
        });
        policy.init("PolicyName", new Content[] { content }, inputTemplate, parent, cmServer);
        when(cmServer.getPolicy(BrightcoveConfigPolicy.CONTENT_ID)).thenReturn(configPolicy);
        when(context.getContentModel()).thenReturn(model);
        when(model.getAttribute("_data")).thenReturn(policy);
        when(children.get(ListElementPolicy.MAX_VIDEO)).thenReturn(maxVideoChildPolicy);
        when(maxVideoChildPolicy.getValue()).thenReturn("2");
        when(children.get(ListElementPolicy.SELECTED_DEPT)).thenReturn(departmentChildPolicy);
        when(children.get(ListElementPolicy.DISPLAY)).thenReturn(displayChildPolicy);
        doReturn(Arrays.asList(brightcoveVideoPolicy1, brightcoveVideoPolicy2, brightcoveVideoPolicy3, brightcoveVideoPolicy4)).when(policy).getVideos();
    }

    @Test
    public void shouldNotSetCoverWhenNoVidSet() {
        target.populateModelBeforeCacheKey(request, m, context);
        verify(request, never()).setAttribute("cover", null);
    }

    @Test
    public void shouldNotThrowErrorWhenInvalidContentStringIsUsed() {
        doReturn(Arrays.asList(brightcoveVideoPolicy1, brightcoveVideoPolicy2, brightcoveVideoPolicy3)).when(policy).getVideos();
        when(request.getParameter("vid")).thenReturn("randomString");
        target.populateModelBeforeCacheKey(request, m, context);
        verify(request).setAttribute(eq("cover"), anyObject());
    }

    @Test
    public void shouldNotThrowErrorWhenFaildToFindContent() throws CMException {
        when(request.getParameter("vid")).thenReturn("1.23");
        doThrow(new CMException("Unit test generated Exception")).when(cmServer).getPolicy(any(ContentId.class));
        target.populateModelBeforeCacheKey(request, m, context);
        verify(request).setAttribute(eq("cover"), anyObject());
    }

    @Test
    public void shouldNotSetDepartmentIntoRequestWhenFailedToGetWebTVDepartment() throws CMException {
        doReturn("webtvDept").when(policy).getDisplayLocation();
        doReturn(null).when(target).getConfigWebTVDepartment(context);
        target.populateModelBeforeCacheKey(request, m, context);
        verify(request, never()).setAttribute(eq("department"), anyObject());
    }

    @Test
    public void shouldSetDepartmentContentIdIntoRequest() throws CMException {
        doReturn("selectedDept").when(policy).getDisplayLocation();
        ContentId  selectedDepartment = mock(ContentId.class);
        doReturn(selectedDepartment).when(policy).getSelectedDepartment();
        target.populateModelBeforeCacheKey(request, m, context);
        verify(request).setAttribute(eq("department"),eq(selectedDepartment));
    }

    @Test
    public void shouldGetConfigWebTvDepartment(){
        ContentId departmentId = mock(ContentId.class);
        doReturn(cmServer).when(target).getPolicyCMServer(context);
        doReturn(configUtil).when(target).getConfiguration(cmServer);
        when(context.getContentId()).thenReturn(id);
        when(configUtil.getConfigWebTVDepartment(id)).thenReturn(departmentId);
        assertEquals(departmentId, target.getConfigWebTVDepartment(context));
    }

    @Test
    public void shouldGetPolicyCMServer() {
        when(cmClient.getPolicyCMServer()).thenReturn(cmServer);
        assertEquals(cmServer, target.getPolicyCMServer(context));
    }

    interface FakeRenderRequest extends HttpServletRequest, RenderRequest {

    }

    interface FakeSingleReference extends SingleReference, Policy {

    }
}
