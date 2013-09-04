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
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.atex.plugins.brightcove.BrightcoveConfigPolicy;
import com.atex.plugins.brightcove.BrightcoveVideoPolicy;
import com.atex.plugins.brightcove.service.BrightcoveService;
import com.atex.plugins.brightcove.util.ConfigurationUtil;
import com.atex.plugins.brightcove.widget.OBrightcoveSyncWidget.PreviewEventListener;
import com.atex.plugins.brightcove.widget.OBrightcoveSyncWidget.SyncEventListener;
import com.brightcove.commons.catalog.objects.Video;
import com.brightcove.mediaapi.exceptions.BrightcoveException;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.ContentSession;
import com.polopoly.cm.app.PolicyWidget;
import com.polopoly.cm.app.policy.SelectableSubFieldPolicy;
import com.polopoly.cm.app.search.categorization.CategorizationProvider;
import com.polopoly.cm.app.widget.OFCKEditorPolicyWidget;
import com.polopoly.cm.app.widget.OSelectableSubFieldPolicyWidget;
import com.polopoly.cm.app.widget.OTextAreaPolicyWidget;
import com.polopoly.cm.app.widget.OTextInputPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.impl.OrchidContextImpl;
import com.polopoly.orchid.event.OrchidEvent;
import com.polopoly.orchid.widget.OJavaScript;
import com.polopoly.orchid.widget.OSubmitButton;
import com.polopoly.orchid.widget.OWidget;
import com.polopoly.cm.app.imagemanager.ImageFormatException;
import com.polopoly.cm.app.imagemanager.ImageManagerPolicy;
import com.polopoly.cm.app.imagemanager.ImageTooBigException;

/**
 * @since 1.0.2
 */
public class OBrightcoveSyncWidgetTest {
    
    OBrightcoveSyncWidget target;
    
    @Mock
    OrchidContextImpl oc;
    @Mock
    OrchidEvent oe;
    @Mock
    BrightcoveVideoPolicy policy;
    @Mock
    CustomCategorizationProvider categorizationProvider;
    @Mock
    SelectableSubFieldPolicy selectableSubFieldPolicy;
    @Mock
    ImageManagerPolicy imageManagerPolicy;
    @Mock
    ContentSession contentSession;
    @Mock
    PolicyWidget policyWidget;
    @Mock
    OTextInputPolicyWidget name;
    @Mock
    OWidget nameWidget;
    @Mock
    OTextAreaPolicyWidget shortDescription;
    @Mock
    OWidget shortDescriptionWidget;
    @Mock
    OFCKEditorPolicyWidget longDescription;
    @Mock
    OWidget longDescriptionWidget;
    @Mock
    OSelectableSubFieldPolicyWidget selectableSubFieldPolicyWidget;
    @Mock
    OJavaScript javaScript;
    @Mock
    OSubmitButton syncButton;
    @Mock
    BrightcoveService brightcoveService;
    @Mock
    Video video;
    @Mock
    PolicyCMServer policyCMServer;
    @Mock
    BrightcoveConfigPolicy configPolicy;
    @Mock
    ConfigurationUtil configUtil;

    String siteId = "siteId";

    @Before
    public void before() throws OrchidException, IOException, CMException, BrightcoveException, ImageFormatException, ImageTooBigException, URISyntaxException {
        MockitoAnnotations.initMocks(this);
        
        when(contentSession.getMode()).thenReturn(2);
        when(contentSession.getTopWidget()).thenReturn(policyWidget);
        when(contentSession.findPolicyWidget(OBrightcoveSyncWidget.NAME)).thenReturn(name);
        when(contentSession.findPolicyWidget(OBrightcoveSyncWidget.SHORT_DESCRIPTION)).thenReturn(shortDescription);
        when(contentSession.findPolicyWidget(OBrightcoveSyncWidget.LONG_DESCIPTION)).thenReturn(longDescription);
        when(contentSession.findPolicyWidget(OBrightcoveSyncWidget.IMAGE_TYPE)).thenReturn(selectableSubFieldPolicyWidget);
        when(contentSession.getTopWidget()).thenReturn(policyWidget);
        when(name.getChildWidget()).thenReturn(nameWidget);
        when(shortDescription.getChildWidget()).thenReturn(shortDescriptionWidget);
        when(longDescription.getChildWidget()).thenReturn(longDescriptionWidget);
        when(nameWidget.getCompoundId()).thenReturn("name_id");
        when(shortDescriptionWidget.getCompoundId()).thenReturn("short_id");
        when(longDescriptionWidget.getCompoundId()).thenReturn("long_id");
        when(video.getName()).thenReturn("video name");
        when(video.getShortDescription()).thenReturn("video short description");
        when(video.getLongDescription()).thenReturn("video long description");
        when(video.getThumbnailUrl()).thenReturn(getClass().getClassLoader().getResource("small.jpg").toURI().toURL().toString());
        when(brightcoveService.findByVideoID(1)).thenReturn(video);
        when(policy.getId()).thenReturn("1");
        when(policy.getChildPolicy("categorization")).thenReturn(categorizationProvider);
        when(policy.getSubFieldPolicy()).thenReturn(selectableSubFieldPolicy);
        when(selectableSubFieldPolicy.getChildPolicy(BrightcoveVideoPolicy.IMAGE)).thenReturn(imageManagerPolicy);
        when(policyCMServer.getPolicy(BrightcoveConfigPolicy.CONTENT_ID)).thenReturn(configPolicy);

        doNothing().when(javaScript).render(oc);
        doNothing().when(syncButton).render(oc);
        
        target = spy(new OBrightcoveSyncWidget());
        doReturn(brightcoveService).when(target).getBrightcoveService();
        doReturn(policy).when(target).getPolicy();
        doReturn(javaScript).when(target).getOJavaScript();
        doReturn(syncButton).when(target).getOButton();
        doReturn(contentSession).when(target).getContentSession();
        target.initSelf(oc);
        target.localRender(oc);
    }
    
    @Test
    public void shouldNotRendarOnViewMode() throws OrchidException, IOException {
        when(contentSession.getMode()).thenReturn(1);
        target.localRender(oc);
        verify(javaScript, times(1)).render(oc);
        verify(syncButton, times(2)).render(oc);
    }
    
    @Test
    public void shouldNotAddImageIfThumbnailIsBlank() throws OrchidException, IOException, CMException {
        doReturn(policyCMServer).when(target).getCmServer();
        when(video.getThumbnailUrl()).thenReturn(null);
        SyncEventListener sync = target.new SyncEventListener();
        sync.processEvent(oc, oe);
        verify(policy, never()).getChildPolicy("imageType");
    }
    
    @Test(expected = OrchidException.class)
    public void shouldThrowErrorWhenFaildTogetbrightcoveService() throws CMException, OrchidException {
        doThrow(new CMException("Unit test generated Exception")).when(target).getBrightcoveService();
        target.initSelf(oc);
    }
    
    @Test(expected = OrchidException.class)
    public void shouldThrowErrorWhenFaildToStore() throws CMException, OrchidException {
        doThrow(new CMException("Unit test generated Exception")).when(policyWidget).store();
        SyncEventListener sync = target.new SyncEventListener();
        sync.processEvent(oc, oe);
    }
    
    @Test(expected = OrchidException.class)
    public void shouldThrowErrorWhenFaildConvertURL() throws CMException, OrchidException {
        doReturn(policyCMServer).when(target).getCmServer();
        when(video.getThumbnailUrl()).thenReturn("wrong.url");
        SyncEventListener sync = target.new SyncEventListener();
        sync.processEvent(oc, oe);
    }

    @Test(expected = OrchidException.class)
    public void shouldShowErrorWhenFailedToFindVieo() throws CMException, BrightcoveException, OrchidException {
        doThrow(new BrightcoveException("Unit test generated Exception")).when(brightcoveService).findByVideoID(1);
        SyncEventListener sync = target.new SyncEventListener();
        try {
            sync.processEvent(oc, oe);
        } catch (OrchidException e) {
            verify(target, times(1)).handleErrors(eq(oc), anyString());
            throw e;
        }
    }

    @Test
    public void shouldSuccessfullySyncVideo() throws CMException, OrchidException {
        doReturn(policyCMServer).when(target).getCmServer();
        SyncEventListener sync = target.new SyncEventListener();
        sync.processEvent(oc, oe);
    }

    @Test
    public void shouldNotCallBrightcoveServiceIfBrightcoveIdIsNull() throws OrchidException, CMException, BrightcoveException{
        when(policy.getId()).thenReturn(null);
        SyncEventListener sync = target.new SyncEventListener();
        sync.processEvent(oc, oe);
        verify(brightcoveService,never()).findByVideoID(anyLong());
    }
    
    @Test
    public void shouldNotCallBrightcoveServiceIfBrightcoveIdIsEmpty() throws OrchidException, CMException, BrightcoveException{
        when(policy.getId()).thenReturn("");
        SyncEventListener sync = target.new SyncEventListener();
        sync.processEvent(oc, oe);
        verify(brightcoveService,never()).findByVideoID(anyLong());
    }
    
    @Test
    public void shouldSuccessfullyPreviewVideo() throws OrchidException{
        PreviewEventListener preview = target.new PreviewEventListener();
        preview.processEvent(oc, oe);
    }
    
    @Test
    public void shouldNotSuccessPreviewVideoIfBrightcoveIdIsNull() throws OrchidException{
        when(policy.getId()).thenReturn(null);
        PreviewEventListener preview = target.new PreviewEventListener();
        preview.processEvent(oc, oe);
        verify(target,times(1)).getString(eq("com.atex.plugins.brightcove.brightcove.id.required"));
    }
    
    @Test
    public void shouldNotSuccessPreviewVideoIfBrightcoveIdIsEmpty() throws OrchidException{
        when(policy.getId()).thenReturn("");
        PreviewEventListener preview = target.new PreviewEventListener();
        preview.processEvent(oc, oe);
        verify(target,times(1)).getString(eq("com.atex.plugins.brightcove.brightcove.id.required"));
    }
    
    @Test(expected=OrchidException.class)
    public void throwExceptionWhenPreviewVideo() throws CMException, OrchidException{
        doThrow(CMException.class).when(policyWidget).store();
        PreviewEventListener preview = target.new PreviewEventListener();
        preview.processEvent(oc, oe);
    }

    @Test
    public void shouldGetCmServer() throws CMException {
        Policy policy = mock(Policy.class);
        doReturn(policy).when(target).getPolicy();
        when(policy.getCMServer()).thenReturn(policyCMServer);
        assertEquals(policyCMServer, target.getCmServer());
    }

    @Test
    public void shouldGetCurrentRootSiteId() throws CMException {
        doReturn(policyCMServer).when(target).getCmServer();
        doReturn(configUtil).when(target).getConfiguration(policyCMServer);
        Policy topPolicy = mock(Policy.class);
        VersionedContentId id = mock(VersionedContentId.class);
        when(contentSession.getTopPolicy()).thenReturn(topPolicy);
        when(topPolicy.getContentId()).thenReturn(id);
        when(configUtil.getCurrentRootSiteId(id)).thenReturn(siteId);
        assertEquals(siteId, target.getCurrentRootSiteId());
    }

    interface CustomCategorizationProvider extends CategorizationProvider, Policy  {
        
    }
}
