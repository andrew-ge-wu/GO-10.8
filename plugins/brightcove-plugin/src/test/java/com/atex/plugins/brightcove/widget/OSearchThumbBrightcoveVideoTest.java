/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove.widget;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

import com.atex.plugins.brightcove.BrightcoveVideoPolicy;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.ContentSession;
import com.polopoly.cm.app.orchid.widget.OContentIcon;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.impl.OrchidContextImpl;

public class OSearchThumbBrightcoveVideoTest {

    private OSearchThumbBrightcoveVideo target;
    private static final String FILES = "plugins.com.atex.plugins.brightcove-plugin.files";
    private static final String CSS = "custom.css";

    @Mock
    OrchidContextImpl oc;
    @Mock
    Device device;
    @Mock
    Policy policy; 
    @Mock
    ContentPolicy contentPolicy;
    @Mock
    ContentSession contentSession;
    @Mock
    OContentIcon oContentIcon;
    @Mock
    BrightcoveVideoPolicy brightcoveVideoPolicy; 
    @Mock
    ContentId contentId;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        target = spy (new OSearchThumbBrightcoveVideo());

        doNothing().when(target).callSuperInitSelf(oc);
        doReturn(null).when(target).lookupContentFile(FILES, CSS, oc);
        doReturn(policy).when(target).getPolicy();
        doReturn(contentSession).when(target).getContentSession();
        doReturn(device).when(oc).getDevice();
        doReturn(contentPolicy).when(target).getContentPolicy();
        doReturn(oContentIcon).when(target).getOContentIcon();
        doReturn(brightcoveVideoPolicy).when(target).getBrightcoveVideoPolicy();
    }

    private void initSelf() throws CMException, OrchidException {
        doReturn("name").when(brightcoveVideoPolicy).getName();
        doReturn("").when(brightcoveVideoPolicy).getImagePath();
        target.initSelf(oc);
    }

    private void initSelfBranch() throws CMException, OrchidException {
        doReturn("name").when(brightcoveVideoPolicy).getName();
        doReturn(null).when(brightcoveVideoPolicy).getImagePath();
        target.initSelf(oc);
    }

    @After
    public void tearDown() throws Exception {
        target = null;
    }

    @Test
    public void testGetWidth() {
        int expected = 250;
        int actual = target.getWidth();
        assertEquals(expected, actual);
    }

    @Test
    public void testGetCssDependencies() {
        String expected = null;
        String[] actual = target.getCssDependencies();
        assertEquals(expected, actual[0]);
    }

    @Test
    public void testGetCSSClass() {
        String expected = "customSearchBrightcoveVideo";
        String actual = target.getCSSClass();
        assertEquals(expected, actual);
    }

    @Test
    public void testRenderThumb() throws OrchidException, IOException {
        doNothing().when(target).renderContentTitle(device, oc);
        doNothing().when(target).renderContentBody(device, oc);

        target.renderThumb(oc);
        verify(target).renderContentTitle(device, oc);
        verify(target).renderContentBody(device, oc);
    }

    @Test
    public void testRenderContentTitle() throws OrchidException, IOException, CMException {
        initSelf();
        doNothing().when(oContentIcon).render(oc);
        target.renderContentTitle(device, oc);

        verify(device).print("<div style=\"float:left; margin-right: 2px;\">");
        verify(oContentIcon).render(oc);
        verify(device).print("</div>");
        verify(device).print("<h2>name</h2>");
    }

    @Test
    public void testRenderContentBody() throws OrchidException, IOException, CMException {
        String testString = "This is String!";

        initSelf();
        doReturn(testString).when(brightcoveVideoPolicy).getShortDescription();
        doReturn(testString).when(target).abbreviate(testString, 200);

        target.renderContentBody(device, oc);
        verify(device).println("<img src='' class='brightcoveVideoImage'/>");
        verify(device).println("<span class='lead'>"+testString+"</span>");
    }

    @Test
    public void testRenderContentBodyBranch() throws OrchidException, IOException, CMException {
        String testString = "This is String!";

        initSelfBranch();
        doReturn(testString).when(brightcoveVideoPolicy).getShortDescription();
        doReturn(testString).when(target).abbreviate(testString, 200);

        target.renderContentBody(device, oc);
        verify(device, never()).println("<img src='' class='brightcoveVideoImage'/>");
        verify(device).println("<span class='lead'>" + testString + "</span>");
    }

    @Test
    public void testAbbreviate() {
        int max = 50;
        String expected = "This is a String!";
        String actual = target.abbreviate(expected, max);
        assertEquals(expected, actual);
    }

    @Test
    public void testAbbreviateBranch() {
        int max = 16;
        String str = "This is a String!";
        String expected = "This is a Str...";
        String actual = target.abbreviate(str, max);
        assertEquals(expected, actual);
    }
}
