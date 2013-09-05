/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.deptdrpq.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.atex.plugins.baseline.url.OrchidUrlResolver;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.ContentSession;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.impl.OrchidContextImpl;
import com.polopoly.orchid.widget.OJavaScript;

public class OAutoDeptCheckboxPolicyWidgetTest {
    private OAutoDeptRadioButtonGroupPolicyWidget target;

    @Mock
    private OrchidContextImpl oc;
    @Mock
    private ContentSession contentSession;
    @Mock
    private OJavaScript deptPQScript;
    @Mock
    private OrchidUrlResolver urlResolver;

    @Before
    public void setUp() throws OrchidException {
        MockitoAnnotations.initMocks(this);
        target = spy(new OAutoDeptRadioButtonGroupPolicyWidget());

        doReturn(contentSession).when(target).getContentSession();
    }

    @Test
    public void testLocalRenderIfJsPathIsNull() throws OrchidException, IOException {
        String expected = "/abc/jspath";

        when(urlResolver.getFileUrl(any(ContentId.class), anyString())).thenReturn(expected);

        target.urlResolver = urlResolver;
        target.deptPQScript = deptPQScript;

        target.localRender(oc);

        assertEquals(expected, target.deptPQWidgetJsPath);
        verify(deptPQScript).setSrc(expected);
    }

    @Test
    public void testLocalRenderIfJsPathIsNotNull() throws OrchidException, IOException {
        String expected = "/abc/jspath";

        target.deptPQWidgetJsPath = expected;
        target.urlResolver = urlResolver;
        target.deptPQScript = deptPQScript;

        target.localRender(oc);

        assertEquals(expected, target.deptPQWidgetJsPath);
        verify(deptPQScript).setSrc(expected);
    }

    @Test
    public void testLookupContentFile() {
        target.lookupContentFile(OAutoDeptRadioButtonGroupPolicyWidget.FILES, "script/deptPQWidget.js", oc);

        assertNotNull(target.urlResolver);
    }

    @Test
    public void testLookupContentFileIfException() {
        target.urlResolver = urlResolver;
        doThrow(new CMRuntimeException("Unable to translate path")).when(urlResolver).getFileUrl(any(ContentId.class),
                anyString());

        String result = target.lookupContentFile(OAutoDeptRadioButtonGroupPolicyWidget.FILES, "script/deptPQWidget.js", oc);

        assertNull(result);
    }
}
