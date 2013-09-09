/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;

import com.polopoly.cm.app.policy.CheckboxPolicy;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.policy.PrepareResult;
import com.polopoly.cm.util.SystemNames;

public class TwittableInputPolicyTest {
    TwittableInputPolicy target;

    @Mock
    private PolicyCMServer cmServer;
    @Mock
    private Content content;
    @Mock
    private InputTemplate inputTemplate;
    @Mock
    private ContentPolicy parent;

    @Mock
    private CheckboxPolicy decisionField;
    @Mock
    private SingleValuePolicy textField;
    @Mock
    private InputTemplate textInputTemplate;
    @Mock
    private PrepareResult prepareResult;
    @Mock
    private URLBuilder urlBuilder;

    private final static String POLICY_PARAMETERS_GROUP = SystemNames.ATTRG_POLICY_PARAM;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target =  spy(new TwittableInputPolicy());
        target.init("policy", new Content[] { content }, inputTemplate, parent, cmServer);
        textField.init("tweetText", new Content[] { content }, textInputTemplate, parent, cmServer);
        doReturn(decisionField).when(target).getChildPolicy(TwittableInputPolicy.DECISION_POLICYNAME);
        doReturn(textField).when(target).getChildPolicy(TwittableInputPolicy.TEXT_POLICYNAME);
    }

    @Test
    public void shouldGetDecisionField() throws CMException {
        assertEquals(decisionField, target.getDecisionField());
    }

    @Test
    public void shouldGetTextField() throws CMException {
        assertEquals(textField, target.getTextField());
    }

    @Test
    public void shouldBeEnabled() throws CMException {
        when(decisionField.getChecked()).thenReturn(true);
        assertTrue(target.isEnabled());
        when(decisionField.getChecked()).thenThrow(new CMException("Unit test CME"));
        assertFalse(target.isEnabled());
    }

    @Test
    public void shouldSetDecision() throws CMException {
        doReturn(decisionField).when(target).getChildPolicy(TwittableInputPolicy.DECISION_POLICYNAME);
        target.setDecision(true);
        verify(decisionField, new Times(1)).setChecked(true);
        doThrow(new CMException("Unit test CME")).when(target).getDecisionField();
        target.setDecision(false);
        verify(decisionField, new Times(0)).setChecked(false);
    }

    @Test
    public void shouldGetTextValue() throws CMException {
        String expected = "value";
        when(textField.getValue()).thenReturn(expected);
        assertEquals(expected, target.getTextValue());
        when(textField.getValue()).thenThrow(new CMException("Unit test CME"));
        assertEquals("", target.getTextValue());
    }

    @Test
    public void shouldSetTextValue() throws CMException {
        String expected = "value";
        target.setTextValue(expected);
        verify(textField, new Times(0)).setValue(expected);
        doReturn(true).when(target).isEnabled();
        target.setTextValue(expected);
        verify(textField, new Times(1)).setValue(expected);
        doThrow(new CMException("Unit test CME")).when(target).getTextField();
        target.setTextValue(expected);
        verify(textField, new Times(1)).setValue(expected);
    }

    @Test
    public void shouldPrepareSelf() throws CMException {
        String shortTweet = "tweet";
        String longTweet = "Tweet Long. Tweet Long. ";
        doReturn(prepareResult).when(target).getPrepareResult();
        when(textInputTemplate.getComponent(POLICY_PARAMETERS_GROUP, TwittableInputPolicy.PARAM_MAX_LENGTH)).thenReturn("10");
        doReturn(shortTweet).when(target).getTextValue();
        target.prepareSelf();
        verify(prepareResult, new Times(0)).setError(true);
        verify(prepareResult, new Times(0)).setLocalizeMessage("com.atex.plugins.twitter.twittable.exceedMaxLen");
        doReturn(longTweet).when(target).getTextValue();
        target.prepareSelf();
        verify(prepareResult, new Times(1)).setError(true);
        verify(prepareResult, new Times(1)).setLocalizeMessage("com.atex.plugins.twitter.twittable.exceedMaxLen");
    }

    @Test
    public void shouldGetTweetText() throws CMException {
        String shortTweet = "Some short tweet content";
        String longTweet = "Some long tweet content. Some long tweet content. Some long tweet content. Some long tweet content. Some long tweet content. ";
        String longTweetExpected = "Some long tweet content. Some long tweet content. Some long tweet content. Some long tweet content. Some long tweet content. ...";
        String expectedUrl = "someurl";
        doReturn(shortTweet).when(target).getTextValue();
        assertEquals(shortTweet, target.getTweetText());
        doReturn(longTweet).when(target).getTextValue();
        assertEquals(longTweetExpected, target.getTweetText());
        doReturn("").when(target).getTextValue();
        when(parent.getName()).thenReturn("name");
        assertEquals("name", target.getTweetText());
        when(parent.getComponent(TwittableInputPolicy.LEAD_POLICYNAME, TwittableInputPolicy.VALUE)).thenReturn("lead");
        assertEquals("name - lead", target.getTweetText());
        doReturn(expectedUrl).when(target).getUrlForTwitter();
        assertEquals("name - lead " + expectedUrl, target.getTweetText());
    }

    @Test
    public void shouldGetUrlForTwitter() throws CMException {
        String expected = "urlfortwitter";
        doReturn(urlBuilder).when(target).getUrlBuilder();
        when(urlBuilder.getFullUrl()).thenReturn(expected);
        assertEquals(expected, target.getUrlForTwitter());
        when(urlBuilder.getFullUrl()).thenThrow(new CMException("Unit test CME"));
        assertEquals("", target.getUrlForTwitter());
    }

    @Test
    public void shouldGetUrlBuilder() {
        assertTrue(target.getUrlBuilder() instanceof URLBuilder);
    }
}
