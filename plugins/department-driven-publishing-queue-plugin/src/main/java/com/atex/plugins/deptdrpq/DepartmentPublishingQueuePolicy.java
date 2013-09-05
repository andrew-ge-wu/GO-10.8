/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.deptdrpq;

import com.atex.plugins.baseline.collection.searchbased.PublishingQueuePolicyMetaDataBased;
import com.polopoly.application.Application;
import com.polopoly.cache.SynchronizedUpdateCache;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.policy.RadioButtonPolicy;
import com.polopoly.cm.app.policy.SingleReference;
import com.polopoly.cm.app.search.categorization.Categorization;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.Policy;
import com.polopoly.search.metadata.SearchCriteria;
import com.polopoly.search.metadata.SearchCriteriaBuilder;
import com.polopoly.siteengine.dispatcher.SiteEngine;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.model.request.ContentPath;
import com.polopoly.siteengine.structure.Page;
import com.polopoly.siteengine.structure.Site;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DepartmentPublishingQueuePolicy extends PublishingQueuePolicyMetaDataBased {

    public DepartmentPublishingQueuePolicy(Application application, CmClient cmClient, SynchronizedUpdateCache searchCache) {
        super(application, cmClient, searchCache);
    }


    /**
     * Get Content Reference from ChildPolicy department
     * 
     * @return
     * @throws com.polopoly.cm.client.CMException
     */
    SingleReference getReferenceSource() throws CMException {
        return (SingleReference) getChildPolicy("department");
    }

    public boolean isAutoDepartment() throws CMException {
        return "true".equalsIgnoreCase(((RadioButtonPolicy) getChildPolicy("autoDepartment")).getValue());
    }

    protected SearchCriteria getSearchCriteria() throws CMException {
        Policy site = getSelectedSite();

        Set<ExternalContentId> types = getTypesToSearchFor();

        SearchCriteriaBuilder searchCriteriaBuilder = new SearchCriteriaBuilder()
                .setPagesToSearchUnder(new HashSet<Policy>(Arrays.asList(site)))
                .setCategorization(Categorization.EMPTY_CATEGORIZATION).setTypes(types);

        return searchCriteriaBuilder.build();
    }

    @Override
    protected SolrQuery getSolrQuery() throws CMException {
        return getSearchCriteria().getQuery(getCMServer());
    }

    protected Policy getSelectedSite() throws CMException {
        if (isAutoDepartment()) {
            return getCurrentPagePolicy();
        } else {
            return getCMServer().getPolicy(getReferenceSource().getReference().getContentId());
        }
    }

    protected Policy getCurrentPagePolicy() throws CMException {
        TopModel topModel = getTopModel();

        if (topModel != null) {
            ContentPath contentPath = topModel.getRequest().getOriginalContentPath();
            for (int i = contentPath.size() - 1; i >= 0; i--) {
                ContentId contentId = contentPath.get(i);
                Policy policy = getCMServer().getPolicy(contentId);
                if (policy instanceof Site || policy instanceof Page) {
                    return policy;
                }
            }
        }

        return null;
    }

    protected TopModel getTopModel() {
        return SiteEngine.getTopModel();
    }

}
