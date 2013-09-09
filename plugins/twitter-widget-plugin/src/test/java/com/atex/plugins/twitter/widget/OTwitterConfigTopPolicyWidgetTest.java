package com.atex.plugins.twitter.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.polopoly.cm.app.ContentSession;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.impl.OrchidContextImpl;
import com.polopoly.orchid.widget.OJavaScript;

public class OTwitterConfigTopPolicyWidgetTest {
    private OTwitterConfigTopPolicyWidget target;

    @Mock
    private OrchidContextImpl oc;
    @Mock
    private ContentSession contentSession;
    @Mock
    private Device device;
    @Mock
    private OJavaScript miniColorsScript;
    @Mock
    private OJavaScript twitterConfigScript;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target = spy(new OTwitterConfigTopPolicyWidget());

        when(oc.getDevice()).thenReturn(device);
        doReturn(contentSession).when(target).getContentSession();
    }

    @Test
    public void testInitSelf() throws OrchidException {
        target.initSelf(oc);
        assertNotNull(target.miniColorsScript);
        assertNotNull(target.twitterConfigScript);
    }

    @Test
    public void testLocalRenderIfJsAndCssPathAreNull() throws OrchidException, IOException {
        target.miniColorsScript = miniColorsScript;
        target.twitterConfigScript = twitterConfigScript;

        doReturn("abc").when(target).lookupContentFile(OTwitterConfigTopPolicyWidget.FILES,
                OTwitterConfigTopPolicyWidget.MINICOLORS_CSS_FILE_PATH, oc);
        doReturn("def").when(target).lookupContentFile(OTwitterConfigTopPolicyWidget.FILES,
                OTwitterConfigTopPolicyWidget.MINICOLORS_JS_FILE_PATH, oc);
        doReturn("gfi").when(target).lookupContentFile(OTwitterConfigTopPolicyWidget.FILES,
                OTwitterConfigTopPolicyWidget.TWITTER_CONFIG_JS_FILE_PATH, oc);

        target.localRender(oc);
        assertEquals("abc", target.miniColorsCssPath);
        assertEquals("def", target.miniColorsJsPath);
        assertEquals("gfi", target.twitterConfigWidgetJsPath);
    }
    
    @Test
    public void testLocalRenderIfJsAndCssPathAreNotNull() throws OrchidException, IOException {
        target.miniColorsScript = miniColorsScript;
        target.twitterConfigScript = twitterConfigScript;
        
        target.miniColorsCssPath = "123";
        target.miniColorsJsPath = "456";
        target.twitterConfigWidgetJsPath = "789";

        doReturn("abc").when(target).lookupContentFile(OTwitterConfigTopPolicyWidget.FILES,
                OTwitterConfigTopPolicyWidget.MINICOLORS_CSS_FILE_PATH, oc);
        doReturn("def").when(target).lookupContentFile(OTwitterConfigTopPolicyWidget.FILES,
                OTwitterConfigTopPolicyWidget.MINICOLORS_JS_FILE_PATH, oc);
        doReturn("gfi").when(target).lookupContentFile(OTwitterConfigTopPolicyWidget.FILES,
                OTwitterConfigTopPolicyWidget.TWITTER_CONFIG_JS_FILE_PATH, oc);

        target.localRender(oc);
        assertEquals("123", target.miniColorsCssPath);
        assertEquals("456", target.miniColorsJsPath);
        assertEquals("789", target.twitterConfigWidgetJsPath);
    }
}
