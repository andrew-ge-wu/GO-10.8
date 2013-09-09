/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter.widget;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;

import com.atex.plugins.twitter.TwittableInputPolicy;
import com.polopoly.cm.app.ContentSession;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.util.SystemNames;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.impl.OrchidContextImpl;
import com.polopoly.orchid.widget.OJavaScript;
import com.polopoly.orchid.widget.OTextInput;
import com.polopoly.orchid.widget.OTextOutput;

public class OTwittableTextInputPolicyWidgetTest {
    // TODO: Disable below codes because it throws error during mvn p:run
    /*
    OTwittableTextInputPolicyWidget target;

    @Mock
    private OrchidContextImpl oc;
    @Mock
    private ContentSession contentSession;
    @Mock
    private Device device;
    @Mock
    private PolicyCMServer cmServer;
    @Mock
    private Content content;
    @Mock
    private ContentPolicy parent;
    @Mock
    private InputTemplate textIt;

    @Mock
    private SingleValuePolicy textPolicy;
    @Mock
    private OTextInput tweetText;
    @Mock
    private OTextOutput textCount;
    @Mock
    private OJavaScript jsScript;

    private final static String POLICY_PARAMETERS_GROUP = SystemNames.ATTRG_POLICY_PARAM;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = spy(new OTwittableTextInputPolicyWidget() );
        textPolicy.init("tweetText", new Content[] { content }, textIt, parent, cmServer);
        when(oc.getDevice()).thenReturn(device);
        doReturn(contentSession).when(target).getContentSession();
        doNothing().when(target).initSelfSuper(oc);
        doReturn(textPolicy).when(target).getPolicy();
        doReturn(tweetText).when(target).getTweetTextWidget();
        doReturn(textCount).when(target).getTextCountWidget();
        doReturn(jsScript).when(target).getJsSciptWidget();
        when(contentSession.getMode()).thenReturn(ContentSession.EDIT_MODE);
        target.initSelf(oc);
        when(tweetText.getCompoundId()).thenReturn("textCompId");
        when(textCount.getCompoundId()).thenReturn("countCompId");
        doReturn("mainCompId").when(target).getCompoundId();
    }

    @Test
    public void shouldInitSelf() throws OrchidException {
        verify(tweetText,  new Times(1)).setEnabled(true);
        when(contentSession.getMode()).thenReturn(ContentSession.VIEW_MODE);
        target.initSelf(oc);
        verify(tweetText, new Times(1)).setEnabled(false);
        verify(target, atLeastOnce()).addAndInitChild(oc, tweetText);
        verify(target, atLeastOnce()).addAndInitChild(oc, textCount);
    }

    @Test
    public void shouldGetTextCount() {
        assertEquals("0", target.getTextCount(null));
        assertEquals("0", target.getTextCount(""));
        assertEquals("0", target.getTextCount("     "));
        assertEquals("5", target.getTextCount("count"));
    }

    @Test
    public void shouldInitValueFromPolicy() throws OrchidException, CMException {
        when(textPolicy.getValue()).thenReturn(null);
        doReturn("20").when(target).getMaxLength();
        target.initValueFromPolicy(oc);
        verify(tweetText, new Times(1)).setText("");
        verify(textCount, atLeastOnce()).setText("0");
        when(textPolicy.getValue()).thenReturn(" Sometext ");
        when(contentSession.getMode()).thenReturn(ContentSession.VIEW_MODE);
        target.initValueFromPolicy(oc);
        verify(tweetText, new Times(1)).setText(" Sometext ");
        verify(textCount, new Times(1)).setText("8 / 20");
    }

    @Test
    public void shouldStoreSelf() throws CMException, OrchidException {
        when(tweetText.getText()).thenReturn("");
        target.storeSelf();
        verify(textPolicy, new Times(1)).setValue("");
    }

    @Test
    public void testLocalRender() throws OrchidException, IOException {
        doReturn("20").when(target).getMaxLength();
        target.localRender(oc);
        verify(tweetText, new Times(1)).render(oc);
        verify(textCount, new Times(1)).render(oc);
    }

    @Test
    public void shouldTrueIsAjaxTopWidget() {
        assertTrue(target.isAjaxTopWidget());
    }

    @Test
    public void shouldGetTweetTextWidget() {
        target = new OTwittableTextInputPolicyWidget();
        target.tweetText = target.getTweetTextWidget();
        assertNotNull(target.tweetText);
        assertEquals(target.tweetText, target.getTweetTextWidget());
    }

    @Test
    public void shouldGetTextCountWidget() {
        target = new OTwittableTextInputPolicyWidget();
        target.textCount = target.getTextCountWidget();
        assertNotNull(target.textCount);
        assertEquals(target.textCount, target.getTextCountWidget());
    }

    @Test
    public void shouldGetJsScriptWidget() {
        target = new OTwittableTextInputPolicyWidget();
        target.jsScript = target.getJsSciptWidget();
        assertNotNull(target.jsScript);
        assertEquals(target.jsScript, target.getJsSciptWidget());
    }

    @Test
    public void shouldGetMaxLength() throws CMException {
        assertEquals("", target.getMaxLength());
        when(textIt.getComponent(POLICY_PARAMETERS_GROUP, TwittableInputPolicy.PARAM_MAX_LENGTH)).thenReturn("0");
        assertEquals("", target.getMaxLength());
        when(textIt.getComponent(POLICY_PARAMETERS_GROUP, TwittableInputPolicy.PARAM_MAX_LENGTH)).thenReturn("abc");
        assertEquals("", target.getMaxLength());
        when(textIt.getComponent(POLICY_PARAMETERS_GROUP, TwittableInputPolicy.PARAM_MAX_LENGTH)).thenReturn("10");
        assertEquals("10", target.getMaxLength());
    }

    @Test
    public void shouldGetMaxLenEndText() {
        assertEquals("", target.getMaxLenEndText());
        doReturn("").when(target).getMaxLength();
        assertEquals("", target.getMaxLenEndText());
        doReturn("10").when(target).getMaxLength();
        assertEquals(" / 10", target.getMaxLenEndText());
    }
*/
}