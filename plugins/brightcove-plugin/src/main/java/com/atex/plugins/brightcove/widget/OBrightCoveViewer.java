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

import com.atex.plugins.brightcove.BrightCoveElementPolicy;
import com.atex.plugins.brightcove.BrightcoveConfigPolicy;
import com.atex.plugins.brightcove.util.ConfigurationUtil;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.widget.OFieldPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OTextOutput;
import com.polopoly.util.LocaleUtil;
import com.polopoly.util.StringUtil;

public class OBrightCoveViewer extends OFieldPolicyWidget implements Editor, Viewer {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(OBrightCoveViewer.class.getName());

    public static final String INVALID_BRIGHTCOVE_IDS = "com.atex.plugins.brightcove.video.failedid";
    public static final String ERR_BRIGHTCOVE_VID = "com.atex.plugins.brightcove.video.failedvideoid";
    public static final String ERR_BRIGHTCOVE_PID = "com.atex.plugins.brightcove.video.failedplayerid";

    public static final String PLAYER = "<script language=\"JavaScript\" type=\"text/javascript\" src=\"http://admin.brightcove.com/js/BrightcoveExperiences.js\"></script>" +
            "<object id=\"myExperience%s\" class=\"BrightcoveExperience\">" +
            "<param name=\"bgcolor\" value=\"#ffffff\" />" +
            "<param name=\"width\" value=\"480\" />" +
            "<param name=\"height\" value=\"360\" />" +
            "<param name=\"isVid\" value=\"true\" />" +
            "<param name=\"isUI\" value=\"true\" />" +
            "<param name=\"dynamicStreaming\" value=\"true\" />" +
            "<param name=\"playerID\" value=\"%s\" />" +
            "<param name=\"@videoPlayer\" value=\"%s\" />" +
            "</object>" +
            "<script type=\"text/javascript\">brightcove.createExperiences();</script>";

    private OTextOutput message;

    protected String bcVideoId = "";

    protected String bcPlayerId = "";

    protected boolean showNothing;

    public void initSelf(OrchidContext oc) throws OrchidException {
        super.initSelf(oc);
        message = new OTextOutput();
        message.setText(LocaleUtil.format(INVALID_BRIGHTCOVE_IDS, oc.getMessageBundle()));
        showNothing = true;
        addAndInitChild(oc, message);
    }

    public void localRender(OrchidContext oc) throws OrchidException, IOException {
        checkBeforeDisplay(oc);
        if (!showNothing) {
            String eleId = "";
            try {
                eleId = getElementId();
            } catch (CMException e) {
                LOGGER.log(Level.INFO, "Fail to get element id");
            }
            Device device = oc.getDevice();
            device.print(String.format(PLAYER, eleId, bcPlayerId, bcVideoId));
        } else {
            message.render(oc);
        }
    }

    protected boolean checkVideoId(OrchidContext oc) {
        try {
            bcVideoId = getVideoId();
            return true;
        } catch (CMException e) {
            message.setText(LocaleUtil.format(ERR_BRIGHTCOVE_VID, oc.getMessageBundle()));
            return false;
        }
    }

    protected boolean checkPlayerId(OrchidContext oc) {
        try {
            bcPlayerId = getPlayerId();
            return true;
        } catch (CMException e) {
            message.setText(LocaleUtil.format(ERR_BRIGHTCOVE_PID, oc.getMessageBundle()));
            return false;
        }
    }

    protected void checkBeforeDisplay(OrchidContext oc) {
        boolean video = checkVideoId(oc);
        boolean player = checkPlayerId(oc);
        showNothing  = !(video || player);
    }

    protected BrightCoveElementPolicy getElementParent() throws CMException {
        return ((BrightCoveElementPolicy) getPolicy().getParentPolicy());
    }

    protected String getVideoId() throws CMException {
        return getElementParent().getVideoId();
    }

    protected String getPlayerId() throws CMException {
        String playerId = getElementParent().getPlayerId();
        if (!StringUtil.isEmpty(playerId)) {
            return playerId;
        }
        BrightcoveConfigPolicy configPolicy = (BrightcoveConfigPolicy) getCmServer()
                .getPolicy(new ExternalContentId(BrightcoveConfigPolicy.TEMPLATE));
        return configPolicy.getPreviewPlayerId(getCurrentRootSiteId());
    }

    protected String getElementId() throws CMException {
        return getElementParent().getElementId();
    }

    protected void setMessage(OTextOutput message) {
        this.message = message;
    }

    protected String getCurrentRootSiteId() throws CMException {
        PolicyCMServer cmServer = getCmServer();
        ConfigurationUtil config = getConfiguration(cmServer);
        Policy topPolicy = getContentSession().getTopPolicy();
        ContentId id = topPolicy.getContentId();
        return config.getCurrentRootSiteId(id);
    }

    PolicyCMServer getCmServer() throws CMException {
        return getPolicy().getCMServer();
    }

    public ConfigurationUtil getConfiguration(PolicyCMServer cmServer){
        return new ConfigurationUtil(cmServer);
    }
}
