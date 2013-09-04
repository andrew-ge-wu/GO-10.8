/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove.widget;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.plugins.brightcove.BrightcoveVideoPolicy;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.orchid.widget.OContentIcon;
import com.polopoly.cm.app.search.widget.OSearchThumbBase;
import com.polopoly.cm.app.util.URLBuilder;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.js.HasCssDependencies;

public class OSearchThumbBrightcoveVideo extends OSearchThumbBase implements HasCssDependencies {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(OSearchThumbBrightcoveVideo.CLASS);
    private static final String FILES = "plugins.com.atex.plugins.brightcove-plugin.files";
    private static final String CSS = "custom.css";

    private BrightcoveVideoPolicy videoPolicy;
    private String cssPath;
    private String name;
    private String thumbnail;
    private OContentIcon icon;

    public String[] getCssDependencies() {
        return new String[] { cssPath };
    }

    @Override
    protected String getCSSClass() {
        return "customSearchBrightcoveVideo";
    }

    @Override
    protected int getWidth() {
        return 250;
    }

    @Override
    public void initSelf(OrchidContext oc) throws OrchidException {
        callSuperInitSelf(oc);

        cssPath = lookupContentFile(FILES, CSS, oc);

        try {
            ContentPolicy policy = getContentPolicy();
            icon = getOContentIcon();
            icon.initContent((Policy) policy, getContentSession().getPolicyCMServer());
            addAndInitChild(oc, icon);

            videoPolicy = getBrightcoveVideoPolicy();

            name = videoPolicy.getName();
            name = (name == null || "".equals(name)) ? videoPolicy.getContentId().getContentId().getContentIdString()
                    : abbreviate(name, 30);

            thumbnail = videoPolicy.getImagePath();

        } catch (CMException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
    }

    @Override
    protected void renderThumb(OrchidContext oc) throws IOException, OrchidException {
        Device device = oc.getDevice();
        renderContentTitle(device, oc);
        renderContentBody(device, oc);
    }

    protected void renderContentTitle(Device device, OrchidContext oc) throws OrchidException, IOException {
        device.print("<div style=\"float:left; margin-right: 2px;\">");
        icon.render(oc);
        device.print("</div>");
        device.print("<h2>" + name + "</h2>");
    }

    protected void renderContentBody(Device device, OrchidContext oc) throws OrchidException, IOException {
        if (thumbnail != null) {
            device.println("<img src='" + thumbnail + "' class='brightcoveVideoImage'/>");
        }
        device.println("<span class='lead'>"+abbreviate(videoPolicy.getShortDescription(), 200)+"</span>");
    }

    protected String abbreviate(String str, int maxWidth) {
        return (str.length() <= maxWidth) ? str : str.substring(0, maxWidth - 3) + "...";
    }


    protected void callSuperInitSelf(OrchidContext oc) throws OrchidException {
        super.initSelf(oc);
    }

    protected OContentIcon getOContentIcon() {
        return new OContentIcon();
    }

    protected ContentPolicy getContentPolicy() {
        return (ContentPolicy) getPolicy();
    }

    protected BrightcoveVideoPolicy getBrightcoveVideoPolicy() {
        return (BrightcoveVideoPolicy) getPolicy();
    }

    protected String lookupContentFile(String externalId, String fileName, OrchidContext oc) {
        try {
            return URLBuilder.getFileUrl(new ExternalContentId(externalId), fileName, oc);
        } catch (OrchidException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
        return null;
    }
}
