/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.policy.CheckboxPolicy;
import com.polopoly.cm.app.policy.SingleReference;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.policy.PolicyUtil;
import com.polopoly.cm.policy.PrepareResult;
import com.polopoly.common.logging.LogUtil;
import com.polopoly.util.StringUtil;

/*
 * @since 2.0.3
 */
public class TwittableInputPolicy extends ContentPolicy {

    private final static String SEPARATOR = " - ";
    private final static String ELLIPSIS = "...";
    protected final static String LEAD_POLICYNAME = "lead";
    protected final static String VALUE ="value";
    public final static String POLICYNAME = "tweeterFields";
    public final static String DECISION_POLICYNAME = "tweetitnow";
    public final static String TEXT_POLICYNAME = "tweetText";
    public final static String PARAM_MAX_LENGTH = "maxlength";
    public final static String ACCOUNT = "account";

    private final static Logger LOG = LogUtil.getLog(TwittableInputPolicy.class);

    public CheckboxPolicy getDecisionField() throws CMException {
        return (CheckboxPolicy) getChildPolicy(DECISION_POLICYNAME);
    }

    public SingleValuePolicy getTextField() throws CMException {
        return (SingleValuePolicy) getChildPolicy(TEXT_POLICYNAME);
    }

    public boolean isEnabled() {
        try {
            return getDecisionField().getChecked();
        } catch (CMException e) {
            LOG.log(Level.INFO, "Failed to get decision value " + e);
        }
        return false;
    }

    public void setDecision(boolean value) {
        try {
            getDecisionField().setChecked(value);
        } catch (CMException e) {
            LOG.log(Level.INFO, "Failed to set decision value " + e);
        }
    }

    public String getTextValue() {
        try {
            SingleValuePolicy textPolicy = getTextField();
            String text = "";
            if (textPolicy!=null) {
                text = textPolicy.getValue();
            }
            if (text!=null) {
                return text.trim();
            }
        } catch (CMException e) {
            LOG.log(Level.INFO, "Failed to get text value " + e);
        }
        return "";
    }

    public void setTextValue(String value) {
        try {
            if (isEnabled()) {
                getTextField().setValue(value.trim());
            }
        } catch (CMException e) {
            LOG.log(Level.INFO, "Failed to set text value " + e);
        }
    }

    private ContentId getTwitterAccountContentId() throws CMException {
        return ((SingleReference) getChildPolicy(ACCOUNT)).getReference();
    }

    public TwitterAccount getTwitterAccount() throws CMException {
        return (TwitterAccount) getCMServer().getPolicy(getTwitterAccountContentId());
    }

    @Override
    public PrepareResult prepareSelf() throws CMException {
        PrepareResult prepareResult = getPrepareResult();
        String maxTweetLength = PolicyUtil.getParameter(PARAM_MAX_LENGTH, getTextField().getInputTemplate());
        int maxLength = Integer.valueOf(maxTweetLength);
        if (getTextValue().length() > maxLength) {
            prepareResult.setError(true);
            prepareResult.setLocalizeMessage("com.atex.plugins.twitter.twittable.exceedMaxLen");
            return prepareResult;
        }
        return super.prepareSelf();
    }

    protected PrepareResult getPrepareResult() throws CMException {
        return new PrepareResult(PolicyUtil.getLabelOrName(getChildPolicy(TEXT_POLICYNAME)));
    }

    public String getTweetText() {
        ContentPolicy article = (ContentPolicy) getParentPolicy();
        StringBuilder tweet = new StringBuilder();
        try {
            String tweetText = "";
                tweetText = getTextValue();
            if (StringUtil.isEmpty(tweetText)) {
                String articleTitle = article.getName(); 
                String articleSummary = article.getComponent(LEAD_POLICYNAME, VALUE);
                tweet.append(articleTitle);
                if (!StringUtil.isEmpty(articleSummary)) {
                    tweet.append(SEPARATOR);
                    tweet.append(articleSummary);
                }
            } else {
                tweet.append(tweetText);
            }
            if (tweet.length() > 110) {
                tweet.substring(0, 107);
                tweet.append(ELLIPSIS);
            }
            String url = getUrlForTwitter();
            if (!StringUtil.isEmpty(url)) {
                tweet.append(" ");
                tweet.append(url);
            }
        } catch (CMException e) {
            LOG.log(Level.INFO, e.getMessage());
        }
        return tweet.toString();
    }

    public String getUrlForTwitter() {
        URLBuilder urlBuilder = getUrlBuilder();
        try {
            return urlBuilder.getFullUrl();
        } catch (CMException e) {
            LOG.log(Level.INFO, "Fail to get full url, site alias needed " + e);
        }
        return "";
    }

    protected URLBuilder getUrlBuilder() {
        ContentId cid = getContentId();
        PolicyCMServer cmServer = getCMServer();
        return new URLBuilder(cid, cmServer);
    }
}
