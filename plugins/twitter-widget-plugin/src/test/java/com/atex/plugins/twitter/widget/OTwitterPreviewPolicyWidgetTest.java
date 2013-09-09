/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter.widget;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.polopoly.cm.app.ContentSession;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;

public class OTwitterPreviewPolicyWidgetTest {

    private OTwitterPreviewPolicyWidget target;

    @Mock
    private ContentSession contentSession;
    @Mock
    private OrchidContext orchidContext;
    @Mock
    private Device device;
    @Mock
    private OColorPickerPolicyWidget oColorPickerPolicyWidget;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        target = spy(new OTwitterPreviewPolicyWidget());
        doReturn(contentSession).when(target).getContentSession();
    }

    @Test
    public void testGetOTextInputPolicyWidget() {
        when(contentSession.findPolicyWidget("_TDATA")).thenReturn(oColorPickerPolicyWidget);
        assertEquals(oColorPickerPolicyWidget, target.getOTextInputPolicyWidget("_TDATA"));
    }

    @Test
    public void testGetShellBackgroundColor() {
        doReturn(oColorPickerPolicyWidget).when(target)
                .getOTextInputPolicyWidget("themesLabeledSection/themesColumnHolder/shellColumn/shellBgColor");
        when(oColorPickerPolicyWidget.getValue()).thenReturn("#8EC1DA");
        assertEquals("#8EC1DA", target.getShellBackgroundColor());
    }

    @Test
    public void testGetShellTextColor() {
        doReturn(oColorPickerPolicyWidget).when(target)
                .getOTextInputPolicyWidget("themesLabeledSection/themesColumnHolder/shellColumn2/shellFgColor");
        when(oColorPickerPolicyWidget.getValue()).thenReturn("#FFFFFF");
        assertEquals("#FFFFFF", target.getShellTextColor());
    }

    @Test
    public void testGetTweetBackgroundColor() {
        doReturn(oColorPickerPolicyWidget).when(target)
                .getOTextInputPolicyWidget("themesLabeledSection/themesColumnHolder/shellColumn/tweetBgColor");
        when(oColorPickerPolicyWidget.getValue()).thenReturn("#ffffff");
        assertEquals("#ffffff", target.getTweetBackgroundColor());
    }

    @Test
    public void testGetTweetTextColor() {
        doReturn(oColorPickerPolicyWidget).when(target)
                .getOTextInputPolicyWidget("themesLabeledSection/themesColumnHolder/shellColumn2/tweetFgColor");
        when(oColorPickerPolicyWidget.getValue()).thenReturn("#444444");
        assertEquals("#444444", target.getTweetTextColor());
    }

    @Test
    public void testGetTweetLinkColor() {
        doReturn(oColorPickerPolicyWidget).when(target)
                .getOTextInputPolicyWidget("themesLabeledSection/themesColumnHolder/shellColumn/tweetLinkColor");
        when(oColorPickerPolicyWidget.getValue()).thenReturn("#1985B5");
        assertEquals("#1985B5", target.getTweetLinkColor());
    }

    @Test
    public void testLocalRender() throws Exception {
        when(orchidContext.getDevice()).thenReturn(device);
        doReturn("#ffffff").when(target).getShellBackgroundColor();
        doReturn("#ffff00").when(target).getShellTextColor();
        doReturn("#ff0000").when(target).getTweetBackgroundColor();
        doReturn("#fff000").when(target).getTweetTextColor();
        doReturn("#fffff0").when(target).getTweetLinkColor();

        target.localRender(orchidContext);
        verify(device).println("<div>");
        verify(device).println("<script charset=\"utf-8\" src=\"http://widgets.twimg.com/j/2/widget.js\"></script>");
        verify(device).println("<script>");
        verify(device).println("new TWTR.Widget({");
        verify(device).println("version: 2,");
        verify(device).println("type: 'search',");
        verify(device).println("search: 'rainbow',");
        verify(device).println("interval: 0,");
        verify(device).println("title: 'Rainbow',");
        verify(device).println("subject: 'Across the sky',");
        verify(device).println("width: '280',");
        verify(device).println("height: '100',");
        verify(device).println("theme: {");
        verify(device).println("shell: {");
        verify(device).println("background: '" + "#ffffff" + "',");
        verify(device).println("color: '" + "#ffff00" + "'");
        verify(device, times(2)).println("},");
        verify(device).println("tweets: {");
        verify(device).println("background: '" + "#ff0000" + "',");
        verify(device).println("color: '" + "#fff000" + "',");
        verify(device).println("links: '" + "#fffff0" + "'");
        verify(device, times(2)).println("}");
        verify(device, times(2)).println("},");
        verify(device).println("features: {");
        verify(device).println("scrollbar: false,");
        verify(device).println("loop: true,");
        verify(device).println("live: true,");
        verify(device).println("behavior: 'default'");
        verify(device, times(2)).println("}");
        verify(device).println("}).render().start();");
        verify(device).println("</script>");
        verify(device).println("</div>");
    }
}
