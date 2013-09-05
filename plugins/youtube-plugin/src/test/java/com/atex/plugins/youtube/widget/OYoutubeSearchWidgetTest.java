/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */
package com.atex.plugins.youtube.widget;

import com.atex.plugins.youtube.YouTubeServiceWrapper;
import com.atex.plugins.youtube.YoutubeElementPolicy;
import com.google.gdata.data.TextConstruct;
import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.polopoly.cm.app.ContentSession;
import com.polopoly.cm.app.PolicyWidget;
import com.polopoly.cm.app.widget.OTextInputPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.impl.OrchidContextImpl;
import com.polopoly.orchid.event.OrchidEvent;
import com.polopoly.orchid.event.WidgetEventListener;
import com.polopoly.orchid.session.impl.FrameStateImpl;
import com.polopoly.orchid.widget.*;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import static junit.framework.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class OYoutubeSearchWidgetTest {

    private OYoutubeSearchWidget target;

    /*
     * java.lang.ClassCastException: com.polopoly.orchid.context.
     * OrchidContext$$EnhancerByMockitoWithCGLIB$$4f015875 cannot be cast to
     * com.polopoly.orchid.context.impl.OrchidContextImpl at
     * com.polopoly.orchid.
     * widget.OExtendedWidgetBase.init(OExtendedWidgetBase.java:103)
     * 
     * We cant mock OrchidContext interface due to the above issue(bug?)
     */
    @Mock
    private OrchidContextImpl oc;
    @Mock
    private Device device;
    @Mock
    private OrchidEvent e;
    @Mock
    private FrameStateImpl frameState;

    private OTextInput searchInput;
    private OSubmitButton searchButton;
    private OSelect orderBySelect;

    // PreviewEventListener
    private static final String imageTitle = "this is image title";
    @Mock
    OrchidEvent previewEvent;
    @Mock
    TextConstruct videoTitle;
    @Mock
    private ContentSession contentSession;
    @Mock
    private PolicyWidget topWidget;
    @Mock
    private YoutubeElementPolicy policy;
    @Mock
    private OTextInputPolicyWidget youtubeNamePolicyWidget;
    @Mock
    private OWidget youtubeNameWidget;

    @Before
    public void setup() throws OrchidException, IOException {
        MockitoAnnotations.initMocks(this);
        when(oc.getDevice()).thenReturn(device);
        when(oc.getMessageBundle()).thenReturn(new MyResourceBundle());
        when(oc.getFrameState()).thenReturn(frameState);
        target = spy(new OYoutubeSearchWidget() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void initSearchInput(final ResourceBundle rb) throws OrchidException {
                super.initSearchInput(rb);
                searchInput = getSearchInput();
            }

            @Override
            protected void initSelect(final ResourceBundle rb) throws OrchidException {
                super.initSelect(rb);
                orderBySelect = getOrderBySelect();
            }

            @Override
            public String getCompoundId() throws OrchidException {
                return "work_123";
            }
        });
        target.initSelf(oc);
        target.localRender(oc);
        searchButton = target.getSearchButton();
    }

    @Test
    public void testSearchWithEmptyKeyword() throws OrchidException {
        doSearchResult("", YouTubeServiceWrapper.RELEVANCE, null);
    }

    @Test
    public void testSearchWithNullKeyword() throws OrchidException {
        doSearchResult(null, YouTubeServiceWrapper.RELEVANCE, null);
    }

    @Test
    public void testSearchShouldReturnRelevanceResult() throws OrchidException {
        doSearchResult("adele", YouTubeServiceWrapper.RELEVANCE, "adele");
    }

    @Test
    public void testSearchShouldReturnViewCountResult() throws OrchidException {
        doSearchResult("adele", YouTubeServiceWrapper.VIEW_COUNT, "rolling in the deep");
    }

    @Test
    public void testSearchShouldReturnUpdateResult() throws OrchidException {
        doSearchResult("adele", YouTubeServiceWrapper.UPDATED, null);
    }

    @Test
    public void testSearchShouldReturnRatingResult() throws OrchidException {
        doSearchResult("adele", YouTubeServiceWrapper.RATING, "Someone Like You");
    }

    @Test
    public void testSearchShouldReturnRelevanceResultAndRenderResult() throws OrchidException, IOException {
        doReturn(createDumpImage()).when(target).createImage(eq(oc), any(VideoEntry.class));
        doReturn(new OSubmitButton()).when(target).createButton(eq(oc), any(VideoEntry.class), any(String.class));

        doSearchResult("adele", YouTubeServiceWrapper.RELEVANCE, "adele");

        int size = target.getSearchResults().getEntries().size();

        target.renderSearchResult(oc);
        verify(device, times(size)).print("<h2>" + imageTitle + "</h2>");

        if (size > 0)
            verify(device).print("</table></div></div>");
    }

    @Test
    public void testRenderSearchResultWithNullResult() throws OrchidException, IOException {
        target.renderSearchResult(oc);
        verify(device, never()).print("</table></div></div>");
    }

    @Test
    public void testRenderSearchResultWithEmptyResult() throws OrchidException, IOException {
        doSearchResult("jasfdk afkjsflsaj23if j1j34j2l'dad f", YouTubeServiceWrapper.RELEVANCE, null);

        target.renderSearchResult(oc);
        verify(device, never()).print("</table></div></div>");
    }

    @Test
    public void testeCreateImage() throws OrchidException, IOException {
        final String youtubeName = "this is youtube name";
        final String yId = " video:12345";
        final String imageUrl = "http://www.atex.com/atex.png";

        VideoEntry entry = createVideoEntry(youtubeName, yId, imageUrl);

        OImage targetImage = spy(new OImage());

        doNothing().when(targetImage).render(oc);
        doReturn(targetImage).when(target).newImage();

        target.createImage(oc, entry);

        Assert.assertEquals(youtubeName, targetImage.getAltText());
        Assert.assertEquals(youtubeName, targetImage.getTitle());
        Assert.assertEquals("12345", targetImage.getExtraAttribute(OYoutubeSearchWidget.YID_ATTRIBUTE));
    }

    @Test
    public void testeCreateImageWithNullTitle() throws OrchidException, IOException {
        final String youtubeName = null;
        final String yId = " video:12345";
        final String imageUrl = "http://www.atex.com/atex.png";

        VideoEntry entry = createVideoEntry(youtubeName, yId, imageUrl);

        OImage targetImage = spy(new OImage());

        doNothing().when(targetImage).render(oc);
        doReturn(targetImage).when(target).newImage();

        target.createImage(oc, entry);

        Assert.assertEquals("", targetImage.getAltText());
        Assert.assertEquals("", targetImage.getTitle());
        Assert.assertEquals("12345", targetImage.getExtraAttribute(OYoutubeSearchWidget.YID_ATTRIBUTE));
    }

    @Test
    public void testPreviewEventListenerToEnsureYidAndNameAreSet() throws OrchidException, IOException, CMException {
        final String youtubeName = "this is youtube name";
        final String yId = " video:12345";

        VideoEntry entry = createVideoEntry(youtubeName, yId, "empty url");

        OSubmitButton targetButton = spy(new OSubmitButton());
        doNothing().when(targetButton).render(oc);

        doReturn(targetButton).when(target).newButton();
        doReturn(contentSession).when(target).getContentSession();

        when(previewEvent.getWidget()).thenReturn(targetButton);

        when(youtubeNameWidget.getCompoundId()).thenReturn("work_124");
        when(youtubeNamePolicyWidget.getChildWidget()).thenReturn(youtubeNameWidget);

        when(contentSession.getTopPolicy()).thenReturn(policy);
        when(contentSession.getTopWidget()).thenReturn(topWidget);
        when(contentSession.findPolicyWidget(OYoutubeSearchWidget.YOUTUBE_ELEMENT_COLUM_NAME))
                .thenReturn(youtubeNamePolicyWidget);
        doNothing().when(topWidget).store();

        target.createButton(oc, entry, "Select");
        @SuppressWarnings("unchecked")
        List<WidgetEventListener> listeners = targetButton.getEventListeners(WidgetEventListener.class);
        for (WidgetEventListener listener : listeners) {
            listener.processEvent(oc, previewEvent);
        }

        verify(policy).setName(youtubeName);
        verify(policy).setYid("12345");
    }

    @Test
    public void testCreateButtonWithNullTitle() throws OrchidException, IOException {
        final String youtubeName = null;
        final String yId = " video:12345";

        VideoEntry entry = createVideoEntry(youtubeName, yId, "empty url");

        OSubmitButton targetButton = spy(new OSubmitButton());
        doNothing().when(targetButton).render(oc);

        doReturn(targetButton).when(target).newButton();
        doReturn(contentSession).when(target).getContentSession();

        target.createButton(oc, entry, "Select");
        Assert.assertEquals("", targetButton.getTitle());
    }

    @Test
    public void testGetVideoId() {
        Assert.assertEquals("", target.getVideoId(null));
        Assert.assertEquals("12345", target.getVideoId(" video:12345"));
        Assert.assertEquals("", target.getVideoId("123456"));
    }

    @Test
    public void testCleanImage() {
        target.images = null;
        target.previewButtons = null;
        target.clearSearchResults();

        Assert.assertNotNull(target.images);
        Assert.assertNotNull(target.previewButtons);
        Assert.assertEquals(0, target.images.size());
        Assert.assertEquals(0, target.previewButtons.size());
    }
    
    private Object createDumpImage() {
        OImage image = new OImage();
        image.setTitle(imageTitle);
        return image;
    }

    private VideoEntry createVideoEntry(final String youtubeName, final String yId, final String imageUrl) {
        MediaThumbnail thumbnail = new MediaThumbnail();
        thumbnail.setUrl(imageUrl);

        VideoEntry entry = new VideoEntry();
        entry.setId(yId);
        when(videoTitle.getPlainText()).thenReturn(youtubeName);
        entry.setTitle(videoTitle);
        entry.getOrCreateMediaGroup().addThumbnail(thumbnail);
        return entry;
    }

    private VideoFeed doSearchResult(String keyword, int searchType, String expectedResult) throws OrchidException {
        searchInput.setText(keyword);

        @SuppressWarnings("unchecked")
        List<WidgetEventListener> listeners = searchButton.getEventListeners(WidgetEventListener.class);
        orderBySelect.setSelectedValue(String.valueOf(searchType));
        for (WidgetEventListener listener : listeners) {
            listener.processEvent(oc, e);
        }

        VideoFeed searchResults = target.getSearchResults();
        if (keyword == null || keyword.length() == 0) {
            assertNull("Search result should null", searchResults);
        } else {
            assertNotNull("Search result must not null", searchResults);
        }

        // skip if youtube offline
        if (target.isYoutubeOffline())
            return null;

        if (expectedResult != null && expectedResult.length() > 0) {
            expectedResult = expectedResult.toLowerCase();

            boolean found = false;
            for (int index = 0; index < searchResults.getEntries().size() && index < OYoutubeSearchWidget.LIMIT && !found; index++) {
                VideoEntry entry = searchResults.getEntries().get(index);
                String title = entry.getTitle().getPlainText().toLowerCase();
                found = title.contains(expectedResult);
            }
            assertTrue(found);
        }

        return searchResults;
    }

    private class MyResourceBundle extends ResourceBundle {
        @Override
        protected Object handleGetObject(String key) {
            if (key.equals("p.service.youtube.unabletoconnect"))
                return "Unable to connect to Youtube website";
            else
                return null;
        }

        @Override
        public Enumeration<String> getKeys() {
            return null;
        }
    }
}
