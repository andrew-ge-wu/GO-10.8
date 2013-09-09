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
import com.polopoly.cm.app.widget.OAbstractPolicyWidget;
import com.polopoly.cm.policy.PolicyUtil;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OSubmitButton;
import com.polopoly.util.LocaleUtil;

/**
 * 
 * Refresh Button Policy Widget
 *
 */
public class ORefreshButtonPolicyWidget extends OAbstractPolicyWidget implements Viewer, Editor {
    private static final long serialVersionUID = 1L;

    private String label;
    protected OSubmitButton refreshButton;

    @Override
    public void initSelf(OrchidContext oc) throws OrchidException {
        refreshButton = new OSubmitButton();

        if (label == null) {
            label = getConfiguredLabel(oc);
        }
        refreshButton.setLabel(label);
        if (getContentSession().getMode() == ContentSession.VIEW_MODE) {
            refreshButton.setEnabled(false);
        }
        addAndInitChild(oc, refreshButton);
    }

    /**
     * Get label from configured resource
     * @param oc OrchidContext
     * @return label
     */
    protected String getConfiguredLabel(OrchidContext oc) {
        String configuredLabel = PolicyUtil.getLabel(getPolicy());
        return LocaleUtil.format(configuredLabel, oc.getMessageBundle());
    }

    /**
     * Set button label
     * @param label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public void localRender(OrchidContext oc) throws OrchidException, IOException {
        refreshButton.render(oc);
    }
}
