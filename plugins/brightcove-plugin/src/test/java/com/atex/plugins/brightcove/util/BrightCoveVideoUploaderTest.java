/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.atex.plugins.brightcove.BrightcoveVideoPolicy;
import com.atex.plugins.brightcove.service.BrightcoveService;
import com.brightcove.mediaapi.exceptions.BrightcoveException;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.app.search.categorization.Categorization;
import com.polopoly.cm.app.search.categorization.CategorizationBuilder;
import com.polopoly.cm.app.search.categorization.CategorizationProvider;
import com.polopoly.cm.app.search.categorization.Category;
import com.polopoly.cm.app.search.categorization.CategoryBuilder;
import com.polopoly.cm.app.search.categorization.Dimension;
import com.polopoly.cm.app.search.categorization.DimensionBuilder;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

public class BrightCoveVideoUploaderTest {

    BrightCoveVideoUploader target;
    VersionedContentId vcid;
    ContentId cid;
    String writeToken;
    String fileName;
    String siteId;

    BrightcoveVideoPolicy contentPolicy;
    @Mock
    InputStream is;
    @Mock
    Tag categorizationProvider;
    @Mock
    PolicyCMServer policyCMServer;
    @Mock
    BrightcoveService bcService;
    
    @Mock
    Content content;
    @Mock
    InputTemplate inputTemplate;
    @Mock
    Policy parentPolicy;

    String sampleJson = "{\"method\":\"create_video\",\"params\":{\"token\":\"wr1t370k3n\",\"video\":{\"tags\":[\"name\"],\"customFields\":{},\"shortDescription\":\"short desc\",\"name\":\"videoName\",\"longDescription\":\"long desc\"}}}";
    String invalidToken = "{\"error\": {\"name\":\"InvalidTokenError\",\"message\":\"invalid token\",\"code\":210}, \"result\": null, \"id\": null}";
    String success ="{\"result\": 1779142699001, \"error\": null, \"id\": null}";
    String videoId = "1779142699001";
    
    Map<String, String> EMPTY_MAP = new HashMap<String, String>();
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        writeToken = "wr1t370k3n";
        fileName = "v1d30.flv";
        siteId = "siteId";
        contentPolicy = spy(new BrightcoveVideoPolicy() {
            @Override
            protected void initSelf() {}
        });
        contentPolicy.init("PolicyName", new Content[] { content }, inputTemplate, parentPolicy, policyCMServer);
        target = spy(new BrightCoveVideoUploader(writeToken, siteId) {
        });
        doReturn(bcService).when(target).getBrightcoveService();
    }

    private void mockRequiredField() throws CMException {
        doReturn("videoName").when(contentPolicy).getName();
        SingleValuePolicy shortDescPolicy = mock(SingleValuePolicy.class);
        doReturn(shortDescPolicy).when(contentPolicy).getChildPolicy("lead");
        when(shortDescPolicy.getComponent("value")).thenReturn("short desc");
    }

    private void mockLongDesc() throws CMException {
        SingleValuePolicy longDescPolicy = mock(SingleValuePolicy.class);
        doReturn(longDescPolicy).when(contentPolicy).getChildPolicy("body");
        when(longDescPolicy.getComponent("value")).thenReturn("long desc");
    }

    private void mockEmptyLongDesc() throws CMException {
        SingleValuePolicy longDescPolicy = mock(SingleValuePolicy.class);
        doReturn(longDescPolicy).when(contentPolicy).getChildPolicy("body");
        when(longDescPolicy.getComponent("value")).thenReturn("");
    }

    private void mockNullLongDesc() throws CMException {
        SingleValuePolicy longDescPolicy = mock(SingleValuePolicy.class);
        doReturn(longDescPolicy).when(contentPolicy).getChildPolicy("body");
        when(longDescPolicy.getComponent("value")).thenReturn(null);
    }

    private void mockCategorizationTags() throws CMException {
        Category cat = new CategoryBuilder().id("id").withName("name").build();
        Dimension dim = new DimensionBuilder().withName("dimName").withCategories(cat).build();
        Categorization categorization = new CategorizationBuilder().withDimensions(dim).build();
        doReturn(categorizationProvider).when(contentPolicy).getChildPolicy("categorization");
        when(categorizationProvider.getCategorization()).thenReturn(categorization);
    }

    private void mockEmptyCategorizationTags() throws CMException {
        Categorization categorization = new CategorizationBuilder().build();
        doReturn(categorizationProvider).when(contentPolicy).getChildPolicy("categorization");
        when(categorizationProvider.getCategorization()).thenReturn(categorization);
    }

    private void mockContentId() throws CMException {
        vcid = new VersionedContentId(1,234,5);
        cid = vcid.getContentId(); 
        doReturn(contentPolicy).when(policyCMServer).getPolicy(cid);
        when(contentPolicy.getContentId()).thenReturn(vcid);
    }

    @Test
    public void testUpload() throws BrightcoveException, CMException, IOException {
        String result;
        mockRequiredField();
        mockLongDesc();
        mockCategorizationTags();
        mockContentId();
        when(bcService.addVideo(anyString(), eq(is), eq(fileName))).thenReturn(success);
        result = target.upload(contentPolicy, is, writeToken, fileName, EMPTY_MAP);
        assertEquals(videoId, result);
    }

    @Test
    public void testUploadWithNullLongDesc() throws BrightcoveException, CMException, IOException {
        String result;
        mockRequiredField();
        mockNullLongDesc();
        mockEmptyCategorizationTags();
        mockContentId();
        when(bcService.addVideo(anyString(), eq(is), eq(fileName))).thenReturn(success);
        result = target.upload(contentPolicy, is, writeToken, fileName, EMPTY_MAP);
        assertEquals(videoId, result);
    }

    @Test
    public void testUploadWithNoLongDescEmptyTags() throws CMException, BrightcoveException, ClientProtocolException, IOException {
        String result;
        mockRequiredField();
        mockEmptyLongDesc();
        mockEmptyCategorizationTags();
        mockContentId();
        when(bcService.addVideo(anyString(), eq(is), eq(fileName))).thenReturn(success);
        result = target.upload(contentPolicy, is, writeToken, fileName, EMPTY_MAP);
        assertEquals(videoId, result);
    }

    @Test
    public void shouldCMExceptionWhenUpload() throws BrightcoveException, CMException, IOException {
        String result;
        doThrow(new CMException("Unit test generated exception")).when(contentPolicy).getName();
        result = target.upload(contentPolicy, is, writeToken, fileName, EMPTY_MAP);
        assertEquals("", result);
    }

    @Test
    public void shouldIOExceptionWhenUpload() throws BrightcoveException, CMException, IOException {
        String result;
        mockRequiredField();
        mockLongDesc();
        mockCategorizationTags();
        mockContentId();
        when(bcService.addVideo(anyString(), eq(is), eq(fileName))).thenThrow(new IOException("Unit test generated exception"));
        result = target.upload(contentPolicy, is, writeToken, fileName, EMPTY_MAP);
        assertEquals("", result);
    }

    @Test(expected=BrightcoveException.class)
    public void testInvalidToken() throws CMException, IOException, BrightcoveException {
        String result;
        mockRequiredField();
        mockLongDesc();
        mockCategorizationTags();
        mockContentId();
        when(bcService.addVideo(anyString(), eq(is), eq(fileName))).thenReturn(invalidToken);
        result = target.upload(contentPolicy, is, "invalid", fileName, EMPTY_MAP);
        assertEquals("", result);
    }

    @Test
    public void testInvalidFeedbackJson() throws CMException, IOException, BrightcoveException {
        String result;
        mockRequiredField();
        mockLongDesc();
        mockCategorizationTags();
        mockContentId();
        when(bcService.addVideo(anyString(),eq(is), eq(fileName))).thenReturn("");
        result = target.upload(contentPolicy, is, writeToken, fileName, EMPTY_MAP);
        assertEquals("", result);
    }

    interface Tag extends CategorizationProvider, Policy {} ;
}
