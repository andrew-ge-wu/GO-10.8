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

import com.atex.plugins.baseline.url.UrlResolver;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.ContentSession;
import com.polopoly.cm.app.policy.CheckboxPolicy;
import com.polopoly.cm.app.widget.OPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.impl.OrchidContextImpl;
import com.polopoly.orchid.widget.OCheckbox;
import com.polopoly.orchid.widget.OJavaScript;

public class OTwittableCheckboxPolicyWidgetTest {
    OTwittableCheckboxPolicyWidget target;

    @Mock
    private OrchidContextImpl oc;
    @Mock
    private ContentSession contentSession;
    @Mock
    private Device device;

    @Mock
    private OCheckbox option;
    @Mock
    private OJavaScript twittableTextInputScript;
    @Mock
    private CheckboxPolicy optionPolicy;
    @Mock
    private ExternalContentId extId;
    @Mock
    private UrlResolver urlResolver;
    @Mock
    private OPolicyWidget parentWidget;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = spy(new OTwittableCheckboxPolicyWidget());
        when(oc.getDevice()).thenReturn(device);
        doReturn(contentSession).when(target).getContentSession();
        doNothing().when(target).initSelfSuper(oc);
        doReturn(optionPolicy).when(target).getPolicy();
        doReturn(option).when(target).getOptionWidget();
        doReturn(twittableTextInputScript).when(target).getTwittableTextInputScript();
        target.initSelf(oc);
    }

    @Test
    public void shouldInitSelf() throws OrchidException {
        verify(option, new Times(1)).setEnabled(false);
        when(contentSession.getMode()).thenReturn(ContentSession.EDIT_MODE);
        target.initSelf(oc);
        verify(option, new Times(1)).setEnabled(true);
        verify(target, atLeastOnce()).addAndInitChild(oc, option);
        verify(target, atLeastOnce()).addAndInitChild(oc, twittableTextInputScript);
    }

    @Test
    public void shouldInitValueFromPolicy() throws CMException, OrchidException {
        when(optionPolicy.getChecked()).thenReturn(true);
        target.initValueFromPolicy(oc);
        verify(option, new Times(1)).setChecked(true);
    }

    @Test
    public void shouldStoreSelf() throws CMException, OrchidException {
        when(option.isChecked()).thenReturn(true);
        target.storeSelf();
        verify(optionPolicy, new Times(1)).setChecked(true);
    }

    @Test
    public void shouldLookupContentFile() {
        String expected = "fileUrl";
        String externalId = "extId";
        doThrow(new NullPointerException("Unit test exception")).when(target).getUrlResolver(oc);
        assertNull(target.lookupContentFile(externalId, "fileName", oc));
        doReturn(urlResolver).when(target).getUrlResolver(oc);
        doReturn(extId).when(target).getExternalContentId(externalId);
        when(urlResolver.getFileUrl(extId, "fileName")).thenReturn(expected);
        assertEquals(expected, target.lookupContentFile(externalId, "fileName", oc));
    }

    @Test
    public void testLocalRender() throws OrchidException, IOException {
        doReturn("contentFile").when(target).lookupContentFile(OTwittableCheckboxPolicyWidget.FILES, "script/twittableTextInputWidget.js", oc);
        target.localRender(oc);
        verify(twittableTextInputScript, new Times(1)).setSrc("contentFile");
        verify(twittableTextInputScript, new Times(1)).render(oc);
        verify(option, new Times(1)).render(oc);
    }

    @Test
    public void shouldTrueIsAjaxTopWidget() {
        assertTrue(target.isAjaxTopWidget());
    }

    @Test
    public void shouldGetParentCompoundId() throws OrchidException {
        String expected = "work_1234";
        doReturn(parentWidget).when(target).getParentPolicyWidget();
        when(parentWidget.getCompoundId()).thenReturn(expected);
        assertEquals(expected, target.getParentCompoundId());
        when(parentWidget.getCompoundId()).thenThrow(new OrchidException("Unit test exception"));
        assertEquals("", target.getParentCompoundId());
    }

    @Test
    public void shoudGetJSScriptDependencies() {
        assertNull(target.getJSScriptDependencies()[0]);
    }

    @Test
    public void shouldGetFriendlyName() throws OrchidException {
        String expected = "friendlyName";
        doReturn(expected).when(target).getName();
        assertEquals(expected, target.getFriendlyName());
    }

    @Test
    public void shouldGetInitParams() throws OrchidException {
        String expected = "parentCompId";
        doReturn(expected).when(target).getParentCompoundId();
        assertEquals("'" + expected + "'", target.getInitParams()[0]);
    }

    @Test
    public void shouldGetJSWidgetClassName() throws OrchidException {
        assertEquals("twittableJsWidget", target.getJSWidgetClassName());
    }

    @Test
    public void shouldGetOptionWidget() {
        target = new OTwittableCheckboxPolicyWidget();
        target.option = target.getOptionWidget();
        assertNotNull(target.option);
        assertEquals(target.option, target.getOptionWidget());
    }

    @Test
    public void shouldGetTwittableTextInputScript() {
        target = new OTwittableCheckboxPolicyWidget();
        target.twittableTextInputScript = target.getTwittableTextInputScript();
        assertNotNull(target.twittableTextInputScript);
        assertEquals(target.twittableTextInputScript, target.getTwittableTextInputScript());
    }
}
