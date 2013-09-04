/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove;

import static com.polopoly.cm.app.search.categorization.Categorization.categorization;
import static com.polopoly.cm.app.search.categorization.Categorization.tagDimension;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.search.categorization.Categorization;
import com.polopoly.cm.app.search.categorization.CategorizationProvider;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.Model;
import com.polopoly.model.ModelBase;
import com.polopoly.model.ModelStoreInBean;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

public class BrightcoveRelatedContentElementControllerTest {

    private BrightcoveRelatedContentElementController target;

    @Mock
    private RenderRequest request;
    @Mock
    private TopModel m;
    @Mock
    private ControllerContext context;
    private ModelWrite local;
    private Categorization categorization;
    @Mock
    private Model contentModel;

    @Mock
    private Policy contentPolicy1;
    @Mock
    private Policy contentPolicy2;

    @Mock
    private CategorizationContentPolicy categorizationPolicy;
    private ContentId parentId = new ContentId(1,100);

    @Mock
    private PolicyCMServer cmServer;
    private ContentId parentId2 = new ContentId(1,100);
    @Mock
    Content content2;
    @Mock
    Content content1;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = new BrightcoveRelatedContentElementController();
        local = new ModelBase();
        when(m.getLocal()).thenReturn(local);
        categorization = 
            categorization().withDimensions(
                tagDimension().id("hepp").build()
            ).build();
        when(contentPolicy1.getCMServer()).thenReturn(cmServer);
        when(contentPolicy1.getContent()).thenReturn(content1);
        when(contentPolicy2.getContent()).thenReturn(content2);
        when(categorizationPolicy.getCategorization()).thenReturn(categorization);

        when(context.getContentModel()).thenReturn(contentModel);
        when(contentModel.getAttribute(ModelStoreInBean.BEAN_ATTRIBUTE_NAME)).thenReturn(contentPolicy1);

        when(content1.getSecurityParentId()).thenReturn(parentId);
        when(cmServer.getPolicy(parentId)).thenReturn(contentPolicy2);
}

    @Test
    public void testShouldGetCategorizationFromFirstCategorizationProviderInParentPath() throws Exception {
        when(content2.getSecurityParentId()).thenReturn(parentId2);
        when(cmServer.getPolicy(parentId2)).thenReturn(categorizationPolicy);
        target.populateModelBeforeCacheKey(request, m, context);
        assertEquals(categorization, local.getAttribute("categorization"));
    }

    @Test
    public void testShouldReturnEmptyCategorizationIfNoCategorizationProviderInPath() throws Exception {
        when(content2.getSecurityParentId()).thenReturn(null);
        target.populateModelBeforeCacheKey(request, m, context);
        assertEquals(Categorization.EMPTY_CATEGORIZATION, local.getAttribute("categorization"));
    }

    @Test
    public void testShouldReturnEmptyCategorizationIfCMException() throws CMException {
        when(contentPolicy1.getCMServer())
            .thenThrow(new CMException("Unit Test Exception"));
        target.populateModelBeforeCacheKey(request, m, context);
        assertEquals(Categorization.EMPTY_CATEGORIZATION, local.getAttribute("categorization"));
    }

    @Test
    public void testShouldReturnEmptyCategorizationIfParentPolicyIsNull() throws CMException {
        when(content2.getSecurityParentId()).thenReturn(parentId2);
        when(cmServer.getPolicy(parentId2)).thenReturn(null);
        target.populateModelBeforeCacheKey(request, m, context);
        assertEquals(Categorization.EMPTY_CATEGORIZATION, local.getAttribute("categorization"));
    }

    private class CategorizationContentPolicy extends ContentPolicy 
        implements CategorizationProvider {
        public Categorization getCategorization() throws CMException {
            return null;
        }

        public void setCategorization(Categorization categorization)
                throws CMException {
        }
    }

}
