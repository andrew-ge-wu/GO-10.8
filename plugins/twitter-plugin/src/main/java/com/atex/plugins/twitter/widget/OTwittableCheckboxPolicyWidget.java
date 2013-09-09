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

import com.atex.plugins.baseline.url.OrchidUrlResolver;
import com.atex.plugins.baseline.url.UrlResolver;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.ContentSession;
import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.policy.CheckboxPolicy;
import com.polopoly.cm.app.widget.OCheckboxPolicyWidget;
import com.polopoly.cm.app.widget.OPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.common.logging.LogUtil;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.js.JSWidget;
import com.polopoly.orchid.js.JSWidgetUtil;
import com.polopoly.orchid.widget.OCheckbox;
import com.polopoly.orchid.widget.OJavaScript;

/*
 * @since 2.0.3
 */
public class OTwittableCheckboxPolicyWidget extends OCheckboxPolicyWidget
    implements Editor, Viewer, JSWidget{
    private static final long serialVersionUID = 1L;

    private final static Logger LOG = LogUtil.getLog(OTwittableTextInputPolicyWidget.class);
    public static final String FILES = "plugins.com.atex.plugins.twitter-plugin.files";
    protected String twittableTextInputWidgetJsPath;
    protected OJavaScript twittableTextInputScript;
    protected transient UrlResolver urlResolver;

    OCheckbox option;

    @Override
    public void initSelf(OrchidContext oc) throws OrchidException {
        initSelfSuper(oc);
        option = getOptionWidget();
        option.setEnabled(ContentSession.EDIT_MODE == getContentSession().getMode());
        addAndInitChild(oc, option);
        twittableTextInputScript = getTwittableTextInputScript();
        addAndInitChild(oc, twittableTextInputScript);
    }

    @Override
    protected void initValueFromPolicy(OrchidContext oc) throws CMException, OrchidException {
        boolean value = ((CheckboxPolicy) getPolicy()).getChecked();
        option.setChecked(value);
    }

    @Override
    public void storeSelf() throws CMException {
        ((CheckboxPolicy) getPolicy()).setChecked(option.isChecked());
    }

    protected String lookupContentFile(String externalId, String fileName, OrchidContext oc) {
        try {
            if (urlResolver == null)
                urlResolver = getUrlResolver(oc);
            return urlResolver.getFileUrl(getExternalContentId(externalId), fileName);
        } catch (Exception e) {
            LOG.log(Level.WARNING, e.getMessage(), e.fillInStackTrace());
        }
        return null;
    }

    @Override
    public void localRender(OrchidContext oc) throws IOException, OrchidException {
        if (twittableTextInputWidgetJsPath == null)
            twittableTextInputWidgetJsPath = lookupContentFile(FILES, "script/twittableTextInputWidget.js", oc);
        twittableTextInputScript.setSrc(twittableTextInputWidgetJsPath);
        twittableTextInputScript.render(oc);
        option.render(oc);
    }

    public boolean isAjaxTopWidget() {
        return true;
    }

    protected String getParentCompoundId() {
        String parentCompId = "";
        try {
            parentCompId = ((OPolicyWidget)getParentPolicyWidget()).getCompoundId();
        } catch (OrchidException e) {
            LOG.log(Level.INFO, "Unable to retrieve parent compound id " +e);
        }
        return parentCompId;
    }

    public String[] getJSScriptDependencies() {
        return new String[] { twittableTextInputWidgetJsPath};
    }

    public String getFriendlyName() throws OrchidException {
        return getName();
    }

    public String[] getInitParams() throws OrchidException {
        return new String[] { "'"+ this.getParentCompoundId() + "'"};
    }

    public String getInitScript() throws OrchidException {
        return JSWidgetUtil.genInitScript(this);
    }

    public String getJSWidgetClassName() throws OrchidException {
        return "twittableJsWidget";
    }

    protected void initSelfSuper(OrchidContext oc) throws OrchidException {
        super.initSelf(oc);
    }

    protected OCheckbox getOptionWidget() {
        if (option==null) {
            return new OCheckbox();
        }
        return option;
    }

    protected UrlResolver getUrlResolver(OrchidContext oc) {
        return new OrchidUrlResolver(oc);
    }

    protected ExternalContentId getExternalContentId(String externalId) {
        return new ExternalContentId(externalId);
    }

    protected OJavaScript getTwittableTextInputScript() {
        if (twittableTextInputScript==null) {
            return new OJavaScript();
        }
        return twittableTextInputScript;
    }
}
