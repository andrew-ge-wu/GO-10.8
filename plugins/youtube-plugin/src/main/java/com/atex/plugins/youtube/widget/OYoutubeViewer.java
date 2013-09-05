/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.youtube.widget;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.plugins.youtube.YoutubeElementPolicy;
import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.widget.OFieldPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OTextOutput;


public class OYoutubeViewer extends OFieldPolicyWidget implements Editor, Viewer {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(OYoutubeViewer.class.getName());

    public static final String ID_NO_INPUT = "Could not display Player - as no Youtube Id is set";
    public static final String INVALID_YOUTUBE_ID = "Invalid Youtube Id";
    public static final String FILES = "plugins.com.atex.plugins.youtube-plugin.files";
    // recommended way to embedded YT video.
    public static final String PLAYER = "<iframe width=\"480\" height=\"360\" src=\"http://www.youtube.com/embed/%s\" " +
    		                            "frameborder=\"0\" allowfullscreen></iframe>";

    private OTextOutput message;

    protected String youtubeId = "";

    protected boolean showPlayer;
    protected String swfObjectJS;

    public void initSelf(OrchidContext oc) throws OrchidException {
        super.initSelf(oc);
        message = new OTextOutput();
        message.setText(ID_NO_INPUT);
        showPlayer = false;
        addAndInitChild(oc, message);
    }

    public void localRender(OrchidContext oc) throws OrchidException, IOException {
        checkDisplay();

        if (showPlayer) {
            Device device = oc.getDevice();
            device.print(String.format(PLAYER, youtubeId));

        } else {
            message.render(oc);
        }
    }

    protected void checkDisplay() {
        try {
            youtubeId = getYoutubeId();
            showPlayer = true;
        } catch (CMException e) {
            // if get policy failed.. we dont display the video
            showPlayer = false;
            message.setText(INVALID_YOUTUBE_ID);
            LOGGER.log(Level.WARNING, "Failed to get parent policy", e);
            return;
        }
    }
    
    protected String getYoutubeId() throws CMException {
        return ((YoutubeElementPolicy) getPolicy().getParentPolicy()).getYid();
    }
    
    protected void setMessage(OTextOutput message) {
        this.message = message;
    }
}
