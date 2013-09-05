package com.atex.plugins.youtube;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Enumeration;
import java.util.ResourceBundle;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.util.ServiceException;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.impl.OrchidContextImpl;
import com.polopoly.orchid.session.impl.FrameStateImpl;

public class YouTubeServiceWrapperTest {

    private YouTubeServiceWrapper target;

    @Mock
    private OrchidContextImpl oc;
    @Mock
    private FrameStateImpl frameState;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(oc.getMessageBundle()).thenReturn(new MyResourceBundle());
        when(oc.getFrameState()).thenReturn(frameState);
        target = spy(new YouTubeServiceWrapper());
    }

    @Test
    public void testSearchVideoFeedWithEmptySearchTerm() throws OrchidException {
        VideoFeed actualVideoFeed = target.search("", 1, oc);
        Assert.assertEquals(0, actualVideoFeed.getEntries().size());
    }

    @Test
    public void testSearchVideoFeedWithNullSearchTerm() throws OrchidException {
        VideoFeed actualVideoFeed = target.search(null, 1, oc);
        Assert.assertEquals(0, actualVideoFeed.getEntries().size());
    }

    @Test
    public void testSearchFunctionThrowIOException() throws OrchidException, IOException, ServiceException {
        doThrow(new IOException("throw IOException")).when(target).searchFeed(any(YouTubeService.class), anyString(), anyInt());

        target.search("1234", 0, oc);
        Assert.assertEquals(true, target.isOffline());
    }

    @Test
    public void testSearchFunctionThrowServiceException() throws OrchidException, IOException, ServiceException {
        doThrow(new ServiceException("throw ServiceException")).when(target).searchFeed(any(YouTubeService.class), anyString(),
                anyInt());

        target.search("1234", 0, oc);
        Assert.assertEquals(true, target.isOffline());
    }

    private class MyResourceBundle extends ResourceBundle {
        @Override
        protected Object handleGetObject(String key) {
            if (key.equals("p.service.youtube.unabletoconnect"))
                return "Unable to connect to Youtube website";
            else if (key.equals("p.service.youtube.unabletosearch"))
                return "Unable to search";
            else
                return null;
        }

        @Override
        public Enumeration<String> getKeys() {
            return null;
        }
    }

}
