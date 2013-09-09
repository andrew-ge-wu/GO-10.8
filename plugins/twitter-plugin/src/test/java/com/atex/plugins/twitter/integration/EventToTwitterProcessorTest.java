/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter.integration;

import static org.mockito.Mockito.*;

import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;

import com.atex.plugins.twitter.TwittableInputPolicy;
import com.atex.plugins.twitter.TwitterAccount;
import com.polopoly.application.Application;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.EjbCmClient;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.event.CommitEvent;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

public class EventToTwitterProcessorTest {
    EventToTwitterProcessor target;

    @Mock
    private InputTemplate inputTemplate;
    @Mock
    private HashMap<String, Policy> children;
    @Mock
    private Content contentMock;

    @Mock
    private Exchange exchange;
    @Mock
    private Application application;
    @Mock
    private CmClient cmClient;
    @Mock
    private PolicyCMServer cmServer;
    @Mock
    private Message message;
    @Mock
    private CommitEvent body;
    @Mock
    private ContentId contentId;
    @Mock
    private Policy contentPolicy;
    @Mock
    private ContentPolicy content;
    @Mock
    private TwittableInputPolicy childPolicy;
    @Mock
    TwitterAccount twitterAccount;
    @Mock
    ContentId twitterAccountContentId;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = spy(new EventToTwitterProcessor());
        content = new ContentPolicy(){
            @Override
            protected synchronized void initChildPolicies() throws CMException {
                this.childPolicies = children;
            }
        };
        content.init("policy", new Content[] { contentMock }, inputTemplate, content, cmServer);
    }

    @Test
    public void shouldProcess() throws Exception {
        String expected = "tweetText";
        doReturn(application).when(target).getIntegrationServerApplication();
        when(application.getApplicationComponent(EjbCmClient.DEFAULT_COMPOUND_NAME))
            .thenReturn(cmClient);
        when(cmClient.getPolicyCMServer()).thenReturn(cmServer);
        when(exchange.getIn()).thenReturn(message);
        // body not instance of CommitEvent
        target.process(exchange);
        verify(exchange, new Times(1)).setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
        when(message.getBody()).thenReturn(body);
        when(body.getContentId()).thenReturn(contentId);
        when(cmServer.getPolicy(contentId)).thenReturn(contentPolicy);
        // content not instance of ContentPolicy
        target.process(exchange);
        verify(exchange, new Times(2)).setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
        when(contentPolicy.getContent()).thenReturn(content);
        // policy not instance of TwittableInputPolicy
        target.process(exchange);
        verify(exchange, new Times(3)).setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
        when(children.get(TwittableInputPolicy.POLICYNAME)).thenReturn(childPolicy);
        when(childPolicy.getTwitterAccount()).thenReturn(twitterAccount);
        when(childPolicy.isEnabled()).thenReturn(true);
        when(childPolicy.getTweetText()).thenReturn(expected);
        // successfully tweet
        target.process(exchange);
        verify(message, new Times(1)).setBody(expected);
    }

}
