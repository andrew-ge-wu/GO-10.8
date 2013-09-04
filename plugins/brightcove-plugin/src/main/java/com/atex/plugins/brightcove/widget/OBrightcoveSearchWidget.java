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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.atex.plugins.brightcove.BrightCoveElementPolicy;
import com.atex.plugins.brightcove.service.BrightcoveService;
import com.atex.plugins.brightcove.service.Order;
import com.atex.plugins.brightcove.service.Sort;
import com.atex.plugins.brightcove.service.Type;
import com.atex.plugins.brightcove.util.ConfigurationUtil;
import com.brightcove.commons.catalog.objects.Video;
import com.brightcove.commons.catalog.objects.Videos;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.util.PolicyWidgetUtil;
import com.polopoly.cm.app.widget.OFieldPolicyWidget;
import com.polopoly.cm.app.widget.OTextInputPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.event.OrchidEvent;
import com.polopoly.orchid.event.WidgetEventListener;
import com.polopoly.orchid.util.WidgetUtil;
import com.polopoly.orchid.widget.OButton;
import com.polopoly.orchid.widget.OImage;
import com.polopoly.orchid.widget.OJavaScript;
import com.polopoly.orchid.widget.OSelect;
import com.polopoly.orchid.widget.OSubmitButton;
import com.polopoly.orchid.widget.OTextInput;
import com.polopoly.util.LocaleUtil;

public class OBrightcoveSearchWidget extends OFieldPolicyWidget implements Editor, Viewer {

    private static final long serialVersionUID = 2461861517651485750L;
    private static final String JAVASCRIPT = "document.getElementById(\"%s\").value=\"%s\";";

    public static final int LIMIT = BrightcoveService.LIMIT;
    public static final String NAME = "brightcoveelementcolumholder/brightcoveelementcolumn/name";
    public static final String ID = "brightcoveelementcolumholder/brightcoveelementcolumn/playerType/video/video";

    private OJavaScript valueCopierScript;
    private OTextInput searchInput;
    private OSubmitButton searchButton;
    private OSelect typeSelect;
    private OSelect sortSelect;
    private OSelect orderSelect;
    private List<OImage> images;
    private List<OSubmitButton> previewButtons;
    private Videos videos;
    private ResourceBundle rb;
    private PolicyCMServer cmServer;

    @Override
    public void initSelf(OrchidContext oc) throws OrchidException {
        super.initSelf(oc);
        rb = oc.getMessageBundle();
        try {
            cmServer = getPolicy().getCMServer();
        } catch (CMException e) {
            throw new OrchidException(e.getMessage(), e);
        }

        typeSelect = getOSelect();
        for (Type type : Type.values()) {
            typeSelect.addOption(getString(type.getLabel()), type.toString());
        }
        addAndInitChild(oc, typeSelect);

        valueCopierScript = getOJavaScript();
        addAndInitChild(oc, valueCopierScript);

        searchInput = getOTextInput();
        searchInput.setExtraAttribute("placeholder", getString("com.atex.plugins.brightcove.placeholder.keywords"));
        addAndInitChild(oc, searchInput);
        searchButton = getOSubmitButton();
        searchButton.setLabel(getString("com.atex.plugins.brightcove.label.search"));
        searchButton.addSubmitListener(new SearchEventListener());
        addAndInitChild(oc, searchButton);

        sortSelect = getOSelect();
        for (Sort type : Sort.values()) {
            sortSelect.addOption(getString(type.getLabel()), type.toString());
        }
        addAndInitChild(oc, sortSelect);

        orderSelect = getOSelect();
        for (Order type : Order.values()) {
            orderSelect.addOption(getString(type.getLabel()), type.toString());
        }
        addAndInitChild(oc, orderSelect);

        images = new ArrayList<OImage>(LIMIT);
        previewButtons = new ArrayList<OSubmitButton>(LIMIT);
    }

    @Override
    public void localRender(OrchidContext oc) throws OrchidException, IOException {
        if (!PolicyWidgetUtil.isEditMode(this)) {
            return;
        }
        valueCopierScript.render(oc);
        clearSearchResults();
        super.localRender(oc);
        renderSearchResult(oc);
    }

    private void renderSearchResult(final OrchidContext oc) throws IOException, OrchidException {

        Device device = oc.getDevice();
        device.print("<div class='resultListView'><div class='result'>");
        device.print("<table style='width:100%'>");
        device.print(String
                .format(
                // Image Details Last Update Action
                "<tr><th>%s</th><th>%s</th><th style='width: 100px;'>%s</th><th>%s</th></tr>",
                        getString("com.atex.plugins.brightcove.label.image"),
                        getString("com.atex.plugins.brightcove.label.details"),
                        getString("com.atex.plugins.brightcove.label.last.update"),
                        getString("com.atex.plugins.brightcove.label.action")));
        if (videos != null && !videos.isEmpty()) {
            boolean isEven = false;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String selectLabel = getString("com.atex.plugins.brightcove.label.select");
            for (int i = 0; i < videos.size(); i++) {
                Video video = videos.get(i);
                String even = isEven ? "even" : "odd";
                device.print("<tr class='" + even + "'><td style='padding:6px;width:120px'>");
                OImage image = createImage(oc, video);
                images.add(image);
                device.print("</td><td style='vertical-align:top;padding-top:8px'>");
                device.print("<h2>" + video.getName() + "</h2>");
                device.print("<p>" + video.getShortDescription() + "</p><br />");
                device.print("</td><td style='padding-top:8px'>" + format.format(video.getLastModifiedDate()));
                device.print("</td><td style='vertical-align:top;text-align:right;padding-top:8px'>");
                OSubmitButton previewButton = createButton(oc, video, selectLabel);
                previewButtons.add(previewButton);
                device.print("</td></tr>");
                isEven = !isEven;
            }
        } else {
            device.print(String.format("<tr class='odd'><td colspan='4' style='text-align:center;padding:8px'>%s</td></tr>",
                    getString("com.atex.plugins.brightcove.label.no.result")));
        }
        device.print("</table></div></div>");

    }

    private OSubmitButton createButton(OrchidContext oc, Video video, String label) throws OrchidException, IOException {
        OSubmitButton button = getOSubmitButton();
        button.setLabel(label);
        String name = WidgetUtil.escapeString(video.getName());
        button.setTitle(name);
        addAndInitChild(oc, button);
        button.setExtraAttribute("data-name", WidgetUtil.escapeString(video.getName()));
        button.setExtraAttribute("data-id", String.valueOf(video.getId()));
        button.setExtraAttribute("data-tags", video.getTags().toString());
        button.setExtraAttribute("data-reference-id", video.getReferenceId());
        button.render(oc);
        button.addSubmitListener(new PreviewEventListener());
        return button;
    }

    private OImage createImage(OrchidContext oc, Video video) throws OrchidException, IOException {
        OImage image = getOImage();
        image.setSrc(video.getThumbnailUrl());
        String name = WidgetUtil.escapeString(video.getName());
        image.setAltText(name);
        image.setTitle(name);
        image.setExtraAttribute("data-id", String.valueOf(video.getId()));
        addAndInitChild(oc, image);
        image.render(oc);
        return image;
    }

    private void clearSearchResults() {
        for (OImage image : images) {
            discardChild(image);
        }
        images.clear();

        for (OButton submitButton : previewButtons) {
            discardChild(submitButton);
        }
        previewButtons.clear();
    }

    Videos search() throws CMException {
        String q = searchInput.getText();
        if (q == null || q.trim().isEmpty()) {
            return null;
        }
        return new BrightcoveService(cmServer, getCurrentRootSiteId()).search(searchInput.getText(), Type.valueOf(typeSelect.getSelectedValue()),
                Sort.valueOf(sortSelect.getSelectedValue()), Order.valueOf(orderSelect.getSelectedValue()));
    }

    String getString(String key) {
        return LocaleUtil.format(key, rb);
    }

    OSelect getOSelect() {
        return new OSelect();
    }

    OJavaScript getOJavaScript() {
        return new OJavaScript();
    }

    OTextInput getOTextInput() {
        return new OTextInput();
    }

    OSubmitButton getOSubmitButton() {
        return new OSubmitButton();
    }

    OImage getOImage() {
        return new OImage();
    }

    class SearchEventListener implements WidgetEventListener {

        public void processEvent(OrchidContext oc, OrchidEvent oe) throws OrchidException {
            try {
                videos = search();
                clearSearchResults();
            } catch (CMException e) {
                throw new OrchidException(e.getMessage(), e);
            }
        }
    }

    class PreviewEventListener implements WidgetEventListener {

        public void processEvent(OrchidContext oc, OrchidEvent oe) throws OrchidException {
            try {
                OSubmitButton button = (OSubmitButton) oe.getWidget();
                String id = button.getExtraAttribute("data-id");
                String name = button.getExtraAttribute("data-name");

                getContentSession().getTopWidget().store();
                BrightCoveElementPolicy policy = (BrightCoveElementPolicy) getContentSession().getTopPolicy();
                policy.setName(name);
                policy.setVideoId(id);

                StringBuffer sb = new StringBuffer();
                OTextInputPolicyWidget input = (OTextInputPolicyWidget) getContentSession().findPolicyWidget(ID);
                sb.append(String.format(JAVASCRIPT, input.getChildWidget().getCompoundId(), WidgetUtil.escapeJSString(id)));
                input = (OTextInputPolicyWidget) getContentSession().findPolicyWidget(NAME);
                sb.append(String.format(JAVASCRIPT, input.getChildWidget().getCompoundId(), WidgetUtil.escapeJSString(name)));
                valueCopierScript.setScript(sb.toString());

            } catch (CMException e) {
                throw new OrchidException(e.getMessage(), e);
            }

        }

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
