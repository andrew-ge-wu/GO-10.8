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
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.atex.plugins.brightcove.service.BrightcoveService;
import com.brightcove.commons.catalog.objects.Video;
import com.brightcove.commons.catalog.objects.Videos;
import com.brightcove.mediaapi.exceptions.BrightcoveException;
import com.polopoly.application.Application;
import com.polopoly.application.ConnectionPropertiesConfigurationException;
import com.polopoly.application.ConnectionPropertiesParseException;
import com.polopoly.application.IllegalApplicationStateException;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.imagemanager.ImageFormatException;
import com.polopoly.cm.app.imagemanager.ImageManagerPolicy;
import com.polopoly.cm.app.imagemanager.ImageTooBigException;
import com.polopoly.cm.app.policy.SelectableSubFieldPolicy;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.app.search.categorization.CategorizationProvider;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.EjbCmClient;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

/**
 *
 */
public class ImporterTest {

    Importer target;

    @Mock
    BrightcoveService brightcoveService;
    @Mock
    PolicyCMServer policyCMServer;
    @Mock
    Application application;
    @Mock
    EjbCmClient ejbCmClient;
    @Mock
    private HashMap<String, Policy> children1;
    @Mock
    Content content1;
    @Mock
    InputTemplate inputTemplate;
    @Mock
    Policy parent;
    @Mock
    Content content2;
    BrightcoveVideoPolicy videoPolicy1;
    @Mock
    SingleValuePolicy id1;
    @Mock
    VersionedContentId videoVersionContentId1;
    BrightcoveVideoPolicy videoPolicy2;
    @Mock
    VersionedContentId videoVersionContentId2;
    @Mock
    ContentId videoContentId2;
    @Mock
    FakeSingleValued singleValued;
    @Mock
    FakeCategorizationProvider categorizationProvider;
    @Mock
    SelectableSubFieldPolicy selectableSubFieldPolicy;
    @Mock
    ImageManagerPolicy imageManagerPolicy;
    Videos videos;
    Video video1;
    Video video2;
    Video video3;
    @Mock
    BrightcoveConfigPolicy configPolicy;

    @Before
    public void before() throws CMException, BrightcoveException, ConnectionPropertiesConfigurationException, IllegalArgumentException, ConnectionPropertiesParseException, MalformedURLException, IllegalApplicationStateException, IOException, ImageFormatException, ImageTooBigException {
        MockitoAnnotations.initMocks(this);
        video1 = new Video();
        video1.setId(1l);
        video1.setShortDescription("short description 1");
        video1.setLongDescription("long description 1");
        video1.setVideoStillUrl("http://atex.com");
        video2 = new Video();
        video2.setId(2l);
        video2.setShortDescription("short description 2");
        video2.setLongDescription("long description 2");
        video2.setThumbnailUrl("http://atex.com");
        video3 = new Video();
        video3.setId(3l);
        video3.setShortDescription("short description 2");
        video3.setLongDescription("long description 2");
        videos = new Videos();
        
        target = spy(new Importer());
        target.setUrl(Importer.URL);
        target.setLimit(Importer.LIMIT);
        target.setMinutes(Importer.MINUTES);
        target.setUser(Importer.USER);
        target.setSecurityParent(Importer.SECURITY_PARENT);
        target.setSiteId(Importer.SITEID);
        doReturn(brightcoveService).when(target).getBrightcoveService();
        doReturn(ejbCmClient).when(target).getCmClient();
        doReturn(application).when(target).getApplication();
        doReturn(policyCMServer).when(target).getPolicyCMServer();
        videoPolicy1 = spy(new BrightcoveVideoPolicy() {
            @Override
            protected synchronized void initChildPolicies() throws CMException {
                this.childPolicies = children1;
            }
            @Override
            protected void initSelf() {}
        });
        when(children1.get(BrightcoveVideoPolicy.ID)).thenReturn(id1);
        when(children1.get(BrightcoveVideoPolicy.SHORT_DESCRIPTION)).thenReturn(singleValued);
        when(children1.get(BrightcoveVideoPolicy.LONG_DESCRIPTION)).thenReturn(singleValued);
        when(children1.get("imageType")).thenReturn(selectableSubFieldPolicy);
        when(children1.get(BrightcoveVideoPolicy.CATEGORIZATION)).thenReturn(categorizationProvider);

        when(selectableSubFieldPolicy.getChildPolicy(BrightcoveVideoPolicy.IMAGE)).thenReturn(imageManagerPolicy);
        videoPolicy1.init("PolicyName", new Content[] { content1 }, inputTemplate, parent, policyCMServer);
        when(content1.getContentId()).thenReturn(videoVersionContentId1);
        
        videoPolicy2 = spy(new BrightcoveVideoPolicy() {
            @Override
            protected synchronized void initChildPolicies() throws CMException {
                this.childPolicies = children1;
            }
            @Override
            protected void initSelf() {}
        });
        videoPolicy2.init("PolicyName", new Content[] { content2 }, inputTemplate, parent, policyCMServer);
        when(content2.getContentId()).thenReturn(videoVersionContentId2);
        when(brightcoveService.getVideos(anyLong(), anyInt())).thenReturn(videos);
        when(policyCMServer.contentExists(new ExternalContentId(Importer.PREFIX + video1.getId()))).thenReturn(true);
        when(policyCMServer.contentExists(new ExternalContentId(Importer.PREFIX + video2.getId()))).thenReturn(false);
        when(policyCMServer.getPolicy(new ExternalContentId(Importer.PREFIX + video1.getId()))).thenReturn(videoPolicy1);
        when(policyCMServer.createContent(1, new ExternalContentId(Importer.TEMPLATE))).thenReturn(videoPolicy2);
        when(policyCMServer.createContentVersion(any(VersionedContentId.class))).thenReturn(videoPolicy1);
        when(policyCMServer.getPolicy(BrightcoveConfigPolicy.CONTENT_ID)).thenReturn(configPolicy);
    }

    @Test
    public void shoulldCreateBrightcoveVideo() throws IllegalArgumentException, ConnectionPropertiesParseException, MalformedURLException, ConnectionPropertiesConfigurationException, CMException, IOException, IllegalApplicationStateException, BrightcoveException, ImageFormatException, ImageTooBigException {
        videos.add(video2);
        target.init();
        target.run();
        target.shutdown();
        verify(policyCMServer, times(1)).createContent(eq(1), any(ContentId.class));
        verify(policyCMServer, never()).createContentVersion(videoPolicy2.getContentId());
    }

    @Test
    public void shoulldUpdateBrightcoveVideo() throws IllegalArgumentException, ConnectionPropertiesParseException, MalformedURLException, ConnectionPropertiesConfigurationException, CMException, IOException, IllegalApplicationStateException, BrightcoveException, ImageFormatException, ImageTooBigException {
        videos.add(video1);
        target.init();
        target.run();
        target.shutdown();
        verify(policyCMServer, never()).createContent(eq(1), any(ContentId.class));
        verify(policyCMServer, times(1)).createContentVersion(videoPolicy2.getContentId());
    }

    @Test
    public void shouldNotImportNorUpdateVideoWhenTheStillImageAndThumnnailIsNotAvailable() throws IllegalArgumentException, ConnectionPropertiesParseException, MalformedURLException, ConnectionPropertiesConfigurationException, CMException, IOException, IllegalApplicationStateException, BrightcoveException, ImageFormatException, ImageTooBigException {
        videos.add(video3);
        target.init();
        target.run();
        target.shutdown();
        verify(policyCMServer, never()).createContent(eq(1), any(ContentId.class));
        verify(policyCMServer, never()).createContentVersion(videoPolicy1.getContentId());
        verify(policyCMServer, never()).createContentVersion(videoPolicy2.getContentId());
    }

    interface FakeSingleValued extends Policy, SingleValued {

    }

    interface FakeCategorizationProvider extends Policy, CategorizationProvider {

    }
}
