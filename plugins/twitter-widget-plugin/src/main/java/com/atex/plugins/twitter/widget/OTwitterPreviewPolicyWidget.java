/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter.widget;

import java.io.IOException;

import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.widget.OFieldPolicyWidget;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;

/**
 * 
 * Twitter Preview Policy Widget
 * 
 */
public class OTwitterPreviewPolicyWidget extends OFieldPolicyWidget implements Editor, Viewer {
    private static final long serialVersionUID = 1L;

    @Override
    public void localRender(OrchidContext oc) throws OrchidException, IOException {
        Device device = oc.getDevice();
        device.println("<div>");
        device.println("<script charset=\"utf-8\" src=\"http://widgets.twimg.com/j/2/widget.js\"></script>");
        device.println("<script>");
        device.println("new TWTR.Widget({");
        device.println("version: 2,");
        device.println("type: 'search',");
        device.println("search: 'rainbow',");
        device.println("interval: 0,");
        device.println("title: 'Rainbow',");
        device.println("subject: 'Across the sky',");
        device.println("width: '280',");
        device.println("height: '100',");
        device.println("theme: {");
        device.println("shell: {");
        device.println("background: '" + getShellBackgroundColor() + "',");
        device.println("color: '" + getShellTextColor() + "'");
        device.println("},");
        device.println("tweets: {");
        device.println("background: '" + getTweetBackgroundColor() + "',");
        device.println("color: '" + getTweetTextColor() + "',");
        device.println("links: '" + getTweetLinkColor() + "'");
        device.println("}");
        device.println("},");
        device.println("features: {");
        device.println("scrollbar: false,");
        device.println("loop: true,");
        device.println("live: true,");
        device.println("behavior: 'default'");
        device.println("}");
        device.println("}).render().start();");
        device.println("</script>");
        device.println("</div>");
    }

    /**
     * Get OColorPickerPolicyWidget
     * @param policyWidgetName
     */
    protected OColorPickerPolicyWidget getOTextInputPolicyWidget(String policyWidgetName) {
        return (OColorPickerPolicyWidget) getContentSession().findPolicyWidget(policyWidgetName);
    }

    /**
     * Get shell background color
     * @return HTML color code
     */
    public String getShellBackgroundColor() {
        return getOTextInputPolicyWidget("themesLabeledSection/themesColumnHolder/shellColumn/shellBgColor").getValue();
    }

    /**
     * Get shell text color
     * @return HTML color code
     */
    public String getShellTextColor() {
        return getOTextInputPolicyWidget("themesLabeledSection/themesColumnHolder/shellColumn2/shellFgColor").getValue();
    }

    /**
     * Get tweet background color
     * @return HTML color code
     */
    public String getTweetBackgroundColor() {
        return getOTextInputPolicyWidget("themesLabeledSection/themesColumnHolder/shellColumn/tweetBgColor").getValue();
    }

    /**
     * Get tweet text color
     * @return HTML color code
     */
    public String getTweetTextColor() {
        return getOTextInputPolicyWidget("themesLabeledSection/themesColumnHolder/shellColumn2/tweetFgColor").getValue();
    }

    /**
     * Get tweet link color
     * @return HTML color code
     */
    public String getTweetLinkColor() {
        return getOTextInputPolicyWidget("themesLabeledSection/themesColumnHolder/shellColumn/tweetLinkColor").getValue();
    }
}
