/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter.integration;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.atex.plugins.twitter.TwittableInputPolicy;
import com.atex.plugins.twitter.TwitterAccount;
import com.polopoly.application.Application;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.EjbCmClient;
import com.polopoly.cm.event.CommitEvent;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.common.logging.LogUtil;
import com.polopoly.integration.IntegrationServerApplication;

/*
 * @since 2.0.3
 */
public class EventToTwitterProcessor implements Processor {

    private final static Logger LOG = LogUtil.getLog(EventToTwitterProcessor.class);

    public void process(Exchange exchange) throws Exception {
        Boolean tweetItNow = false;
        String tweet = "";
        CmClient cmClient = (CmClient) getIntegrationServerApplication()
                .getApplicationComponent(EjbCmClient.DEFAULT_COMPOUND_NAME);
        PolicyCMServer cmServer = cmClient.getPolicyCMServer();
        Object body = exchange.getIn().getBody();
        if (body instanceof CommitEvent) {
            CommitEvent commitEvent = (CommitEvent) body;
            ContentId contentId = commitEvent.getContentId();
            Content content = cmServer.getPolicy(contentId).getContent();
            if (!(content instanceof ContentPolicy)) {
                exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
                return;
            }

            ContentPolicy article = (ContentPolicy) content;
            // check if the article is twittable
            Policy policy = article.getChildPolicy(TwittableInputPolicy.POLICYNAME);
            if (policy instanceof TwittableInputPolicy) {
                tweetItNow = ((TwittableInputPolicy) policy).isEnabled();
            }
            if (!tweetItNow) {
                exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
                return;
            }
            tweet = ((TwittableInputPolicy) policy).getTweetText();
            TwitterAccount account = ((TwittableInputPolicy) policy).getTwitterAccount();
            exchange.getIn().setHeader("recipients", account.getCamelTwitterURI());
            // sent tweet
            exchange.getIn().setBody(tweet);
            LOG.log(Level.INFO, 
                    " Content : " + article.getContentId() 
                    + " tweeted with text \"" + tweet + "\"");
        } else {
            exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
        }
    }

    protected Application getIntegrationServerApplication() {
        return IntegrationServerApplication.getPolopolyApplication();
    }
}
