/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove.widget;

import static org.mockito.Mockito.*;
import static junit.framework.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.atex.plugins.brightcove.BrightcoveConfigPolicy;
import com.atex.plugins.brightcove.BrightcoveVideoPolicy;
import com.atex.plugins.brightcove.service.BrightcoveService;
import com.atex.plugins.brightcove.util.BrightCoveVideoUploader;
import com.atex.plugins.brightcove.util.ConfigurationUtil;
import com.atex.plugins.brightcove.widget.OBrightCoveUploadWidget.FileUploadListner;
import com.atex.plugins.brightcove.widget.OBrightCoveUploadWidget.UpdateMetadataEventListener;
import com.brightcove.mediaapi.exceptions.BrightcoveException;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.ContentSession;
import com.polopoly.cm.app.PolicyWidget;
import com.polopoly.cm.app.search.categorization.CategorizationProvider;
import com.polopoly.cm.app.widget.OFCKEditorPolicyWidget;
import com.polopoly.cm.app.widget.OTextAreaPolicyWidget;
import com.polopoly.cm.app.widget.OTextInputPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.impl.OrchidContextImpl;
import com.polopoly.orchid.event.OrchidEvent;
import com.polopoly.orchid.widget.OFileInput;
import com.polopoly.orchid.widget.OJavaScript;
import com.polopoly.orchid.widget.OSubmitButton;
import com.polopoly.orchid.widget.OWidget;

public class OBrightCoveUploadWidgetTest {

    OBrightCoveUploadWidget target;

    @Mock
    OrchidContextImpl oc;
    @Mock
    OrchidEvent oe;
    @Mock
    BrightcoveConfigPolicy brightcoveConfigPolicy;
    @Mock
    ContentSession contentSession;
    @Mock
    PolicyWidget policyWidget;
    @Mock
    PolicyCMServer policyCMServer;
    @Mock
    BrightcoveVideoPolicy contentPolicy;
    @Mock
    OTextInputPolicyWidget textInput;
    @Mock
    OWidget childWidget;
    @Mock
    Policy video;
    @Mock
    Policy lead;
    @Mock
    OFileInput fileInput;
    @Mock
    OSubmitButton submitButton;
    @Mock
    OJavaScript javaScript;
    @Mock
    OTextInputPolicyWidget videoId;
    @Mock
    OWidget videoIdWidget;
    @Mock
    BrightCoveVideoUploader uploader;
    @Mock
    InputStream is;
    @Mock
    BrightcoveVideoPolicy brightcoveVideoPolicy;
    @Mock
    BrightcoveService brightcoveService;
    @Mock
    OTextInputPolicyWidget oTextInputPolicyWidget;
    @Mock
    OTextAreaPolicyWidget oTextAreaPolicyWidget;
    @Mock
    OFCKEditorPolicyWidget oFCKEditorPolicyWidget;
    @Mock
    Tag categorizationProvider;
    @Mock
    ConfigurationUtil configUtil;

    String siteId = "siteId";

    @Before
    public void before() throws OrchidException, CMException, IOException {
        MockitoAnnotations.initMocks(this);

        when(contentSession.getPolicyCMServer()).thenReturn(policyCMServer);
        when(contentSession.getTopPolicy()).thenReturn(contentPolicy);
        when(contentSession.getTopWidget()).thenReturn(policyWidget);
        when(contentSession.findPolicyWidget(OBrightCoveUploadWidget.BCVIDEO_RESOURCE_VIDEOID_FIELD)).thenReturn(textInput);
        when(textInput.getChildWidget()).thenReturn(childWidget);
        when(contentPolicy.getChildPolicy("lead")).thenReturn(lead);
        when(contentPolicy.getChildPolicy("video")).thenReturn(video);
        when(lead.getComponent("value")).thenReturn("lead");
        when(contentPolicy.getName()).thenReturn("name");
        when(policyCMServer.getPolicy(BrightcoveConfigPolicy.CONTENT_ID)).thenReturn(brightcoveConfigPolicy);
        when(brightcoveConfigPolicy.getWriteToken("siteId")).thenReturn("writetoken");

        target = spy(new OBrightCoveUploadWidget());
        doReturn(contentSession).when(target).getContentSession();
        doReturn(fileInput).when(target).getOFileInput();
        doReturn(submitButton).when(target).getOSubmitButton();
        doReturn(javaScript).when(target).getOJavaScript();
        doReturn(policyCMServer).when(target).getCmServer();
        target.initSelf(oc);

        when(fileInput.getFileData()).thenReturn(is);
        when(fileInput.getFileName()).thenReturn("filename");
        doReturn(uploader).when(target).getUploader("writetoken");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldSuccesfullyUploadVideo() throws OrchidException, IOException, CMException, BrightcoveException {
        doReturn(siteId).when(target).getCurrentRootSiteId();
        target.localRender(oc);
        when(uploader.upload(eq(contentPolicy), eq(is), anyString(), anyString(), any(Map.class))).thenReturn("1234");
        when(contentSession.findPolicyWidget(OBrightCoveUploadWidget.BCVIDEO_RESOURCE_VIDEOID_FIELD)).thenReturn(videoId);
        when(videoId.getChildWidget()).thenReturn(videoIdWidget);
        when(videoIdWidget.getCompoundId()).thenReturn("1235");
        FileUploadListner uplloadListener = target.new FileUploadListner();
        uplloadListener.processEvent(oc, oe);
        verify(target, never()).handleErrors(eq(oc), anyString());
    }

    @Test
    public void shouldShowErrorWhenWriteTokenIsEmpty() throws OrchidException, IOException, CMException, BrightcoveException {
        doReturn(siteId).when(target).getCurrentRootSiteId();
        target.localRender(oc);
        when(brightcoveConfigPolicy.getWriteToken()).thenReturn("");
        FileUploadListner uplloadListener = target.new FileUploadListner();
        uplloadListener.processEvent(oc, oe);
        verify(target).handleErrors(eq(oc), anyString());
    }

    @Test
    public void shouldShowErrorWhenWriteTokenIsNull() throws OrchidException, IOException, CMException, BrightcoveException {
        doReturn(siteId).when(target).getCurrentRootSiteId();
        target.localRender(oc);
        when(brightcoveConfigPolicy.getWriteToken()).thenReturn(null);
        FileUploadListner uplloadListener = target.new FileUploadListner();
        uplloadListener.processEvent(oc, oe);
        verify(target).handleErrors(eq(oc), anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldShowErrorWhenNameIsNullOrEmpty() throws OrchidException, IOException, CMException, BrightcoveException {
        target.localRender(oc);
        when(contentPolicy.getName()).thenReturn("");
        when(uploader.upload(eq(contentPolicy), eq(is), anyString(), anyString(), any(Map.class))).thenReturn("");
        FileUploadListner uplloadListener = target.new FileUploadListner();
        uplloadListener.processEvent(oc, oe);
        verify(target, times(1)).handleErrors(eq(oc), anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldShowErrorWhenDesciptionIsNullOrEmpty() throws OrchidException, IOException, CMException, BrightcoveException {
        target.localRender(oc);
        when(lead.getComponent("value")).thenReturn("");
        when(uploader.upload(eq(contentPolicy), eq(is), anyString(), anyString(), any(Map.class))).thenReturn("");
        FileUploadListner uplloadListener = target.new FileUploadListner();
        uplloadListener.processEvent(oc, oe);
        verify(target, times(1)).handleErrors(eq(oc), anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldShowErrorWhenVideoIdIsEmpty() throws OrchidException, IOException, CMException, BrightcoveException {
        doReturn(siteId).when(target).getCurrentRootSiteId();
        target.localRender(oc);
        when(uploader.upload(eq(contentPolicy), eq(is), anyString(), anyString(), any(Map.class))).thenReturn("");
        FileUploadListner uplloadListener = target.new FileUploadListner();
        uplloadListener.processEvent(oc, oe);
        verify(target, times(1)).handleErrors(eq(oc), anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldShowErrorWhenIOException() throws OrchidException, IOException, CMException, BrightcoveException {
        doReturn(siteId).when(target).getCurrentRootSiteId();
        target.localRender(oc);
        when(fileInput.getFileData()).thenThrow(new IOException("Unit test generated Exception"));
        when(uploader.upload(eq(contentPolicy), eq(is), anyString(), anyString(), any(Map.class))).thenReturn("");
        FileUploadListner uplloadListener = target.new FileUploadListner();
        uplloadListener.processEvent(oc, oe);
        verify(target, times(1)).handleErrors(eq(oc), anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldShowErrorWhenCMexception() throws OrchidException, IOException, CMException, BrightcoveException {
        target.localRender(oc);
        when(contentPolicy.getChildPolicy(anyString())).thenThrow(new CMException("Unit test generated Exception"));
        when(uploader.upload(eq(contentPolicy), eq(is), anyString(), anyString(), any(Map.class))).thenReturn("");
        FileUploadListner uplloadListener = target.new FileUploadListner();
        uplloadListener.processEvent(oc, oe);
        verify(target, times(1)).handleErrors(eq(oc), anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldShowErrorWhenBrightcoveException() throws OrchidException, IOException, CMException, BrightcoveException {
        doReturn(siteId).when(target).getCurrentRootSiteId();
        target.localRender(oc);
        doThrow(new BrightcoveException("Unit test generated Exception")).when(target).upload(eq(contentPolicy), eq(fileInput), anyString(), any(Map.class));
        FileUploadListner uplloadListener = target.new FileUploadListner();
        uplloadListener.processEvent(oc, oe);
        verify(target, times(1)).handleErrors(eq(oc), anyString());
    }

    @Test(expected=NullPointerException.class)
    @SuppressWarnings("unchecked")
    public void shouldUploadVideoNotPopulateVideoId() throws OrchidException, IOException, CMException, BrightcoveException {
        doReturn(siteId).when(target).getCurrentRootSiteId();
        target.localRender(oc);
        when(contentSession.findPolicyWidget(OBrightCoveUploadWidget.BCVIDEO_RESOURCE_VIDEOID_FIELD)).thenReturn(null);
        when(uploader.upload(eq(contentPolicy), eq(is), anyString(), anyString(), any(Map.class))).thenReturn("1234");
        FileUploadListner uplloadListener = target.new FileUploadListner();
        uplloadListener.processEvent(oc, oe);
        verify(target, never()).handleErrors(eq(oc), anyString());
    }

    interface Tag extends CategorizationProvider, Policy {};

    @Test
    public void shouldUpdateMetadata() throws OrchidException, CMException {
        doReturn(siteId).when(target).getCurrentRootSiteId();
        doReturn(brightcoveVideoPolicy).when(target).getBrightcoveVideoPolicy();
        doReturn(brightcoveService).when(target).getBrightcoveService();
        doReturn("123456789").when(brightcoveVideoPolicy).getId();
        doNothing().when(target).updateTextField(brightcoveVideoPolicy);

        UpdateMetadataEventListener event = target.new UpdateMetadataEventListener();
        event.processEvent(oc, oe);
        verify(target).updateTextField(brightcoveVideoPolicy);
    }

    @Test
    public void shouldUpdateMetadataNullBranch() throws OrchidException, CMException {
        doReturn(brightcoveVideoPolicy).when(target).getBrightcoveVideoPolicy();
        doReturn(null).when(brightcoveVideoPolicy).getId();
        UpdateMetadataEventListener event = target.new UpdateMetadataEventListener();
        event.processEvent(oc, oe);
        assertTrue((target).getBrightcoveVideoPolicy().getId() == null);
    }

    @Test
    public void shouldUpdateMetadataEmptyBranch() throws OrchidException, CMException {
        doReturn(brightcoveVideoPolicy).when(target).getBrightcoveVideoPolicy();
        doReturn("").when(brightcoveVideoPolicy).getId();
        UpdateMetadataEventListener event = target.new UpdateMetadataEventListener();
        event.processEvent(oc, oe);
        assertTrue((target).getBrightcoveVideoPolicy().getId().isEmpty());
    }

    @Test
    public void shouldUpdateTextField() throws OrchidException, CMException {
        doReturn("Metadata-Test").when(brightcoveVideoPolicy).getName();
        doReturn(contentSession).when(target).getContentSession();
        doReturn(oTextInputPolicyWidget).when(contentSession).findPolicyWidget("menu/standard/brightcovecolumholder/brightcovecolumn/name");
        doReturn(childWidget).when(oTextInputPolicyWidget).getChildWidget();
        doReturn("name_123").when(childWidget).getCompoundId();

        doReturn("Metadata-Test-Short-Description").when(brightcoveVideoPolicy).getShortDescription();
        doReturn(contentSession).when(target).getContentSession();
        doReturn(oTextAreaPolicyWidget).when(contentSession).findPolicyWidget("menu/standard/brightcovecolumholder/brightcovecolumn/lead");
        doReturn(childWidget).when(oTextAreaPolicyWidget).getChildWidget();
        doReturn("short_123").when(childWidget).getCompoundId();

        doReturn("Metadata-Test-Long-Description").when(brightcoveVideoPolicy).getLongDescription();
        doReturn(contentSession).when(target).getContentSession();
        doReturn(oFCKEditorPolicyWidget).when(contentSession).findPolicyWidget("menu/standard/body");
        doReturn(childWidget).when(oFCKEditorPolicyWidget).getChildWidget();
        doReturn("long_123").when(childWidget).getCompoundId();

        target.updateTextField(brightcoveVideoPolicy);
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

}
