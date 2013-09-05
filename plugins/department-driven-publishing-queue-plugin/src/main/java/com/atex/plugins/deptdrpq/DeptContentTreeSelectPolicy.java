/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.deptdrpq;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyUtil;
import com.polopoly.cm.policy.PrepareResult;
import com.polopoly.siteengine.field.SiteSelectorPolicy;

public class DeptContentTreeSelectPolicy extends SiteSelectorPolicy {

    @Override
    public PrepareResult prepareSelf() throws CMException {
        PrepareResult prepareResult = super.prepareSelf();

        ContentId[] refs = getReferences();

        boolean isAutoDept = isAutoDept();
        if (!isAutoDept && (refs == null || refs.length == 0)) {
            prepareResult.setError(true);
            prepareResult.setLocalizeMessage("cm.policy.ValueRequired");
        }

        return prepareResult;
    }

    protected boolean isAutoDept() throws CMException {
        return ((DepartmentPublishingQueuePolicy) getParentPolicy()).isAutoDepartment();
    }

    @Override
    public void preCommitSelf() throws CMException {
        super.preCommitSelf();

        if (isAutoDept()) {
            PolicyUtil.removeComponents(this);
            PolicyUtil.removeContentReferences(this);
        }
    }

}
