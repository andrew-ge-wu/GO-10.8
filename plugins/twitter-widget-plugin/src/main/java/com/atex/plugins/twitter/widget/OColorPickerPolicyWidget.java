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

import com.polopoly.cm.app.ContentSession;
import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.widget.OSingleValuePolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OTextInput;
import com.polopoly.orchid.widget.OWidget;
import com.polopoly.util.LocaleUtil;

/**
 * 
 * ColorPicker Policy Widget
 *
 */
public class OColorPickerPolicyWidget extends OSingleValuePolicyWidget implements Viewer, Editor {
    private static final long serialVersionUID = 1L;

    protected OTextInput oTextInput;
    protected String defaultValue;

    @Override
    public void initSelf(OrchidContext oc) throws OrchidException {
        oTextInput = new OTextInput();
        oTextInput.setMaxLength(7);
        oTextInput.setSize(7);
        if (getContentSession().getMode() == ContentSession.VIEW_MODE) {
            oTextInput.setEnabled(false);
        }
        this.defaultValue = LocaleUtil.format(getDefaultValue(), oc.getMessageBundle());
        addAndInitChild(oc, oTextInput);
    }

    @Override
    public void localRender(OrchidContext oc) throws IOException, OrchidException {
        oTextInput.render(oc);
    }

    @Override
    public void initValueFromPolicy() throws CMException {
        String value = getSingleValued().getValue();
        if (value == null) {
            value = this.defaultValue;
        }
        this.oTextInput.setText(value);
    }

    @Override
    public String getValue() {
        return oTextInput.getValue();
    }

    @Override
    public OWidget getChildWidget() {
        return oTextInput;
    }
}
