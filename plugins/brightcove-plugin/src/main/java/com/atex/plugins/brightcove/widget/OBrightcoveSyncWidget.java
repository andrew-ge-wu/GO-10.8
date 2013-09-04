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
import java.net.URL;
import java.net.URLConnection;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;

import com.atex.plugins.brightcove.BrightcoveConfigPolicy;
import com.atex.plugins.brightcove.BrightcoveVideoPolicy;
import com.atex.plugins.brightcove.service.BrightcoveService;
import com.atex.plugins.brightcove.util.ConfigurationUtil;
import com.brightcove.commons.catalog.objects.Video;
import com.brightcove.mediaapi.exceptions.BrightcoveException;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.imagemanager.ImageFormatException;
import com.polopoly.cm.app.imagemanager.ImageManagerPolicy;
import com.polopoly.cm.app.imagemanager.ImageTooBigException;
import com.polopoly.cm.app.policy.SelectableSubFieldPolicy;
import com.polopoly.cm.app.util.PolicyWidgetUtil;
import com.polopoly.cm.app.widget.OComplexFieldPolicyWidget;
import com.polopoly.cm.app.widget.OFCKEditorPolicyWidget;
import com.polopoly.cm.app.widget.OSelectableSubFieldPolicyWidget;
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
import com.polopoly.orchid.widget.OJavaScript;
import com.polopoly.orchid.widget.OSubmitButton;
import com.polopoly.util.LocaleUtil;

/**
 * @since 1.0.2
 */
public class OBrightcoveSyncWidget extends OComplexFieldPolicyWidget implements Editor, Viewer {
    private static final long serialVersionUID = 1L;
    private static final String ON_LOAD = "$(function(){%s});";
    private static final String JAVASCRIPT = "$('#%s').val('%s');";
    public static final String NAME = "menu/standard/brightcovecolumholder/brightcovecolumn/name";
    public static final String SHORT_DESCRIPTION = "menu/standard/brightcovecolumholder/brightcovecolumn/lead";
    public static final String LONG_DESCIPTION = "menu/standard/body";
    public static final String IMAGE_TYPE = "menu/standard/brightcovecolumholder/brightcovecolumn/imageType";

    private OJavaScript valueCopierScript;
    private OSubmitButton syncButton;
    private OSubmitButton previewButton;
    private ResourceBundle rb;
    private BrightcoveService brightcoveService;

    @Override
    public void initSelf(OrchidContext oc) throws OrchidException {
        super.initSelf(oc);
        this.rb = oc.getMessageBundle();
        try {
            brightcoveService = getBrightcoveService();
        } catch (CMException e) {
            throw new OrchidException(e.getMessage(), e);
        }

        valueCopierScript = getOJavaScript();
        addAndInitChild(oc, valueCopierScript);

        syncButton = getOButton();
        syncButton.addSubmitListener(new SyncEventListener());
        syncButton.setLabel(getString("com.atex.plugins.brightcove.label.pull"));
        addAndInitChild(oc, syncButton);

        previewButton = getOButton();
        previewButton.addSubmitListener(new PreviewEventListener());
        previewButton.setLabel(getString("com.atex.plugins.brightcove.label.update.preview"));

        addAndInitChild(oc, previewButton);
    }

    @Override
    public void localRender(OrchidContext oc) throws OrchidException, IOException {
        super.localRender(oc);
        if (!PolicyWidgetUtil.isEditMode(this)) {
            return;
        }
        valueCopierScript.render(oc);
        syncButton.render(oc);
        previewButton.render(oc);
    }

    class PreviewEventListener implements WidgetEventListener {
        public void processEvent(OrchidContext oc, OrchidEvent oe) throws OrchidException {
            try {
                getContentSession().getTopWidget().store();
                BrightcoveVideoPolicy policy = (BrightcoveVideoPolicy) PolicyUtil.getTopPolicy(getPolicy());
                if (policy.getId() == null || policy.getId().trim().isEmpty()) {
                    OBrightcoveSyncWidget.this.handleError(oc, getString("com.atex.plugins.brightcove.brightcove.id.required"));
                }
            } catch (CMException e) {
                throw new OrchidException(e.getMessage(), e);
            }
        }
    }

    class SyncEventListener implements WidgetEventListener {
        public void processEvent(OrchidContext oc, OrchidEvent oe) throws OrchidException {
            try {
                getContentSession().getTopWidget().store();
                BrightcoveVideoPolicy policy = (BrightcoveVideoPolicy) PolicyUtil.getTopPolicy(getPolicy());

                if (policy.getId() == null || policy.getId().trim().isEmpty()) {
                    OBrightcoveSyncWidget.this.handleError(oc, getString("com.atex.plugins.brightcove.brightcove.id.required"));
                } else {
                    Video video = brightcoveService.findByVideoID(Long.parseLong(policy.getId()));

                    StringBuffer sb = new StringBuffer();
                    // set name
                    OTextInputPolicyWidget name = (OTextInputPolicyWidget) getContentSession().findPolicyWidget(NAME);
                    sb.append(String.format(JAVASCRIPT, name.getChildWidget().getCompoundId(), escapeJSString(video.getName())));
                    // set short description
                    OTextAreaPolicyWidget shortDescription = (OTextAreaPolicyWidget) getContentSession().findPolicyWidget(
                            SHORT_DESCRIPTION);
                    sb.append(String.format(JAVASCRIPT, shortDescription.getChildWidget().getCompoundId(),
                            escapeJSString(video.getShortDescription())));
                    // set long description
                    OFCKEditorPolicyWidget longDescription = (OFCKEditorPolicyWidget) getContentSession().findPolicyWidget(
                            LONG_DESCIPTION);
                    sb.append(String.format(JAVASCRIPT, longDescription.getChildWidget().getCompoundId(),
                            escapeJSString(video.getLongDescription())));

                    // set categorization
                    BrightcoveConfigPolicy config = (BrightcoveConfigPolicy) getCmServer().getPolicy(BrightcoveConfigPolicy.CONTENT_ID);
                    policy.mergeCategorization(video.getTags(), video.getCustomFields(), config.getMappings());
                    // set thumbnail
                    boolean needReloadPage = addThubmail(policy, video.getVideoStillUrl() == null ? video.getThumbnailUrl()
                            : video.getVideoStillUrl());

                    if (needReloadPage) {
                        OSelectableSubFieldPolicyWidget imageType = (OSelectableSubFieldPolicyWidget) getContentSession()
                                .findPolicyWidget(IMAGE_TYPE);

                        // to prevent infinity submitting form, form will submit
                        // if
                        // selected value is not 'httpImage'
                        sb.append(String.format("if($('#%s select').val()!='httpImage'){", imageType.getCompoundId()));
                        sb.append(String.format("$('#%s select').val('httpImage').change();", imageType.getCompoundId()));
                        sb.append("}");
                    }

                    valueCopierScript.setScript(String.format(ON_LOAD, sb.toString()));
                }
            } catch (CMException e) {
                throw new OrchidException(e.getMessage(), e);
            } catch (BrightcoveException e) {
                handleErrors(oc, e.getMessage());
                throw new OrchidException(e.getMessage(), e);
            } catch (IOException e) {
                throw new OrchidException(e.getMessage(), e);
            } catch (ImageFormatException e) {
                throw new OrchidException(e.getMessage(), e);
            } catch (ImageTooBigException e) {
                throw new OrchidException(e.getMessage(), e);
            }
        }
    }

    boolean addThubmail(BrightcoveVideoPolicy policy, String url)
            throws CMException, IOException, ImageFormatException, OrchidException, ImageTooBigException {
        if (StringUtils.isNotBlank(url)) {
            SelectableSubFieldPolicy imageType = policy.getSubFieldPolicy();

            boolean reloadPage = !BrightcoveVideoPolicy.IMAGE.equalsIgnoreCase(imageType.getSelectedSubFieldName());
            imageType.setSelectedSubFieldName(BrightcoveVideoPolicy.IMAGE);
            ImageManagerPolicy image = (ImageManagerPolicy) imageType.getChildPolicy(BrightcoveVideoPolicy.IMAGE);

            URL imageUrl = new URL(url);
            URLConnection conn = imageUrl.openConnection();
            image.importImage("image/image.png", conn.getInputStream());

            return reloadPage;
        }
        return false;
    }

    void handleErrors(OrchidContext oc, String message) {
        handleError(oc, message);
    }

    OSubmitButton getOButton() {
        return new OSubmitButton();
    }

    OJavaScript getOJavaScript() {
        return new OJavaScript();
    }

    BrightcoveService getBrightcoveService() throws CMException {
        String siteId = getCurrentRootSiteId();
        return new BrightcoveService(getPolicy().getCMServer(), siteId);
    }

    String getString(String key) {
        return LocaleUtil.format(key, rb);
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
