/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.deptdrpq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.policy.PrepareResult;

public class DeptContentTreeSelectPolicyTest {
    private DeptContentTreeSelectPolicy target;

    @Mock
    private Content content;
    @Mock
    InputTemplate inputTemplate;
    @Mock
    PolicyCMServer cmServer;
    @Mock
    private DepartmentPublishingQueuePolicy parentPolicy;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        target = spy(new DeptContentTreeSelectPolicy());
        target.init("PolicyName", new Content[] { content }, inputTemplate, parentPolicy, cmServer);
    }

    @Test
    public void testIsAutoDeptTrue() throws CMException {
        when(parentPolicy.isAutoDepartment()).thenReturn(true);
        assertTrue(target.isAutoDept());
    }

    @Test
    public void testIsAutoDeptFalse() throws CMException {
        when(parentPolicy.isAutoDepartment()).thenReturn(false);
        assertFalse(target.isAutoDept());
    }

    @Test
    public void testPrepareSelfIfNonAutoAndNoDeptSelected() throws CMException {
        doReturn(false).when(target).isAutoDept();

        PrepareResult prepareResult = target.prepareSelf();
        assertTrue(prepareResult.isError());
        assertEquals("cm.policy.ValueRequired", prepareResult.getLocalizeMessage());
    }

    @Test
    public void testPrepareSelfIfAutoDept() throws CMException {
        doReturn(true).when(target).isAutoDept();

        PrepareResult prepareResult = target.prepareSelf();
        assertFalse(prepareResult.isError());
    }

    @Test
    public void testPreCommitSelf() throws CMException {
        target.setComponent("test", "test");

        target.preCommitSelf();
        assertNull(target.getComponent("test"));
    }
}
