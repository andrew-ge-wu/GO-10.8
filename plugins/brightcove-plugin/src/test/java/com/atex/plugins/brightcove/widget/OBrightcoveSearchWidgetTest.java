/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove.widget;

import java.io.IOException;
import java.util.Date;
import java.util.ResourceBundle;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.mockito.MockitoAnnotations;

import com.atex.plugins.brightcove.BrightCoveElementPolicy;
import com.atex.plugins.brightcove.util.ConfigurationUtil;
import com.atex.plugins.brightcove.widget.OBrightcoveSearchWidget.PreviewEventListener;
import com.atex.plugins.brightcove.widget.OBrightcoveSearchWidget.SearchEventListener;
import com.brightcove.commons.catalog.objects.Video;
import com.brightcove.commons.catalog.objects.Videos;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.ContentSession;
import com.polopoly.cm.app.PolicyWidget;
import com.polopoly.cm.app.widget.OTextInputPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.impl.OrchidContextImpl;
import com.polopoly.orchid.event.OrchidEvent;
import com.polopoly.orchid.widget.OImage;
import com.polopoly.orchid.widget.OJavaScript;
import com.polopoly.orchid.widget.OSelect;
import com.polopoly.orchid.widget.OSubmitButton;
import com.polopoly.orchid.widget.OTextInput;
import com.polopoly.orchid.widget.OWidget;

/**
 *
 */
public class OBrightcoveSearchWidgetTest {
    
    OBrightcoveSearchWidget target;
    
    @Mock
    OrchidContextImpl oc;
    @Mock
    OrchidEvent oe;
    @Mock
    ResourceBundle rb;
    @Mock
    Device device;
    @Mock
    Policy policy;
    @Mock
    PolicyWidget policyWidget;
    @Mock
    BrightCoveElementPolicy brightCoveElementPolicy;
    
    @Mock
    ContentSession contentSession;
    @Mock
    OSelect select;
    @Mock
    OJavaScript javaScript;
    @Mock
    OTextInput input;
    @Mock
    OSubmitButton button;
    @Mock
    OImage image;
    @Mock
    OSubmitButton preview;
    @Mock
    OTextInputPolicyWidget textInput;
    @Mock
    OWidget childWidget;
    
    @Mock
    Videos videos;
    @Mock
    Video video1;
    @Mock
    Video video2;
    @Mock
    PolicyCMServer cmServer;
    @Mock
    ConfigurationUtil configUtil;

    String siteId = "siteId";

    @Before
    public void before() throws OrchidException, IOException, CMException {
        MockitoAnnotations.initMocks(this);
        when(oc.getMessageBundle()).thenReturn(rb);
        when(oc.getDevice()).thenReturn(device);
        when(policy.getCMServer()).thenReturn(cmServer);
        when(video1.getThumbnailUrl()).thenReturn("http://abc.com/images/thumbnail1.jpg");
        when(video1.getLastModifiedDate()).thenReturn(new Date());
        when(video2.getThumbnailUrl()).thenReturn("http://abc.com/images/thumbnail2.jpg");
        when(video2.getLastModifiedDate()).thenReturn(new Date());
        when(contentSession.getTopWidget()).thenReturn(policyWidget);
        when(contentSession.getTopPolicy()).thenReturn(brightCoveElementPolicy);
        when(contentSession.findPolicyWidget(OBrightcoveSearchWidget.ID)).thenReturn(textInput);
        when(contentSession.findPolicyWidget(OBrightcoveSearchWidget.NAME)).thenReturn(textInput);
        when(textInput.getChildWidget()).thenReturn(childWidget);
        when(childWidget.getCompoundId()).thenReturn("compoundId");
        when(oe.getWidget()).thenReturn(preview);
        doNothing().when(select).render(oc);
        doNothing().when(javaScript).render(oc);
        doNothing().when(input).render(oc);
        doNothing().when(button).render(oc);
        doNothing().when(image).render(oc);
        target = spy(new OBrightcoveSearchWidget());
        doReturn(contentSession).when(target).getContentSession();
        doReturn(select).when(target).getOSelect();
        doReturn(javaScript).when(target).getOJavaScript();
        doReturn(input).when(target).getOTextInput();
        doReturn(button).when(target).getOSubmitButton();
        doReturn(videos).when(target).search();
        doReturn(policy).when(target).getPolicy();
        doReturn(image).when(target).getOImage();
        
        target.initSelf(oc);
    }
    
    @Test(expected = OrchidException.class)
    public void showThrowErrorWhenFailedToGetPolicyCMServer() throws CMException, OrchidException {
        when(policy.getCMServer()).thenThrow(new CMException("Unit test generated Exception"));
        target.initSelf(oc);
    }
    
    @Test
    public void viewModeShouldNotRenderWidget() throws OrchidException, IOException {
        when(contentSession.getMode()).thenReturn(1);
        target.localRender(oc);
        verify(javaScript, never()).render(oc);
    }
    
    @Test
    public void searchWithNoResult() throws OrchidException, IOException, CMException {
        when(contentSession.getMode()).thenReturn(2);
        doReturn(null).when(target).search();
        SearchEventListener doSearch = target.new SearchEventListener();
        doSearch.processEvent(oc, oe);
        target.localRender(oc);
        verify(device, times(5)).print(anyString());
    }
    
    @Test(expected = OrchidException.class)
    public void shouldThrowErrorWhenFailedToSearch() throws OrchidException, IOException, CMException {
        when(contentSession.getMode()).thenReturn(2);
        doThrow(new CMException("Unit test generated Exception")).when(target).search();
        SearchEventListener doSearch = target.new SearchEventListener();
        doSearch.processEvent(oc, oe);
    }
    
    @Test
    public void searchWithOneResult() throws OrchidException, IOException, CMException {
        when(contentSession.getMode()).thenReturn(2);
        when(videos.size()).thenReturn(2);
        when(videos.get(0)).thenReturn(video1);
        when(videos.get(1)).thenReturn(video2);
        SearchEventListener doSearch = target.new SearchEventListener();
        doSearch.processEvent(oc, oe);
        target.localRender(oc);
        target.localRender(oc);
        verify(device, times(2)).print("<tr class='even'><td style='padding:6px;width:120px'>");
        verify(device, times(2)).print("<tr class='odd'><td style='padding:6px;width:120px'>");
        verify(device, times(36)).print(anyString());
    }
    
    @Test
    public void previewResult() throws OrchidException, IOException, CMException {
        when(contentSession.getMode()).thenReturn(2);
        when(videos.size()).thenReturn(2);
        when(videos.get(0)).thenReturn(video1);
        when(videos.get(1)).thenReturn(video2);
        PreviewEventListener doPreview = target.new PreviewEventListener();
        doPreview.processEvent(oc, oe);
        target.localRender(oc);
        verify(javaScript).setScript(anyString());
    }
    
    @Test(expected = OrchidException.class)
    public void previewResultWithError() throws OrchidException, IOException, CMException {
        when(contentSession.getMode()).thenReturn(2);
        doThrow(new CMException("Unit test generated Exception")).when(policyWidget).store();
        when(videos.size()).thenReturn(2);
        when(videos.get(0)).thenReturn(video1);
        when(videos.get(1)).thenReturn(video2);
        PreviewEventListener doPreview = target.new PreviewEventListener();
        doPreview.processEvent(oc, oe);
        target.localRender(oc);
        verify(javaScript).setScript(anyString());
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
