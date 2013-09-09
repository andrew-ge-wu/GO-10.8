/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter.widget;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.plugins.twitter.TwittableInputPolicy;
import com.atex.plugins.twitter.TwitterAccount;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.orchid.widget.OContentSingleSelect;
import com.polopoly.cm.app.policy.SingleReference;
import com.polopoly.cm.app.widget.OContentSingleSelectPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.policy.PrepareResult;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.util.WidgetUtil;
import com.polopoly.util.LocaleUtil;

/*
 * @since 2.0.3
 */
public class OTwitterAccountSelectWidget extends OContentSingleSelectPolicyWidget {

    private static final long serialVersionUID = 4500234348987217926L;
    private static final Logger LOGGER = Logger.getLogger(OTwitterAccountSelectWidget.class.getName());
    public static final String ACCOUNT_REQUIRED = "com.atex.plugins.twitter.select.account.required";
    public static final String MUST_BE_TWITTER_ACCOUNT = "com.atex.plugins.twitter.select.account.only";
    public static final String MUST_BE_TWITTABLE_INPUT_POLICY = "com.atex.plugins.twitter.select.wrong.policy";

    @Override
    public void initSelf(OrchidContext oc) throws OrchidException {
        super.initSelf(oc);
        selectDefaultAccount(oc);
    }

    @Override
    public PrepareResult validateSelf() throws CMException {
        PrepareResult result = getResult();
        Policy policy = getParentPolicyWidget().getPolicy();
        if(policy instanceof TwittableInputPolicy && ((TwittableInputPolicy)policy).isEnabled()) {
            SingleReference reference = (SingleReference) getPolicy();
            if(reference.getReference() == null) {
                result.setError(true);
                result.setLocalizeMessage(ACCOUNT_REQUIRED);
                handleError(getOrchidContext(), getString(ACCOUNT_REQUIRED));
            } else if(!isInstanceOfTwitterAccount(reference.getReference())) {
                result.setError(true);
                result.setLocalizeMessage(MUST_BE_TWITTER_ACCOUNT);
                handleError(getOrchidContext(), getString(MUST_BE_TWITTER_ACCOUNT));
            } else {
                setDefaultAccount(reference.getReference());
            }
        } else if(!(policy instanceof TwittableInputPolicy)){
            result.setError(true);
            result.setLocalizeMessage(MUST_BE_TWITTABLE_INPUT_POLICY);
            handleError(getOrchidContext(), getString(MUST_BE_TWITTABLE_INPUT_POLICY));
        }
        return result;
    }

    void selectDefaultAccount(OrchidContext oc) {
        if(contentSelect.getSelectedContentId() == null) {
            try {
                PolicyCMServer cmServer = getContentSession().getPolicyCMServer();
                String userId = cmServer.getCurrentCaller().getUserId().getPrincipalIdString();
                ContentId userContentId = new ExternalContentId(userId);
                Policy user = cmServer.getPolicy(userContentId);
                ContentId twitterAccount = user.getContentReference("twitterAccount");
                if (twitterAccount != null
                        && cmServer.getPolicy(twitterAccount) instanceof TwitterAccount) {
                    contentSelect.setSelectedContentId(twitterAccount, oc);
                }
            } catch (CMException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    void setDefaultAccount(ContentId contentId) {
        try {
            PolicyCMServer cmServer = getContentSession().getPolicyCMServer();
            String userId = cmServer.getCurrentCaller().getUserId().getPrincipalIdString();
            VersionedContentId userContentId = new ExternalContentId(userId);
            Policy user = (Policy) cmServer.createContentVersion(userContentId);
            user.setContentReference("twitterAccount",contentId);
            cmServer.commitContent(user);
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    PrepareResult getResult() throws CMException {
        return super.validateSelf();
    }

    String getString(String key) {
        return LocaleUtil.format(key, getOrchidContext().getMessageBundle());
    }

    OrchidContext getOrchidContext() {
        return WidgetUtil.getOrchidContext();
    }

    boolean isInstanceOfTwitterAccount(ContentId contentId) throws CMException {
        return getContentSession().getPolicyCMServer().getPolicy(contentId) instanceof TwitterAccount;
    }

    void setOContentSingleSelect(OContentSingleSelect select) {
        this.contentSelect = select;
    }
}
