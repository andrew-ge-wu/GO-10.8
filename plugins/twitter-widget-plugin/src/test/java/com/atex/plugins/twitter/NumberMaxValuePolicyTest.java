/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.policy.PolicyUtil;
import com.polopoly.cm.policy.PrepareResult;

public class NumberMaxValuePolicyTest {

    @Mock
    private PolicyCMServer cmServer;
    @Mock
    private Content content;
    @Mock
    private InputTemplate inputTemplate;
    @Mock
    private Policy parent;
    @Mock
    private HashMap<String, Policy> children;

    private NumberMaxValuePolicy target;

    private static String MAX_VALUE = "100";
    private static final String MAX_SIZE = "maxSize";
    private static final String INT = "int";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = new NumberMaxValuePolicy() {
            @Override
            protected synchronized void initChildPolicies() throws CMException {
                this.childPolicies = children;
            }
        };
        target.init("policy", new Content[] { content }, inputTemplate, parent, cmServer);
        when(PolicyUtil.getLabelOrName(target)).thenReturn(MAX_SIZE);
    }

    @Test
    public void testGetMaxSizeNoValue() {
        assertEquals(INT, target.getMaxSize());
    }

    @Test
    public void testGetMaxSizeMaxValue() throws CMException {
        when(PolicyUtil.getParameter(MAX_SIZE, INT, target)).thenReturn(MAX_VALUE);
        assertEquals(MAX_VALUE, target.getMaxSize());
    }

    @Test
    public void testNullValue() throws CMException {
        when(target.getValue()).thenReturn(null);
        PrepareResult prepareResult = target.prepareSelf();
        assertNull(target.getValue());
        assertNotNull(prepareResult);
    }

    @Test
    public void testEmptyValue() throws CMException {
        when(target.getValue()).thenReturn("");
        PrepareResult prepareResult = target.prepareSelf();
        assertNotNull(target.getValue());
        assertEquals(0, target.getValue().length());
        assertNotNull(prepareResult);
    }

    @Test
    public void testIsRequiredAndEmptyValue() throws CMException {
        when(PolicyUtil.getParameter("required", target)).thenReturn("true");
        when(target.getValue()).thenReturn("");
        PrepareResult prepareResult = target.prepareSelf();
        assertEquals(true, PolicyUtil.isRequired(target));
        assertNotNull(target.getValue());
        assertEquals(0, target.getValue().length());
        assertTrue(prepareResult.isError());
        assertEquals("cm.policy.ValueRequired", prepareResult.getLocalizeMessage());
    }

    @Test
    public void testIsRequiredAndNullValue() throws CMException {
        when(PolicyUtil.getParameter("required", target)).thenReturn("true");
        when(target.getValue()).thenReturn(null);
        PrepareResult prepareResult = target.prepareSelf();
        assertEquals(true, PolicyUtil.isRequired(target));
        assertNull(target.getValue());
        assertTrue(prepareResult.isError());
        assertEquals("cm.policy.ValueRequired", prepareResult.getLocalizeMessage());
    }

    @Test
    public void testValueWithinRange() throws CMException {
        when(PolicyUtil.getParameter(MAX_SIZE, INT, target)).thenReturn(MAX_VALUE);
        when(PolicyUtil.getParameter("required", target)).thenReturn("true");
        when(target.getValue()).thenReturn(MAX_VALUE);
        PrepareResult prepareResult = target.prepareSelf();
        assertFalse(prepareResult.isError());
        assertNull(prepareResult.getLocalizeMessage());
    }

    @Test
    public void testOverMaxValue() throws CMException {
        when(PolicyUtil.getParameter(MAX_SIZE, INT, target)).thenReturn(MAX_VALUE);
        when(PolicyUtil.getParameter("required", target)).thenReturn("true");
        when(target.getValue()).thenReturn("1000");
        PrepareResult prepareResult = target.prepareSelf();
        assertTrue(prepareResult.isError());
        assertEquals("com.atex.plugins.twitter.maxvalue", prepareResult.getLocalizeMessage());
    }

    @Test
    public void testIllegalNumberFormatValue() throws CMException {
        when(PolicyUtil.getParameter(MAX_SIZE, INT, target)).thenReturn(MAX_VALUE);
        when(PolicyUtil.getParameter("required", target)).thenReturn("true");
        when(target.getValue()).thenReturn("abc");
        PrepareResult prepareResult = target.prepareSelf();
        assertTrue(prepareResult.isError());
        assertEquals("com.atex.plugins.twitter.integer", prepareResult.getLocalizeMessage());
    }
}
