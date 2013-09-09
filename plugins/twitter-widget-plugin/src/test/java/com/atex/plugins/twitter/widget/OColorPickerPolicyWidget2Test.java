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

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.polopoly.cm.app.ContentSession;
import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.client.CMException;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.impl.OrchidContextImpl;

public class OColorPickerPolicyWidget2Test {
    private OColorPickerPolicyWidget target;

    @Mock
    private OrchidContextImpl oc;
    @Mock
    private ContentSession contentSession;

    @Before
    public void setUp() throws OrchidException {
        MockitoAnnotations.initMocks(this);
        target = spy(new OColorPickerPolicyWidget() {
            private static final long serialVersionUID = 1L;

            @Override
            protected String getDefaultValue() {
                return "#ffffff";
            }

            @Override
            protected SingleValued getSingleValued() {
                return new SingleValued() {
                    public void setValue(String paramString) throws CMException {
                    }

                    public String getValue() throws CMException {
                        return null;
                    }
                };
            }
        });

        doReturn(contentSession).when(target).getContentSession();
    }

    @Test
    public void testInitSelfAndInitValueWhenValueIsNotSet() throws OrchidException, CMException, IOException {
        when(contentSession.getMode()).thenReturn(ContentSession.VIEW_MODE);
        target.initSelf(oc);
        target.initValueFromPolicy();

        assertEquals("#ffffff", target.getValue());
        assertEquals(target.oTextInput, target.getChildWidget());
    }
}
