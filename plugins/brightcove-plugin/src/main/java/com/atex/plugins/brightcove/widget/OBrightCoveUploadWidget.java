/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove.widget;

import static com.polopoly.orchid.util.WidgetUtil.escapeJSString;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.plugins.brightcove.BrightcoveConfigPolicy;
import com.atex.plugins.brightcove.BrightcoveVideoPolicy;
import com.atex.plugins.brightcove.service.BrightcoveService;
import com.atex.plugins.brightcove.util.BrightCoveVideoUploader;
import com.atex.plugins.brightcove.util.ConfigurationUtil;
import com.brightcove.mediaapi.exceptions.BrightcoveException;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.util.PolicyWidgetUtil;
import com.polopoly.cm.app.widget.OFCKEditorPolicyWidget;
import com.polopoly.cm.app.widget.OFieldPolicyWidget;
import com.polopoly.cm.app.widget.OTextAreaPolicyWidget;
import com.polopoly.cm.app.widget.OTextInputPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.policy.PolicyUtil;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.event.OrchidEvent;
import com.polopoly.orchid.event.WidgetEventListener;
import com.polopoly.orchid.widget.OFileInput;
import com.polopoly.orchid.widget.OJavaScript;
import com.polopoly.orchid.widget.OSubmitButton;
import com.polopoly.util.LocaleUtil;

public class OBrightCoveUploadWidget extends OFieldPolicyWidget implements Editor, Viewer{

    private static final long serialVersionUID = 1L;

    public static final String BCVIDEO_RESOURCE_VIDEOID_FIELD = "menu/standard/brightcovecolumholder/brightcovecolumn/video";

    private static final String JAVASCRIPT = "document.getElementById(\"%s\").value=\"%s\";";

    private static final String LOCALE_LBL_PUSH_BRIGHTCOVE = "com.atex.plugins.brightcove.video.upload.pushtobc";

    private static final String LOCALE_MSG_EMPTY_TOKEN = "com.atex.plugins.brightcove.video.upload.failedemptytokenmsg";

    private static final String LOCALE_MSG_CM_CONFIG = "com.atex.plugins.brightcove.video.upload.failedconfigmsg";

    private static final String LOCALE_MSG_IO = "com.atex.plugins.brightcove.video.upload.failediomsg";

    private static final String LOCALE_MSG_UPLOAD_FAILED = "com.atex.plugins.brightcove.video.upload.failedmsg";

    private static final String LOCALE_MSG_REQUIRED_SHORTDESC = "com.atex.plugins.brightcove.video.upload.shortdesc.required";

    private static final String LOCALE_MSG_REQUIRED_TITLE = "com.atex.plugins.brightcove.video.upload.title.required";

    private static final String NAME = "menu/standard/brightcovecolumholder/brightcovecolumn/name";

    private static final String SHORT_DESCRIPTION = "menu/standard/brightcovecolumholder/brightcovecolumn/lead";

    private static final String LONG_DESCIPTION = "menu/standard/body";

    private static final Logger LOGGER = Logger.getLogger(OBrightCoveUploadWidget.class.getName());

    private OJavaScript valueCopierScript;

    private OJavaScript oJavascript;

    OFileInput fileInput;

    OSubmitButton pushVideoButton;

    BrightCoveVideoUploader uploader;

    @Override
    public void initSelf(OrchidContext oc) throws OrchidException {
        fileInput = getOFileInput();
        fileInput.addNewFileListener(new FileUploadListner());
        pushVideoButton = getOSubmitButton();
        pushVideoButton.setLabel(LocaleUtil.format(LOCALE_LBL_PUSH_BRIGHTCOVE, oc.getMessageBundle()));
        pushVideoButton.addSubmitListener(new UpdateMetadataEventListener());
        addAndInitChild(oc, fileInput);
        fileInput.addAndInitChild(oc, pushVideoButton);
        valueCopierScript = getOJavaScript();
        addAndInitChild(oc, valueCopierScript);
        oJavascript = getOJavaScript();
        addAndInitChild(oc, oJavascript);
    }

    @Override
    public void localRender(OrchidContext oc) throws OrchidException, IOException {
        boolean isEnabled = PolicyWidgetUtil.isEditMode(this);
        fileInput.setEnabled(isEnabled);
        fileInput.setVisible(isEnabled);
        fileInput.render(oc);
        pushVideoButton.setEnabled(isEnabled);
        pushVideoButton.setVisible(isEnabled);
        pushVideoButton.render(oc);
        valueCopierScript.render(oc);
        oJavascript.render(oc);
    }

    class FileUploadListner implements WidgetEventListener {

        public void processEvent(OrchidContext oc, OrchidEvent oe) throws OrchidException {
            resetValidationError();
            doUploadVideo(oc);
        }

    }

    class UpdateMetadataEventListener implements WidgetEventListener {

        public void processEvent(OrchidContext oc, OrchidEvent oe) throws OrchidException {
            try {
                getContentSession().getTopWidget().store();
                BrightcoveVideoPolicy policy = getBrightcoveVideoPolicy();
                BrightcoveConfigPolicy config = (BrightcoveConfigPolicy) getCmServer().getPolicy(BrightcoveConfigPolicy.CONTENT_ID);
                if (policy.getId() != null && !policy.getId().isEmpty()) {
                    BrightcoveService service = getBrightcoveService();
                    service.updateVideo(policy.toVideo(config.getMappings(getCurrentRootSiteId())));
                    updateTextField(policy);
                    LOGGER.log(Level.INFO, "Pushed Brightcove Id: "+policy.getId()+ " metadata to Brightcove");
                }
            } catch (BrightcoveException e) {
                handleErrors(oc, e.getMessage());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, e.getMessage());
            }
        }

    }

    private BrightcoveVideoPolicy getTopPolicy() throws CMException {
        getContentSession().getTopWidget().store();
        BrightcoveVideoPolicy policy =  (BrightcoveVideoPolicy) getContentSession().getTopPolicy();
        return policy;
    }

    private String getWriteToken(OrchidContext oc) throws CMException, BrightcoveException {
        PolicyCMServer cmServer = getContentSession().getPolicyCMServer();
        BrightcoveConfigPolicy configPolicy = (BrightcoveConfigPolicy) cmServer.getPolicy(BrightcoveConfigPolicy.CONTENT_ID);
        String token = configPolicy.getWriteToken(getCurrentRootSiteId());
        String message = "";
        if (token == null || token.isEmpty()) {
            message = LocaleUtil.format(LOCALE_MSG_EMPTY_TOKEN, oc.getMessageBundle());
            throw new BrightcoveException(message);
        }
        return token;
    }

    public void doUploadVideo(OrchidContext oc) throws OrchidException {
        BrightcoveVideoPolicy contentPolicy ;
        String vName;
        String vShortDesc;
        String message = "";
        String bcVideoId = "";
        StringBuffer sb = new StringBuffer();
        try {
            contentPolicy =  getTopPolicy();
            vName = contentPolicy.getName();
            vShortDesc = contentPolicy.getChildPolicy("lead").getComponent("value");
            if (vName==null || vName.isEmpty()) {
                message = LocaleUtil.format(LOCALE_MSG_REQUIRED_TITLE, oc.getMessageBundle());
                throw new BrightcoveException(message);
            }
            if (vShortDesc==null || vShortDesc.isEmpty()) {
                message = LocaleUtil.format(LOCALE_MSG_REQUIRED_SHORTDESC, oc.getMessageBundle());
                throw new BrightcoveException(message);
            }
            String writeToken = getWriteToken(oc);
            BrightcoveConfigPolicy config = (BrightcoveConfigPolicy) getCmServer().getPolicy(BrightcoveConfigPolicy.CONTENT_ID);
            Map<String, String> mappings = config.getMappings(getCurrentRootSiteId());
            bcVideoId = getUploader(writeToken).upload(contentPolicy, fileInput.getFileData(), writeToken, fileInput.getFileName(), mappings);
            if (bcVideoId!=null && !bcVideoId.isEmpty()) {
                LOGGER.log(Level.INFO, "Video uploaded " + bcVideoId);
                getTopPolicy().getChildPolicy("video").setComponent("value", bcVideoId);
                OTextInputPolicyWidget videoId = (OTextInputPolicyWidget) getContentSession()
                        .findPolicyWidget(BCVIDEO_RESOURCE_VIDEOID_FIELD);
                sb.append(String.format(JAVASCRIPT, videoId.getChildWidget().getCompoundId(), escapeJSString(bcVideoId)));
                valueCopierScript.setScript(sb.toString());
            } else {
                message = LocaleUtil.format(LOCALE_MSG_UPLOAD_FAILED, oc.getMessageBundle());
                handleErrors(oc, message);
            }
        } catch (IOException e) {
            message = LocaleUtil.format(LOCALE_MSG_IO, oc.getMessageBundle());
            handleErrors(oc, message);
        } catch (CMException e) {
            message = LocaleUtil.format(LOCALE_MSG_CM_CONFIG, oc.getMessageBundle());
            handleErrors(oc, message);
        } catch (BrightcoveException e) {
            handleErrors(oc, e.getMessage());
        }
    }

    public void updateTextField(BrightcoveVideoPolicy policy) throws OrchidException, CMException {
        String jScript = "$('#%s').val('%s');";
        String onLoad = "$(function(){%s});";

        StringBuffer sb = new StringBuffer();
        // set name
        OTextInputPolicyWidget name = (OTextInputPolicyWidget) getContentSession().findPolicyWidget(NAME);
        sb.append(String.format(jScript, name.getChildWidget().getCompoundId(), escapeJSString(policy.getName())));
        // set short description
        OTextAreaPolicyWidget shortDescription = (OTextAreaPolicyWidget) getContentSession().findPolicyWidget(SHORT_DESCRIPTION);
        sb.append(String.format(jScript, shortDescription.getChildWidget().getCompoundId(), escapeJSString(policy.getShortDescription())));
        // set long description
        OFCKEditorPolicyWidget longDescription = (OFCKEditorPolicyWidget) getContentSession().findPolicyWidget(LONG_DESCIPTION);
        sb.append(String.format(jScript, longDescription.getChildWidget().getCompoundId(), escapeJSString(policy.getLongDescription())));

        oJavascript.setScript(String.format(onLoad, sb.toString()));
    }

    // for unit test only
    BrightCoveVideoUploader getUploader(String writeToken) throws CMException {
        if (uploader==null) {
            uploader = new BrightCoveVideoUploader(writeToken, getCurrentRootSiteId());
        }
        return uploader;
    }

    String upload(BrightcoveVideoPolicy contentPolicy, OFileInput fileInput, String token, Map<String, String> mapping) throws CMException, BrightcoveException, IOException {
        return getUploader(token).upload(contentPolicy, fileInput.getFileData(), token, fileInput.getFileName(), mapping);
    }
    
    OFileInput getOFileInput() {
        return new OFileInput();
    }
    
    OSubmitButton getOSubmitButton() {
        return new OSubmitButton();
    }
    
    OJavaScript getOJavaScript() {
        return new OJavaScript();
    }
    
    void handleErrors(OrchidContext oc, String message) {
        handleError(oc, message);
    }

    BrightcoveVideoPolicy getBrightcoveVideoPolicy() throws CMException {
        return (BrightcoveVideoPolicy) PolicyUtil.getTopPolicy(getPolicy());
    }

    BrightcoveService getBrightcoveService() throws CMException {
        String siteId = getCurrentRootSiteId();
        return new BrightcoveService(getPolicy().getCMServer(), siteId);
    }
    
    PolicyCMServer getCmServer() throws CMException {
        return getPolicy().getCMServer();
    }

    protected String getCurrentRootSiteId() throws CMException {
        PolicyCMServer cmServer = getCmServer();
        ConfigurationUtil config = getConfiguration(cmServer);
        Policy topPolicy = getContentSession().getTopPolicy();
        ContentId id = topPolicy.getContentId();
        return config.getCurrentRootSiteId(id);
    }

    public ConfigurationUtil getConfiguration(PolicyCMServer cmServer){
        return new ConfigurationUtil(cmServer);
    }
}
