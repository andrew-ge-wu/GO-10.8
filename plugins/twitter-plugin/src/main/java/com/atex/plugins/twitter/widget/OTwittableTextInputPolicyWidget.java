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

import com.atex.plugins.twitter.TwittableInputPolicy;
import com.polopoly.cm.app.ContentSession;
import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.app.widget.OTextInputPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyUtil;
import com.polopoly.cm.policy.PrepareResult;
import com.polopoly.common.logging.LogUtil;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OJavaScript;
import com.polopoly.orchid.widget.OTextInput;
import com.polopoly.orchid.widget.OTextOutput;
import com.polopoly.util.StringUtil;

/*
 * @since 2.0.3
 */
public class OTwittableTextInputPolicyWidget extends OTextInputPolicyWidget implements Editor, Viewer {

    private static final long serialVersionUID = 1L;
    private final static Logger LOG = LogUtil.getLog(OTextInputPolicyWidget.class);

    OTextInput tweetText;
    OJavaScript jsScript;

    @Override
    public void initSelf(OrchidContext oc) throws OrchidException {
        initSelfSuper(oc);
        tweetText = getTweetTextWidget();
        tweetText.setMaxLength(Integer.parseInt(getMaxLength()));
        tweetText.setOnKeyUp("addOnChangeEvent(this);");
        tweetText.setEnabled(ContentSession.EDIT_MODE == getContentSession().getMode());
        addAndInitChild(oc, tweetText);
    }

    @Override
    protected void initValueFromPolicy(OrchidContext oc) throws CMException, OrchidException {
        String text = ((SingleValuePolicy) getPolicy()).getValue();

        if (text == null) {
            text = "";
        }
        tweetText.setText(text);
    }

    @Override
    public void storeSelf() throws CMException {
        ((SingleValuePolicy) getPolicy()).setValue(tweetText.getText().trim());
    }

    @Override
    public void localRender(OrchidContext oc) throws IOException, OrchidException {
        tweetText.render(oc);
    }

    protected void initSelfSuper(OrchidContext oc) throws OrchidException {
        super.initSelf(oc);
    }

    protected OTextInput getTweetTextWidget() {
        if (tweetText == null) {
            return new OTextInput();
        }
        return tweetText;
    }

    protected String getMaxLength() {
        String maxLen = PolicyUtil.getParameter(TwittableInputPolicy.PARAM_MAX_LENGTH, getPolicy());
        try {
            if (maxLen != null && Integer.parseInt(maxLen) > 0) {
                return maxLen;
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
        return "";
    }

    public OJavaScript getJsSciptWidget() {
        if (jsScript == null) {
            return new OJavaScript();
        }
        return jsScript;
    }

}