/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.plugins.brightcove.service.BrightcoveService;
import com.brightcove.commons.catalog.objects.Video;
import com.brightcove.commons.catalog.objects.Videos;
import com.brightcove.mediaapi.exceptions.BrightcoveException;
import com.polopoly.application.Application;
import com.polopoly.application.ConnectionProperties;
import com.polopoly.application.ConnectionPropertiesConfigurationException;
import com.polopoly.application.ConnectionPropertiesParseException;
import com.polopoly.application.IllegalApplicationStateException;
import com.polopoly.application.StandardApplication;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.imagemanager.ImageFormatException;
import com.polopoly.cm.app.imagemanager.ImageTooBigException;
import com.polopoly.cm.app.policy.SelectableSubFieldPolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.EjbCmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.user.server.Caller;
import com.polopoly.user.server.UserId;
import com.polopoly.cm.app.imagemanager.ImageManagerPolicy;

/**
 * This is importer client for Brightcove can run as normal standalone java application
 * periodically via cron job
 * @since 1.1.0
 */
public class Importer {

    private static final Logger LOGGER = Logger.getLogger(Importer.class.getName());
    public static final String TEMPLATE = "com.atex.plugins.brightcove.video.MainElement";
    public static final String URL = "http://localhost:8081/connection-properties/connection.properties";
    public static final String SECURITY_PARENT = "GreenfieldTimes.d";
    public static final String USER = "sysadmin";
    public static final long MINUTES = TimeUnit.DAYS.toMinutes(1);
    public static final int LIMIT = 100;
    public static final String SITEID = null;
    public static final String PREFIX = "brightcove_video_";
    
    private String url = URL;
    private String securityParent = SECURITY_PARENT;
    private String user = USER;
    private long minutes = MINUTES;
    private int limit = LIMIT;
    private String siteId = SITEID;
    private PolicyCMServer policyCMServer;
    private Application application;
    private BrightcoveService brightcoveService;

    /**
     * Initialize the importer
     * 
     * @throws IllegalArgumentException
     * @throws ConnectionPropertiesParseException
     * @throws MalformedURLException
     * @throws IOException
     * @throws ConnectionPropertiesConfigurationException
     * @throws IllegalApplicationStateException
     * @throws CMException
     */
    public void init() throws IllegalArgumentException, ConnectionPropertiesParseException, MalformedURLException, IOException, ConnectionPropertiesConfigurationException, IllegalApplicationStateException, CMException {
        
        EjbCmClient cmClient = getCmClient();
        
        application = getApplication();
        application.addApplicationComponent(cmClient);
        application.init();
        setPolicyCMServer(cmClient.getPolicyCMServer());
        Caller caller = new Caller(new UserId(user));
        getPolicyCMServer().setCurrentCaller(caller);
        setBrightcoveService(new BrightcoveService(getPolicyCMServer(), siteId));
    }

    /**
     * run the import step
     * @throws BrightcoveException 
     * @throws CMException 
     * @throws ImageFormatException 
     * @throws IOException 
     * @throws MalformedURLException 
     */
    public void run() throws CMException, BrightcoveException, MalformedURLException, IOException, ImageFormatException, ImageTooBigException {
        Videos videos = getBrightcoveVideosFromRemote();
        LOGGER.log(Level.INFO, "Found " + videos.size() + " new videos from Brightcove to be import to Polopoly.");
        for(Video video: videos) {
            create(video);
        }
    }

    void create(Video video) throws CMException, MalformedURLException, IOException, ImageFormatException, ImageTooBigException {
        if(video.getVideoStillUrl() == null && video.getThumbnailUrl() == null) {
            LOGGER.log(Level.WARNING, "Video with ID " + video.getId() + " does not have still video image or thumbnail, skiping import.");
        } else {
            application.triggerHeartbeat();
            BrightcoveVideoPolicy brightcoveVideo = null;
            if(!getPolicyCMServer().contentExists(new ExternalContentId(PREFIX + video.getId()))) {
                brightcoveVideo = (BrightcoveVideoPolicy) getPolicyCMServer().createContent(1, new ExternalContentId(TEMPLATE));
                brightcoveVideo.setExternalId(PREFIX + video.getId());
                brightcoveVideo.setSecurityParentId(new ExternalContentId(securityParent));
                LOGGER.log(Level.INFO, "Imported video with ID " + video.getId() + " as Polopoly content " + brightcoveVideo.getContentId().getContentIdString());            
            } else {
                brightcoveVideo = (BrightcoveVideoPolicy) getPolicyCMServer().getPolicy(new ExternalContentId(PREFIX + video.getId()));
                brightcoveVideo = (BrightcoveVideoPolicy) getPolicyCMServer().createContentVersion(brightcoveVideo.getContentId());
                LOGGER.log(Level.INFO, "Brightcove video " + PREFIX + video.getId() + " is already created before, update it.");
            }
            brightcoveVideo.setId(String.valueOf(video.getId()));
            brightcoveVideo.setName(video.getName());
            brightcoveVideo.setShortDescription(video.getShortDescription());
            brightcoveVideo.setLongDescription(video.getLongDescription());
            BrightcoveConfigPolicy config = (BrightcoveConfigPolicy) getPolicyCMServer().getPolicy(BrightcoveConfigPolicy.CONTENT_ID);
            brightcoveVideo.mergeCategorization(video.getTags(), video.getCustomFields(), config.getMappings());
            SelectableSubFieldPolicy imageType = (SelectableSubFieldPolicy) brightcoveVideo.getChildPolicy("imageType");
            imageType.setSelectedSubFieldName(BrightcoveVideoPolicy.IMAGE);
            ImageManagerPolicy image = (ImageManagerPolicy) imageType.getChildPolicy(BrightcoveVideoPolicy.IMAGE);
            URL url = new URL(video.getVideoStillUrl() == null? video.getThumbnailUrl(): video.getVideoStillUrl());
            URLConnection conn = url.openConnection();
            image.importImage("image/image.png", conn.getInputStream());

            brightcoveVideo.commit();
        }
    }

    EjbCmClient getCmClient() throws ConnectionPropertiesConfigurationException, IllegalApplicationStateException, IllegalArgumentException, ConnectionPropertiesParseException, MalformedURLException, IOException {
        EjbCmClient cmClient = new EjbCmClient();
        ConnectionProperties connectionProperties = new ConnectionProperties(new URL(url));
        cmClient.readConnectionProperties(connectionProperties);
        return cmClient;
    }

    Application getApplication() {
        return new StandardApplication("brightcove-importer");
    }

    Videos getBrightcoveVideosFromRemote() throws CMException, BrightcoveException {
        return getBrightcoveService().getVideos(minutes, limit);
    }

    PolicyCMServer getPolicyCMServer() {
        return policyCMServer;
    }

    void setPolicyCMServer(final PolicyCMServer policyCMServer) {
        this.policyCMServer = policyCMServer;
    }

    BrightcoveService getBrightcoveService() {
        return brightcoveService;
    }

    void setBrightcoveService(final BrightcoveService brightcoveService) {
        this.brightcoveService = brightcoveService;
    }

    /**
     * shutdown importer
     */
    public void shutdown() {
        application.destroy();
    }

    /**
     * set the url of the connection properties
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Set the security parent of the created content
     * @param securityParent
     */
    public void setSecurityParent(String securityParent) {
        this.securityParent = securityParent;
    }

    /**
     * Set the user used to import content
     * @param user
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * only get videos since <code>minutes</code> ago, default is 1 day ago.
     * @param minutes
     */
    public void setMinutes(long minutes) {
        this.minutes = minutes;
    }

    /**
     * set the limit of the video to get from Brightcove, default is 100
     * @param limit
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * set the site configuration to be use default is the first configuration in the list
     * @param siteId
     */
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

}
