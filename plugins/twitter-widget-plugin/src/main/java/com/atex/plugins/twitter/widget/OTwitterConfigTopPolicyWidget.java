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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.util.URLBuilder;
import com.polopoly.cm.app.widget.OTopPolicyWidget;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OJavaScript;

/**
 * 
 * Override OTopPolicyWidget
 *
 */
public class OTwitterConfigTopPolicyWidget extends OTopPolicyWidget {
    private static final long serialVersionUID = 1L;

    private static Logger LOG = Logger.getLogger(OTwitterConfigTopPolicyWidget.class.getName());

    public static final String FILES = "plugins.com.atex.plugins.twitter-plugin.files";
    public static final String MINICOLORS_CSS_FILE_PATH = "jquery.miniColors.css";
    public static final String MINICOLORS_JS_FILE_PATH = "script/jquery.miniColors.js";
    public static final String TWITTER_CONFIG_JS_FILE_PATH = "script/twitterConfigWidget.js";

    protected String miniColorsJsPath;
    protected OJavaScript miniColorsScript;

    protected String twitterConfigWidgetJsPath;
    protected OJavaScript twitterConfigScript;

    protected String miniColorsCssPath;

    @Override
    public void localRender(OrchidContext oc) throws IOException, OrchidException {
        super.localRender(oc);

        Device device = oc.getDevice();

        if (miniColorsCssPath == null)
            miniColorsCssPath = lookupContentFile(FILES, MINICOLORS_CSS_FILE_PATH, oc);

        device.println(String.format("<link type=\"text/css\" rel=\"stylesheet\" href=\"%s\" />", miniColorsCssPath));

        if (miniColorsJsPath == null)
            miniColorsJsPath = lookupContentFile(FILES, MINICOLORS_JS_FILE_PATH, oc);

        miniColorsScript.setSrc(miniColorsJsPath);
        miniColorsScript.render(oc);

        if (twitterConfigWidgetJsPath == null)
            twitterConfigWidgetJsPath = lookupContentFile(FILES, TWITTER_CONFIG_JS_FILE_PATH, oc);

        twitterConfigScript.setSrc(twitterConfigWidgetJsPath);
        twitterConfigScript.render(oc);
    }

    @Override
    public void initSelf(OrchidContext oc) throws OrchidException {
        miniColorsScript = new OJavaScript();
        addAndInitChild(oc, miniColorsScript);

        twitterConfigScript = new OJavaScript();
        addAndInitChild(oc, twitterConfigScript);
    }

    /**
     * Lookup resource content file path
     * @param externalId External content id
     * @param fileName Resource file name
     * @param oc OrchidContext
     * @return Resource file path
     */
    protected String lookupContentFile(String externalId, String fileName, OrchidContext oc) {
        try {
            return URLBuilder.getFileUrl(new ExternalContentId(externalId), fileName, oc);
        } catch (OrchidException e) {
            LOG.log(Level.WARNING, e.getMessage(), e.fillInStackTrace());
        }
        return null;
    }
}
