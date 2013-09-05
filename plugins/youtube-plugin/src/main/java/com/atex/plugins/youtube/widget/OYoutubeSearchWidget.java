/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */
package com.atex.plugins.youtube.widget;

import static com.polopoly.orchid.util.WidgetUtil.escapeJSString;
import static com.polopoly.orchid.util.WidgetUtil.escapeString;
import static com.polopoly.util.LocaleUtil.format;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.plugins.youtube.YouTubeServiceWrapper;
import com.atex.plugins.youtube.YoutubeElementPolicy;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.util.PolicyWidgetUtil;
import com.polopoly.cm.app.widget.OFieldPolicyWidget;
import com.polopoly.cm.app.widget.OTextInputPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.event.OrchidEvent;
import com.polopoly.orchid.event.WidgetEventListener;
import com.polopoly.orchid.widget.OButton;
import com.polopoly.orchid.widget.OImage;
import com.polopoly.orchid.widget.OJavaScript;
import com.polopoly.orchid.widget.OLabel;
import com.polopoly.orchid.widget.OSelect;
import com.polopoly.orchid.widget.OSubmitButton;
import com.polopoly.orchid.widget.OTextInput;

public class OYoutubeSearchWidget extends OFieldPolicyWidget implements Editor, Viewer {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(OYoutubeSearchWidget.class.getName());
    public static final int LIMIT = 10;
    public static final String YID_ATTRIBUTE = "data-yid";
    private static final String JAVASCRIPT = "document.getElementById(\"%s\").value=\"%s\";";
    public static final String YOUTUBE_ELEMENT_COLUM_NAME = "youtubeelementcolumholder/youtubeelementcolumn/name";
    public static final String YOUTUBE_ELEMENT_COLUMN_YID = "youtubeelementcolumholder/youtubeelementcolumn/yid";

    private OTextInput searchInput;
    private OSelect orderBySelect;
    private OSubmitButton searchButton;
    private transient VideoFeed searchResults;
    protected List<OImage> images;
    protected List<OSubmitButton> previewButtons;

    private OJavaScript valueCopierScript;
    private boolean youtubeOffline;

    @Override
    public void initSelf(final OrchidContext oc) throws OrchidException {
        super.initSelf(oc);
        ResourceBundle rb = oc.getMessageBundle();
        initSearchInput(rb);
        initSelect(rb);
        initSearchButton(rb);
        initJavascript(oc);

        images = new ArrayList<OImage>(LIMIT);
        previewButtons = new ArrayList<OSubmitButton>(LIMIT);
    }

    @Override
    public void localRender(final OrchidContext oc) throws OrchidException, IOException {
        // only shows search panel in edit mode
        if (!PolicyWidgetUtil.isEditMode(this)) {
            return;
        }
        valueCopierScript.render(oc);
        clearSearchResults();
        super.localRender(oc);
        renderSearchResult(oc);
    }

    protected void renderSearchResult(final OrchidContext oc) throws IOException, OrchidException {
        Device device = oc.getDevice();
        device.print(String.format("<div class=\"inlinehelp\" style=\"display:inline;\">%s</div>",
                format("com.atex.plugins.youtube.help", oc.getMessageBundle())));
        if (searchResults != null && searchResults.getTotalResults() > 0) {
            device.print("<div class='resultListView'><div class='result'>");
            device.print("<table style='width:100%'>");
            // pre compute labels outside loop
            String label = format("com.atex.plugins.youtube.label.select", oc.getMessageBundle());
            String viewCount = format("com.atex.plugins.youtube.label.view.count", oc.getMessageBundle());

            for (int index = 0; index < searchResults.getEntries().size() && index < LIMIT; index++) {
                VideoEntry entry = searchResults.getEntries().get(index);
                if (entry.getMediaGroup().getThumbnails().size() > 0) {
                    String even = (index + 1) % 2 == 0 ? "even" : "odd";
                    device.print("<tr class='" + even + "'><td style='padding-right:6px;padding-bottom:6px;width:120px'>");
                    OImage image = createImage(oc, entry);
                    images.add(image);
                    device.print("</td><td style='vertical-align:top;padding-top:8px'>");
                    device.print("<h2>" + image.getTitle() + "</h2>");
                    device.print("<h3>" + viewCount + getViewCount(entry) + "<h3><br />");
                    device.print("</td><td style='vertical-align:top;padding-top:8px'>");
                    OSubmitButton previewButton = createButton(oc, entry, label);
                    previewButtons.add(previewButton);
                    device.print("</td></tr>");
                }
            }
            device.print("</table></div></div>");
        }
    }

    private long getViewCount(VideoEntry entry) {
        try {
            return entry.getStatistics().getViewCount();
        } catch (NullPointerException e) {
            return 0l;
        }
    }

    private class PreviewEventListener implements WidgetEventListener {
        public void processEvent(OrchidContext oc, OrchidEvent e) throws OrchidException {
            OSubmitButton widget = (OSubmitButton) e.getWidget();
            String yid = widget.getExtraAttribute(YID_ATTRIBUTE);
            String name = widget.getTitle();
            try {
                getContentSession().getTopWidget().store();
                YoutubeElementPolicy policy = (YoutubeElementPolicy) getContentSession().getTopPolicy();
                policy.setYid(yid);
                policy.setName(name);
                OTextInputPolicyWidget youtubeName = (OTextInputPolicyWidget) getContentSession().findPolicyWidget(
                        YOUTUBE_ELEMENT_COLUM_NAME);
                StringBuffer sb = new StringBuffer();

                if (youtubeName != null) {
                    sb.append(String.format(JAVASCRIPT, youtubeName.getChildWidget().getCompoundId(), escapeJSString(name)));
                }
                OTextInputPolicyWidget youtubeId = (OTextInputPolicyWidget) getContentSession().findPolicyWidget(
                        YOUTUBE_ELEMENT_COLUMN_YID);
                if (youtubeId != null) {
                    sb.append(String.format(JAVASCRIPT, youtubeId.getChildWidget().getCompoundId(), escapeJSString(yid)));
                }
                if (sb.length() > 0) {
                    valueCopierScript.setScript(sb.toString());
                }

            } catch (CMException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }

    protected OImage createImage(final OrchidContext oc, final VideoEntry entry) throws OrchidException, IOException {
        OImage image = newImage();
        try {
            image.setSrc(entry.getMediaGroup().getThumbnails().get(0).getUrl());
            String title = entry.getTitle().getPlainText() == null ? "" : entry.getTitle().getPlainText();
            // escape single quote and double quotes to avoid corrupted html
            title = escapeString(title);
            image.setAltText(title);
            image.setTitle(title);
            image.setExtraAttribute(YID_ATTRIBUTE, getVideoId(entry.getId()));
            addAndInitChild(oc, image);
            image.render(oc);
        } catch (NullPointerException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
        return image;
    }

    protected OImage newImage() {
        return new OImage();
    }

    protected OSubmitButton createButton(final OrchidContext oc, final VideoEntry entry, final String label)
            throws OrchidException, IOException {
        OSubmitButton previewButton = newButton();
        try {
            previewButton.setLabel(label);
            String title = entry.getTitle().getPlainText() == null ? "" : entry.getTitle().getPlainText();
            // escape single quote and double quotes to avoid corrupted html
            title = escapeString(title);
            previewButton.setTitle(title);
            previewButton.setExtraAttribute(YID_ATTRIBUTE, getVideoId(entry.getId()));
            addAndInitChild(oc, previewButton);
            previewButton.render(oc);
            previewButton.addSubmitListener(new PreviewEventListener());
        } catch (NullPointerException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
        return previewButton;
    }

    protected OSubmitButton newButton() {
        return new OSubmitButton();
    }

    protected void initSearchInput(final ResourceBundle rb) throws OrchidException {
        String searchLabel = format("com.atex.plugins.youtube.label.search", rb);
        OLabel label = new OLabel();
        label.setText(searchLabel);
        addChild(label);
        searchInput = new OTextInput();
        searchInput.setTitle(searchLabel);
        addChild(searchInput);
    }

    protected void initSelect(final ResourceBundle rb) throws OrchidException {
        OLabel label = new OLabel();
        label.setText(format("com.atex.plugins.youtube.label.order.by", rb));
        addChild(label);
        orderBySelect = new OSelect();
        orderBySelect.clearOptions();
        orderBySelect.addOption(format("com.atex.plugins.youtube.label.relevance", rb), "1");
        orderBySelect.addOption(format("com.atex.plugins.youtube.label.views", rb), "2");
        orderBySelect.addOption(format("com.atex.plugins.youtube.label.updated", rb), "3");
        orderBySelect.addOption(format("com.atex.plugins.youtube.label.rating", rb), "4");
        addChild(orderBySelect);
    }

    protected void initSearchButton(final ResourceBundle rb) throws OrchidException {
        searchButton = new OSubmitButton();
        searchButton.setLabel(format("com.atex.plugins.youtube.label.search", rb));
        searchButton.addSubmitListener(new WidgetEventListener() {

            public void processEvent(OrchidContext oc, OrchidEvent e) throws OrchidException {
                String keyword = searchInput.getText();
                if (keyword != null && !keyword.trim().isEmpty()) {
                    YouTubeServiceWrapper wrapper = new YouTubeServiceWrapper();
                    searchResults = wrapper.search(keyword, Integer.valueOf(orderBySelect.getSelectedValue()), oc);
                    clearSearchResults();
                    youtubeOffline = wrapper.isOffline();
                }
            }
        });
        searchButton.setHashOnSubmit(getCompoundId());
        addChild(searchButton);
    }

    private void initJavascript(final OrchidContext oc) throws OrchidException {
        valueCopierScript = new OJavaScript();
        addAndInitChild(oc, valueCopierScript);
    }

    protected void clearSearchResults() {
        if (images == null)
            images = new ArrayList<OImage>();
        for (OImage image : images) {
            discardChild(image);
        }
        images.clear();

        if (previewButtons == null)
            previewButtons = new ArrayList<OSubmitButton>();
        for (OButton submitButton : previewButtons) {
            discardChild(submitButton);
        }
        previewButtons.clear();
    }

    protected String getVideoId(String videoEntryId) {
        if (videoEntryId == null) {
            return "";
        }
        int idx = videoEntryId.indexOf("video:");
        if (idx > 0) {
            return videoEntryId.substring(idx + 6);
        }
        return "";
    }

    // getters in protected visibilities for test

    /**
     * @return the searchInput
     */
    protected OTextInput getSearchInput() {
        return searchInput;
    }

    /**
     * @return the orderBySelect
     */
    protected OSelect getOrderBySelect() {
        return orderBySelect;
    }

    /**
     * @return the searchButton
     */
    protected OSubmitButton getSearchButton() {
        return searchButton;
    }

    /**
     * @return the searchResults
     */
    protected VideoFeed getSearchResults() {
        return searchResults;
    }

    public boolean isYoutubeOffline() {
        return youtubeOffline;
    }
}
