package com.atex.plugins.youtube.widget;

import com.atex.plugins.youtube.YoutubeElementPolicy;
import com.atex.plugins.youtube.YoutubeFieldPolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.impl.OrchidContextImpl;
import com.polopoly.orchid.session.impl.FrameStateImpl;
import com.polopoly.orchid.widget.OTextOutput;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class OYoutubeViewerTest {
    @Mock
    private OrchidContextImpl oc;
    @Mock
    private Device device;

    private OYoutubeViewer target;
    private YoutubeFieldPolicy fieldPolicy;

    private static final String youtubeId = "xyxYff11";

    @Mock
    FrameStateImpl frameState;

    @Mock
    private Content fieldContent;
    @Mock
    InputTemplate fieldInputTemplate;
    @Mock
    PolicyCMServer cmServer;

    @Mock
    private YoutubeElementPolicy parentPolicy;

    @Before
    public void setup() throws OrchidException {
        MockitoAnnotations.initMocks(this);
        when(oc.getDevice()).thenReturn(device);
        when(oc.getFrameState()).thenReturn(frameState);
        when(parentPolicy.getYid()).thenReturn(youtubeId);
        when(parentPolicy.getPlayTime()).thenReturn(5);

        fieldPolicy = new YoutubeFieldPolicy() {
            @Override
            protected void initSelf() {
            
            }
            
        };
        fieldPolicy.init("PolicyName", new Content[] { fieldContent }, fieldInputTemplate, parentPolicy, cmServer);

        target = spy(new OYoutubeViewer());
        doReturn(fieldPolicy).when(target).getPolicy();
        target.initSelf(oc);
    }

    @Test
    public void testCheckDisplayShouldDisplayIfGetPolicySuccess() throws IOException {
        target.checkDisplay();
        assertEquals(target.youtubeId, youtubeId);
        assertEquals(target.showPlayer, true);
    }

    @Test
    public void testLocalRender() throws OrchidException, IOException {
        target.localRender(oc);

        assertEquals(target.showPlayer, true);
    }
    
    @Test
    public void shouldNotShowPlayerWhenFailedToGetYoutubeId() throws OrchidException, IOException, CMException {
        doThrow(new CMException("Dummy Message for Unit Test")).when(target).getYoutubeId();
        OTextOutput message = mock(OTextOutput.class);
        target.setMessage(message);
        target.localRender(oc);
        assertEquals("", target.youtubeId);
        assertEquals(target.showPlayer, false);
        verify(message).render(oc);
    }
    

}
