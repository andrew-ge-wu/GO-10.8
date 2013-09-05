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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.polopoly.application.Application;
import com.polopoly.cache.SynchronizedUpdateCache;
import com.polopoly.cm.app.policy.ContentListModel;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.model.ModelDomain;
import com.polopoly.model.ModelFactory;
import com.polopoly.siteengine.dispatcher.SiteEngine;
import com.polopoly.siteengine.dispatcher.SiteEngineApplication;

public class DepartmentPublishingQueuePolicyTest {
    private DepartmentPublishingQueuePolicy target;

    @Mock
    private SiteEngineApplication siteEngineApplication;
    @Mock
    private ModelFactory modelFactory;
    @Mock
    private ModelDomain domain;

    @Mock
    Application application;
    @Mock
    CmClient cmClient;
    @Mock
    SynchronizedUpdateCache searchCache;

    @Mock
    ContentList contentList;
    @Mock
    ContentListModel contentListModel;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        target = spy(new DepartmentPublishingQueuePolicy(application, cmClient, searchCache));
        SiteEngine.setApplication(siteEngineApplication);
        when(siteEngineApplication.getModelFactory()).thenReturn(modelFactory);
        when(siteEngineApplication.getModelDomain()).thenReturn(domain);
    }

    @After
    public void tearDown() throws Exception {
        SiteEngine.setApplication(null);
    }

    @Ignore
    @Test
    public void testGetContentList() throws CMException {
        
        when(modelFactory.createModel(domain, contentList)).thenReturn(contentListModel);

        ContentList returnedContentList = target.getContentList();

        assertEquals(contentListModel, returnedContentList);
    }
}
