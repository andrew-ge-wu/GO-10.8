/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.brightcove.commons.catalog.objects.Video;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.app.imagemanager.ContentImage;
import com.polopoly.cm.app.imagemanager.ImageManagerPolicy;
import com.polopoly.cm.app.imagemanager.ImageSetPolicy;
import com.polopoly.cm.app.policy.SelectableSubFieldPolicy;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.app.search.categorization.Categorization;
import com.polopoly.cm.app.search.categorization.CategorizationBuilder;
import com.polopoly.cm.app.search.categorization.CategorizationProvider;
import com.polopoly.cm.app.search.categorization.Dimension;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.servlet.URLBuilder;
import com.polopoly.orchid.OrchidException;

public class BrightcoveVideoPolicyTest {
    public static final String IMAGE_TYPE = "imageType";

    @Mock
    PolicyCMServer cmServer;
    @Mock
    Content content;
    @Mock
    InputTemplate inputTemplate;
    @Mock
    Policy parent;
    @Mock
    HashMap<String, Policy> children;
    @Mock
    SingleValuePolicy id;
    @Mock
    SingleValuePolicy shortDescription;
    @Mock
    SingleValuePolicy longDescription;
    @Mock
    SelectableSubFieldPolicy imageType;
    @Mock
    ImageManagerPolicy imageManagerPolicy;
    @Mock
    ImageSetPolicy imageSetPolicy;
    @Mock
    HttpServletRequest request;
    @Mock
    ContentImage image;
    @Mock
    URLBuilder urlBuilder;
    @Mock
    ContentList relatedElements;
    @Mock
    ListIterator<ContentReference> iterator;
    @Mock
    ContentReference relatedCf;
    @Mock
    ContentId relatedId;
    @Mock
    CategorizationProvider categorizationProvider;

    BrightcoveVideoPolicy target;
    Categorization categorization;
    Map<String, Dimension> map;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        target = spy(new BrightcoveVideoPolicy() {
            @Override
            protected synchronized void initChildPolicies() throws CMException {
                this.childPolicies = children;
            }

            // does nothing to avoid NullPointerExeception when trace up parant
            // ids
            @Override
            protected void initSelf() {
            }

            @Override
            String getUrl(URLBuilder urlBuilder, String path, HttpServletRequest httpServletRequest) throws CMException {
                return path;
            }
            
            
        });
        target.init("policy", new Content[] { content }, inputTemplate, parent, cmServer);

        when(children.get(BrightcoveVideoPolicy.ID)).thenReturn(id);
        when(children.get(BrightcoveVideoPolicy.SHORT_DESCRIPTION)).thenReturn(shortDescription);
        when(children.get(BrightcoveVideoPolicy.LONG_DESCRIPTION)).thenReturn(longDescription);
        when(children.get(IMAGE_TYPE)).thenReturn(imageType);
        when(imageManagerPolicy.getSelectedImage()).thenReturn(imageSetPolicy);
        when(imageSetPolicy.getImage()).thenReturn(image);
        doReturn(categorizationProvider).when(target).getCategorizationProvider();
    }

    @After
    public void tearDown() throws Exception {
        target = null;
    }

    @Test
    public void testGetId() throws CMException {
        String expected = "1234577811";
        when(id.getValue()).thenReturn(expected);
        assertNotNull(target.getId());
        assertEquals(expected, target.getId());
    }

    @Test
    public void testSetId() throws CMException {
        String expected = "8766294182";
        target.setId(expected);
        verify(id).setValue(expected);
    }

    @Test
    public void testGetShortDescription() throws CMException {
        String expected = "this is very very short description";
        when(shortDescription.getValue()).thenReturn(expected);
        assertNotNull(target.getShortDescription());
        assertEquals(expected, target.getShortDescription());
    }

    @Test
    public void testSetShortDescription() throws CMException {
        String expected = "set very short descrption";
        target.setShortDescription(expected);
        verify(shortDescription).setValue(expected);
    }

    @Test
    public void testGetLongDescription() throws CMException {
        String expected = "this is very very long description";
        when(longDescription.getValue()).thenReturn(expected);
        assertNotNull(target.getLongDescription());
        assertEquals(expected, target.getLongDescription());
    }

    @Test
    public void testSetLongDescription() throws CMException {
        String expected = "set very long descrption";
        target.setLongDescription(expected);
        verify(longDescription).setValue(expected);
    }

    @Test(expected = CMException.class)
    public void testSetChildValueIfNotSingleValued() throws CMException {
        target.setChildValue(IMAGE_TYPE, "testing image type");
    }
    
    @Test(expected = CMException.class)
    public void testSetChildValueIfNotExistChild() throws CMException {
        target.setChildValue("notExist", "nothing");
    }

    @Test
    public void shouldReturnPolopolyImageUrl() throws CMException, OrchidException, IOException {
        when(imageType.getChildPolicy(BrightcoveVideoPolicy.IMAGE)).thenReturn(imageManagerPolicy);
        when(imageType.getSelectedSubFieldName()).thenReturn(BrightcoveVideoPolicy.IMAGE);
        when(image.getPath()).thenReturn("/logo.png");
        assertEquals("/logo.png", target.getUrl(request));
    }

    @Test
    public void shouldReturnNullImageUrl() throws CMException, MalformedURLException {
        when(imageType.getChildPolicy(BrightcoveVideoPolicy.IMAGE)).thenReturn(imageManagerPolicy);
        assertNull(target.getUrl(request));
    }
    
    @Test
    public void shouldReturnNullImageUrlException() throws CMException, OrchidException, IOException {
        doThrow(new CMException("Unit Test Exception")).when(target).getSubFieldPolicy();
        assertNull(target.getUrl(request));
    }
    
    @Test
    public void shouldReturnPublishingDateTime() {
        doReturn(1000L).when(target).getContentCreationTime();
        assertEquals(1000L, target.getPublishingDateTime());
    }
    
    @Test
    public void shouldReturnVideoBean() {
        assertEquals(target, target.getVideoBean());
    }

    @Test
    public void shoudGetImagePathNull() throws CMException, OrchidException, IOException {
        when(imageType.getChildPolicy(BrightcoveVideoPolicy.IMAGE)).thenReturn(imageManagerPolicy);
        when(imageType.getSelectedSubFieldName()).thenReturn(BrightcoveVideoPolicy.IMAGE);
        assertNull(target.getImagePath());
        when(imageManagerPolicy.getSelectedImage()).thenReturn(null);
        assertNull(target.getImagePath());
    }

    @Test
    public void shouldGetRelatedElementId() throws CMException {
        doReturn(relatedElements).when(target).getContentList(BrightcoveVideoPolicy.SLOT_ELEMENTS);
        when(relatedElements.getListIterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true);
        when(iterator.next()).thenReturn(relatedCf);
        when(relatedCf.getReferredContentId()).thenReturn(relatedId);
        assertEquals(relatedId, target.getRelatedElementId());
    }

    @Test
    public void shouldNullGetRelatedElementId() throws CMException {
        doThrow(new CMException("Unit Test Exception")).when(target).getContentList(BrightcoveVideoPolicy.SLOT_ELEMENTS);
        assertNull(target.getRelatedElementId());
        doReturn(relatedElements).when(target).getContentList(BrightcoveVideoPolicy.SLOT_ELEMENTS);
        when(relatedElements.getListIterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(false);
        assertNull(target.getRelatedElementId());
    }
    
    // toVideo method test cases
    @Test
    public void nullMappingShouldNotCauseNPE() throws CMException {
        categorization = new CategorizationBuilder().build();
        when(categorizationProvider.getCategorization()).thenReturn(categorization);
        when(id.getValue()).thenReturn("1");
        doReturn("name").when(target).getName();
        when(shortDescription.getValue()).thenReturn("short description");
        when(longDescription.getValue()).thenReturn("long desciption");
        Video video = target.toVideo(null);
        assertEquals("name", video.getName());
        assertEquals("short description", video.getShortDescription());
        assertEquals("long desciption", video.getLongDescription());
    }
}
