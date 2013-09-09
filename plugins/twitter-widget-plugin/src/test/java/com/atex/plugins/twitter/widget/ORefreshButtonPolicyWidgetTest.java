/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter.widget;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.polopoly.cm.app.ContentSession;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.impl.OrchidContextImpl;

public class ORefreshButtonPolicyWidgetTest {
    private ORefreshButtonPolicyWidget target;

    @Mock
    private OrchidContextImpl oc;
    @Mock
    private ContentSession contentSession;

    @Before
    public void setUp() throws OrchidException {
        MockitoAnnotations.initMocks(this);
        target = spy(new ORefreshButtonPolicyWidget());

        doReturn(contentSession).when(target).getContentSession();
        doReturn("Update preview").when(target).getConfiguredLabel(oc);
    }

    @Test
    public void testInitSelfWhenLabelIsSet() throws OrchidException {
        when(contentSession.getMode()).thenReturn(ContentSession.EDIT_MODE);
        target.setLabel("Testing");
        target.initSelf(oc);

        assertEquals("Testing", target.refreshButton.getLabel());
        assertEquals(true, target.refreshButton.isEnabled());
    }

    @Test
    public void testInitSelfIfInViewMode() throws OrchidException {
        when(contentSession.getMode()).thenReturn(ContentSession.VIEW_MODE);
        target.initSelf(oc);

        assertEquals("Update preview", target.refreshButton.getLabel());
        assertEquals(false, target.refreshButton.isEnabled());
    }
}
