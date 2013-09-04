/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.atex.plugins.brightcove.BrightcoveConfigPolicy;
import com.atex.plugins.brightcove.service.BrightcoveService;
import com.atex.plugins.brightcove.service.Order;
import com.atex.plugins.brightcove.service.Sort;
import com.atex.plugins.brightcove.service.Type;
import com.brightcove.commons.catalog.objects.Video;
import com.brightcove.commons.catalog.objects.Videos;
import com.brightcove.commons.catalog.objects.enumerations.SortByTypeEnum;
import com.brightcove.commons.catalog.objects.enumerations.SortOrderTypeEnum;
import com.brightcove.commons.catalog.objects.enumerations.VideoFieldEnum;
import com.brightcove.commons.catalog.objects.enumerations.VideoStateFilterEnum;
import com.brightcove.mediaapi.exceptions.BrightcoveException;
import com.brightcove.mediaapi.wrapper.ReadApi;
import com.brightcove.mediaapi.wrapper.WriteApi;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;

/**
 *
 */
public class BrightcoveServiceTest {
    
    BrightcoveService target;
    @Mock
    ReadApi readApi;
    @Mock
    WriteApi writeApi;
    @Mock
    PolicyCMServer cmServer;
    @Mock
    InputStream is;
    @Mock
    HttpClient httpClient;
    @Mock
    HttpParams httpParams;
    @Mock
    ClientConnectionManager clientConnectionManager;
    @Mock
    HttpResponse httpResponse;
    @Mock
    HttpEntity httpEntity;
    @Mock
    Video video;
    @Mock
    Videos videos;
    @Mock
    BrightcoveConfigPolicy configPolicy;

    String fileName = "small.flv";
    String readToken = "read";
    String writeToken = "write";
    
    int limit = BrightcoveService.LIMIT;
    
    @Before
    public void before() throws CMException, ClientProtocolException, IOException {
        MockitoAnnotations.initMocks(this);
        when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
        when(httpClient.getParams()).thenReturn(httpParams);
        when(httpClient.getConnectionManager()).thenReturn(clientConnectionManager);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        target = spy(new BrightcoveService(cmServer));
        doReturn(httpClient).when(target).getHttpClient();
        doReturn(readApi).when(target).getReadApi();
        doReturn(writeApi).when(target).getWriteApi();
    }
    
    @Test
    public void shouldSearchByNameAndDescription() throws BrightcoveException, CMException {
        doReturn(readToken).when(target).getReadToken();
        target.search("q", Type.NAME, Sort.CREATION_DATE, Order.DESC);
        verify(readApi).FindVideosByText(eq(readToken), eq("q"), eq(Integer.valueOf(limit)), eq(Integer.valueOf(0)), any(EnumSet.class), any(Set.class));
    }
    
    @Test
    public void shouldSearchTags() throws BrightcoveException, CMException {
        doReturn(readToken).when(target).getReadToken();
        target.search("tag1 tag2", Type.TAG, Sort.CREATION_DATE, Order.DESC);
        verify(readApi).FindVideosByTags(anyString(), any(Set.class), eq(new HashSet<String>(Arrays.asList("tag1 tag2".split(" ")))), eq(Integer.valueOf(limit)), eq(Integer.valueOf(0)), eq(SortByTypeEnum.valueOf(Sort.CREATION_DATE.toString())), eq(SortOrderTypeEnum.valueOf(Order.DESC.toString())), any(EnumSet.class), any(Set.class));
    }
    
    @Test
    public void shouldSerchReferenceId() throws BrightcoveException, CMException {
        doReturn(readToken).when(target).getReadToken();
        target.search("reference-id1 reference-id2", Type.REFERENCE_ID, Sort.CREATION_DATE, Order.DESC);
        verify(readApi).FindVideosByReferenceIds(anyString(), eq(new HashSet<String>(Arrays.asList("reference-id1 reference-id2".split(" ")))), any(EnumSet.class), any(Set.class));
    }
    
    @Test(expected = CMException.class)
    public void shouldExpetCMExceptionWhenBrightcoveServiceIsThrowException() throws CMException, BrightcoveException {
        doReturn(readToken).when(target).getReadToken();
        when(readApi.FindVideosByText(eq(readToken), eq("q"), eq(Integer.valueOf(limit)), eq(Integer.valueOf(0)), any(EnumSet.class), any(Set.class))).thenThrow(new BrightcoveException("This is unit test created Exception"));
        target.search("q", Type.NAME, Sort.CREATION_DATE, Order.DESC);
        verify(readApi).FindVideosByText(eq(readToken), eq("q"), eq(Integer.valueOf(limit)), eq(Integer.valueOf(0)), any(EnumSet.class), any(Set.class));
    }
    
    @Test
    public void shouldAbleFindVideoById() throws CMException, BrightcoveException {
        doReturn(readToken).when(target).getReadToken();
        target.findByVideoID(1);
        verify(readApi).FindVideoById(eq(readToken), eq(Long.valueOf(1)), any(EnumSet.class), any(Set.class));
    }
    
    @Test
    public void shouldAbleDeleteVideoById() throws CMException, BrightcoveException {
        doReturn(writeToken).when(target).getWriteToken();
        target.deleteVideo(1l);
        verify(writeApi).DeleteVideo(writeToken, 1l, null, true, true);
    }

    @Test
    public void shouldAbleUpdateVideo() throws CMException, BrightcoveException {
        doReturn(writeToken).when(target).getWriteToken();
        target.updateVideo(video);
        verify(writeApi).UpdateVideo(writeToken, video);
    }

    @Test
    public void uploadToBrightcove() throws ClientProtocolException, IOException {
        when(is.read((byte[]) any())).thenReturn(-1);
        target.addVideo(getJson(), is, fileName);
    }

    @Test
    public void uploadToBrightcoveWithNullResponse() throws ClientProtocolException, IOException {
        when(httpResponse.getEntity()).thenReturn(null);
        when(is.read((byte[]) any())).thenReturn(-1);
        target.addVideo(getJson(), is, fileName);
    }

    @Test
    public void shouldGetVideos() throws CMException, BrightcoveException {
        long minutes = 5l;
        int limit = 10;
        doReturn(readToken).when(target).getReadToken();
        when(readApi.FindModifiedVideos(
                eq(readToken), anyLong(), 
                eq(Collections.singleton(VideoStateFilterEnum.PLAYABLE)), 
                eq(Integer.valueOf(limit)), eq(Integer.valueOf(0)), 
                eq(SortByTypeEnum.MODIFIED_DATE), 
                eq(SortOrderTypeEnum.DESC), 
                eq(VideoFieldEnum.CreateFullEnumSet()), 
                any(Set.class)
                )).thenReturn(videos);
        assertEquals(videos, target.getVideos(minutes, limit));
    }

    private String getJson() {
        return "{'key': 'value'}";
    }

    @Test
    public void shouldGetWriteToken() throws CMException {
        String defaultToken = "defaultToken";
        String token = "normalToken";
        String siteId = "someSiteId";
        when(cmServer.getPolicy(BrightcoveConfigPolicy.CONTENT_ID)).thenReturn(configPolicy);
        when(configPolicy.getWriteToken()).thenReturn(defaultToken);
        when(configPolicy.getWriteToken(siteId)).thenReturn(token);
        doReturn(null).when(target).getSiteId();
        assertEquals(defaultToken, target.getWriteToken());
        doReturn(siteId).when(target).getSiteId();
        assertEquals(token, target.getWriteToken());
    }

    @Test
    public void shouldGetReadToken() throws CMException {
        String defaultToken = "defaultToken";
        String token = "normalToken";
        String siteId = "someSiteId";
        when(cmServer.getPolicy(BrightcoveConfigPolicy.CONTENT_ID)).thenReturn(configPolicy);
        when(configPolicy.getReadToken()).thenReturn(defaultToken);
        when(configPolicy.getReadToken(siteId)).thenReturn(token);
        doReturn(null).when(target).getSiteId();
        assertEquals(defaultToken, target.getReadToken());
        doReturn(siteId).when(target).getSiteId();
        assertEquals(token, target.getReadToken());
    }
}
