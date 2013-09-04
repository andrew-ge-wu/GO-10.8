/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import com.atex.plugins.brightcove.BrightcoveConfigPolicy;
import com.atex.plugins.brightcove.util.CategorizationService;
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
public class BrightcoveService {
    
    private static final EnumSet<VideoFieldEnum> FIELDS = VideoFieldEnum.CreateFullEnumSet();
    private static final Set<String> CUSTOM_FIELDS = new HashSet<String>(CategorizationService.CUSTOM_FIELDS);
    private static final Set<String> EMPTY = Collections.emptySet();
    
    private PolicyCMServer cmServer;
    private String siteId;
    
    public static final int LIMIT = 20;
    public static final String BC_MEDIA_URL = "http://api.brightcove.com/services/post";
    
    public BrightcoveService(final PolicyCMServer cmServer) {
        this.cmServer = cmServer;
        this.siteId = null;
    }

    public BrightcoveService(final PolicyCMServer cmServer, String siteId) {
        this.cmServer = cmServer;
        this.siteId = siteId;
    }

    public Videos search(String keyword, Type type, Sort sort, Order order) throws CMException {
        
        ReadApi api = getReadApi();
        
        Videos result = null;
        try {
            
            String token = getReadToken();
            switch (type) {
            case NAME:
                result = api.FindVideosByText(token, keyword, LIMIT, 0, FIELDS, CUSTOM_FIELDS);
                break;
                
            case TAG:
                Set<String> tags = new HashSet<String>(Arrays.asList(keyword.split(" ")));
                result = api.FindVideosByTags(token, 
                        EMPTY, 
                        tags, 
                        LIMIT, 
                        0, 
                        SortByTypeEnum.valueOf(sort.toString()), 
                        SortOrderTypeEnum.valueOf(order.toString()), FIELDS, CUSTOM_FIELDS);
                break;
                
            case REFERENCE_ID:
                Set<String> ids = new HashSet<String>(Arrays.asList(keyword.split(" ")));
                result = api.FindVideosByReferenceIds(token, ids, FIELDS, CUSTOM_FIELDS);
                break;
                
            }
        } catch (BrightcoveException e) {
            throw new CMException(e.getMessage(), e);
        }
        
        return result;
    }
    
    public Video findByVideoID(long id) throws CMException, BrightcoveException {
        
        ReadApi api = getReadApi();
        return api.FindVideoById(getReadToken(), id, FIELDS, CUSTOM_FIELDS);        
    }
    
    public void deleteVideo(long id) throws CMException, BrightcoveException {
        
        WriteApi api = getWriteApi();
        api.DeleteVideo(getWriteToken(), id, null, true, true);
    }

    public void updateVideo(Video video) throws CMException, BrightcoveException {
        WriteApi api = getWriteApi();
        api.UpdateVideo(getWriteToken(), video);
    }

    public String addVideo(String json, InputStream is, String fileName) throws ClientProtocolException, IOException {
        HttpClient httpclient = getHttpClient();
        try {
            File video = new File(fileName);
            OutputStream out = new FileOutputStream(video);
            IOUtils.copy(is, out);
            httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpPost post = new HttpPost(BC_MEDIA_URL);
            MultipartEntity multipart = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            ContentBody content = new FileBody(video, new MimetypesFileTypeMap().getContentType(fileName));
            multipart.addPart("JSON-RPC", new StringBody(json));
            multipart.addPart("file", content);
            post.setEntity(multipart);
            HttpResponse response = httpclient.execute(post);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                return result;
            }
        } finally {
            httpclient.getConnectionManager().shutdown();            
        }
        return "";
    }
    
    public Videos getVideos(long minutes, int limit) throws CMException, BrightcoveException {
        ReadApi readApi = getReadApi();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -(int)minutes);
        long timeInMinutes = calendar.getTimeInMillis()/1000l/60l;

        return readApi.FindModifiedVideos(
                getReadToken(), 
                timeInMinutes, 
                Collections.singleton(VideoStateFilterEnum.PLAYABLE), 
                limit, 
                0, 
                SortByTypeEnum.MODIFIED_DATE, 
                SortOrderTypeEnum.DESC, 
                FIELDS, CUSTOM_FIELDS);
    }
    
    HttpClient getHttpClient() {
        return new DefaultHttpClient();
    }

    protected String getSiteId() {
        return siteId;
    }

    String getWriteToken() throws CMException {
        String siteId = getSiteId();
        if (siteId!=null) {
            return ((BrightcoveConfigPolicy)cmServer.getPolicy(BrightcoveConfigPolicy.CONTENT_ID)).getWriteToken(siteId);
        }
        return ((BrightcoveConfigPolicy)cmServer.getPolicy(BrightcoveConfigPolicy.CONTENT_ID)).getWriteToken();
    }

    String getReadToken() throws CMException {
        String siteId = getSiteId();
        if (siteId!=null) {
            return ((BrightcoveConfigPolicy)cmServer.getPolicy(BrightcoveConfigPolicy.CONTENT_ID)).getReadToken(siteId);
        }
        return ((BrightcoveConfigPolicy)cmServer.getPolicy(BrightcoveConfigPolicy.CONTENT_ID)).getReadToken();
    }

    ReadApi getReadApi() {
        return new ReadApi();
    }
    
    WriteApi getWriteApi() {
        return new WriteApi();
    }
    
}
