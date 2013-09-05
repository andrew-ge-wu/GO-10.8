/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.deptdrpq.widget;

import com.atex.plugins.baseline.url.OrchidUrlResolver;
import com.atex.plugins.baseline.url.UrlResolver;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.widget.ORadioButtonGroupPolicyWidget;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OJavaScript;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OAutoDeptRadioButtonGroupPolicyWidget extends ORadioButtonGroupPolicyWidget {
    private static final long serialVersionUID = 1L;

    private static Logger LOG = Logger.getLogger(OAutoDeptRadioButtonGroupPolicyWidget.class.getName());
    public static final String FILES = "plugins.com.atex.plugins.department-driven-publishing-queue-plugin.files";

    protected String deptPQWidgetJsPath;
    protected OJavaScript deptPQScript;

    protected transient UrlResolver urlResolver;

    @Override
    public void localRender(OrchidContext oc) throws IOException, OrchidException {
        if (deptPQWidgetJsPath == null)
            deptPQWidgetJsPath = lookupContentFile(FILES, "script/deptPQWidget.js", oc);

        deptPQScript.setSrc(deptPQWidgetJsPath);
        deptPQScript.render(oc);

        super.localRender(oc);
    }

    @Override
    public void initSelf(OrchidContext oc) throws OrchidException {
        super.initSelf(oc);

        deptPQScript = new OJavaScript();
        addAndInitChild(oc, deptPQScript);
    }

    protected String lookupContentFile(String externalId, String fileName, OrchidContext oc) {
        try {
            if (urlResolver == null)
                urlResolver = new OrchidUrlResolver(oc);
            return urlResolver.getFileUrl(new ExternalContentId(externalId), fileName);
        } catch (Exception e) {
            LOG.log(Level.WARNING, e.getMessage(), e.fillInStackTrace());
            return null;
        }
    }
}
