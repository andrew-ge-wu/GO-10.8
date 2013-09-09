/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter;

import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyUtil;
import com.polopoly.cm.policy.PrepareResult;

/**
 * 
 * Number Max Value Policy
 *
 */
public class NumberMaxValuePolicy extends SingleValuePolicy {
    private static final String MAX_SIZE = "maxSize";
    private static final String INT = "int";

    /**
     * Get number max value from input template
     * @return Return the max value as String
     */
    public String getMaxSize() {
        return PolicyUtil.getParameter(MAX_SIZE, INT, this);
    }

    /**
     * Override to implement validate max value in this life-cycle phase.
     */
    public PrepareResult prepareSelf() throws CMException {
        PrepareResult prepareResult = new PrepareResult(PolicyUtil.getLabelOrName(this));

        if ((PolicyUtil.isRequired(this)) && ((getValue() == null) || (getValue().length() == 0))) {
            prepareResult.setError(true);
            prepareResult.setLocalizeMessage("cm.policy.ValueRequired");
            return prepareResult;
        }

        if ((getValue() == null) || (getValue().length() == 0)) {
            return prepareResult;
        }

        try {
            int value = Integer.parseInt(getValue());
            int maxSize = Integer.parseInt(getMaxSize());

            if (value > maxSize) {
                throw new CMException ("com.atex.plugins.twitter.maxvalue");
            }
        } catch (NumberFormatException e) {
            prepareResult.setError(true);
            prepareResult.setLocalizeMessage("com.atex.plugins.twitter.integer");
            return prepareResult;
        } catch (CMException e) {
            prepareResult.setError(true);
            prepareResult.setLocalizeMessage(e.getLocalizedMessage());
            return prepareResult;
        }
        return prepareResult;
    }
}
