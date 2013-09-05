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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.polopoly.application.Application;
import com.polopoly.cache.SynchronizedUpdateCache;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.policy.RadioButtonPolicy;
import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.ModelDomain;
import com.polopoly.model.ModelFactory;
import com.polopoly.siteengine.dispatcher.SiteEngine;
import com.polopoly.siteengine.dispatcher.SiteEngineApplication;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.model.request.ContentPath;
import com.polopoly.siteengine.model.request.RequestScope;
import com.polopoly.siteengine.structure.Page;
import com.polopoly.siteengine.structure.Site;

public class DepartmentPublishingQueuePolicy2Test {
    private DepartmentPublishingQueuePolicy target;

    @Mock
    private SiteEngineApplication siteEngineApplication;
    @Mock
    private ModelFactory modelFactory;
    @Mock
    private ModelDomain domain;
    @Mock
    private TopModel topModel;
    @Mock
    private RequestScope requestScope;
    @Mock
    private ContentPath contentPath;

    @Mock
    Application application;
    @Mock
    CmClient cmClient;
    @Mock
    SynchronizedUpdateCache searchCache;

    @Mock
    private Content content;
    @Mock
    InputTemplate inputTemplate;
    @Mock
    PolicyCMServer cmServer;
    @Mock
    private Policy parentPolicy;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        target = spy(new DepartmentPublishingQueuePolicy(application, cmClient, searchCache));

        SiteEngine.setApplication(siteEngineApplication);
        when(siteEngineApplication.getModelFactory()).thenReturn(modelFactory);
        when(siteEngineApplication.getModelDomain()).thenReturn(domain);
        when(topModel.getRequest()).thenReturn(requestScope);
        when(requestScope.getOriginalContentPath()).thenReturn(contentPath);

        target.init("PolicyName", new Content[] { content }, inputTemplate, parentPolicy, cmServer);
    }

    @After
    public void tearDown() throws Exception {
        SiteEngine.setApplication(null);
    }

    @Test
    public void testGetCurrentPagePolicyWhenTopModelIsNull() throws CMException {
        assertNull(target.getCurrentPagePolicy());
    }

    @Test
    public void testGetCurrentPagePolicyForSite() throws CMException {
        Policy expectedPolicy = mock(DumpSite.class);
        ContentId contentId = mock(ContentId.class);

        doReturn(topModel).when(target).getTopModel();
        doReturn(1).when(contentPath).size();

        when(contentPath.get(0)).thenReturn(contentId);
        when(cmServer.getPolicy(contentId)).thenReturn(expectedPolicy);

        assertEquals(expectedPolicy, target.getCurrentPagePolicy());
    }

    @Test
    public void testGetCurrentPagePolicyForPage() throws CMException {
        Policy expectedPolicy = mock(DumpPage.class);
        ContentId contentId = mock(ContentId.class);

        doReturn(topModel).when(target).getTopModel();
        doReturn(1).when(contentPath).size();

        when(contentPath.get(0)).thenReturn(contentId);
        when(cmServer.getPolicy(contentId)).thenReturn(expectedPolicy);

        assertEquals(expectedPolicy, target.getCurrentPagePolicy());
    }

    @Test
    public void testGetCurrentPagePolicyForNonSiteOrPage() throws CMException {
        Policy expectedPolicy = mock(Policy.class);
        ContentId contentId = mock(ContentId.class);

        doReturn(topModel).when(target).getTopModel();
        doReturn(1).when(contentPath).size();

        when(contentPath.get(0)).thenReturn(contentId);
        when(cmServer.getPolicy(contentId)).thenReturn(expectedPolicy);

        assertNull(target.getCurrentPagePolicy());
    }

    @Test
    public void testGetSelectedSitesIfAutoDepartment() throws CMException {
        Policy policy = mock(Policy.class);
        doReturn(true).when(target).isAutoDepartment();
        doReturn(policy).when(target).getCurrentPagePolicy();

        assertEquals(policy, target.getSelectedSite());
    }

    @Test
    public void testGetReferenceSource() throws CMException {
        Policy policy = mock(Policy.class);
        assertEquals(policy.getContentReference("department"), target.getReferenceSource());
    }

    @Test
    public void testIsAutoDepartment() throws CMException {
        RadioButtonPolicy radioButtonPolicy = mock(RadioButtonPolicy.class);
        doReturn(radioButtonPolicy).when(target).getChildPolicy("autoDepartment");
        doReturn("true").when(radioButtonPolicy).getValue();
        assertTrue("true".equalsIgnoreCase(Boolean.toString(target.isAutoDepartment())));
    }

    @Test
    public void testGetTitleIfTitleExists() throws CMException {
        String expected = "this is title";
        String notExpected = "this is name";
        DumpSingleValued titlePolicy = mock(DumpSingleValued.class);
        when(titlePolicy.getValue()).thenReturn(expected);

        doReturn(titlePolicy).when(target).getChildPolicy("title");

        String result = target.getTitle();
        assertEquals(expected, result);
        assertNotSame(notExpected, result);
    }

    @Test
    public void testGetTitleIfTitleIsNull() throws CMException {
        String expected = "this is name";
        DumpSingleValued titlePolicy = mock(DumpSingleValued.class);
        when(titlePolicy.getValue()).thenReturn(null);

        doReturn(expected).when(target).getName();
        doReturn(titlePolicy).when(target).getChildPolicy("title");

        String result = target.getTitle();
        assertEquals(expected, result);
        assertNotNull(result);
    }

    @Test
    public void testGetTitleIfTitleIsEmpty() throws CMException {
        String expected = "this is name";
        DumpSingleValued titlePolicy = mock(DumpSingleValued.class);
        when(titlePolicy.getValue()).thenReturn("");

        doReturn(expected).when(target).getName();
        doReturn(titlePolicy).when(target).getChildPolicy("title");

        String result = target.getTitle();
        assertEquals(expected, result);
        assertNotNull(result);
        assertNotSame("", result);
    }

    @Test
    public void testGetTimeForLastYear() {

        Calendar cal = Calendar.getInstance();

        int actual = cal.get(Calendar.YEAR) -1;

        cal.setTime(new Date());
        cal.add(Calendar.YEAR, -1);
        int expected = cal.get(Calendar.YEAR);

        assertEquals(expected, actual);
    }

    private interface DumpSingleValued extends SingleValued, Policy {
    }


    private interface DumpSite extends Site, Policy {
    }

    private interface DumpPage extends Page, Policy {
    }

}
