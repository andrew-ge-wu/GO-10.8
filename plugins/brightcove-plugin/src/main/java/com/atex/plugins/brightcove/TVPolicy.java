/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.app.policy.SelectableSubFieldPolicy;
import com.polopoly.cm.app.search.categorization.CategorizationProvider;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.app.search.categorization.Categorization;
import com.polopoly.siteengine.standard.content.ContentBasePolicy;

/**
 *
 */
public class TVPolicy extends ContentBasePolicy implements VideoListModelTypeDescription ,
    CategorizationProvider {

    private static final Logger LOGGER = Logger.getLogger(TVPolicy.class.getName());
    
    public static final String CONTENT_LIST_NAME = "videos";
    public static final String SLOT_ELEMENTS = "elements/slotElements";

    public List<BrightcoveVideoPolicy> getVideos() {
        Policy policy = getFirstBcListEle();
        if (policy!=null) {
            ListElementPolicy listElePolicy = (ListElementPolicy) policy;
            return listElePolicy.getVideos();
        }
        return new ArrayList<BrightcoveVideoPolicy>();
    }

    protected Policy getFirstBcListEle() {
        try {
            ContentList contentList = getContentList(CONTENT_LIST_NAME);
            for (int i=0; i<contentList.size(); i++) {
                ContentReference cRef = contentList.getEntry(i);
                ContentId cId = cRef.getReferredContentId();
                Policy policy = getCMServer().getPolicy(cId);
                if (policy instanceof ListElementPolicy) {
                    return policy;
                }
            }
        } catch (CMException e) {
            LOGGER.log(Level.INFO, "Fail to retrieve list from CM" + e);
        }
        return null;
    }

    public String getSelectedCategorizationOption() {
        String selectedName = null;
        try {
            SelectableSubFieldPolicy selectablePolicy = (SelectableSubFieldPolicy) getChildPolicy("categorizationOption");
            if (selectablePolicy!=null) {
                selectedName = selectablePolicy.getSelectedSubFieldName();
            }
        } catch (CMException e) {
            LOGGER.log(Level.INFO, "Failed to retrieve categorization option " + e);
            return null;
        }
        return selectedName;
    }

    protected CategorizationProvider getCategorizationProvider()
        throws CMException {
        CategorizationProvider categorizationProvider = (CategorizationProvider) getChildPolicy("categorizationOption/categorization");
        return categorizationProvider;
    }

    public Categorization getCategorization() throws CMException {
        return getCategorizationProvider().getCategorization();
    }

    public void setCategorization(Categorization categorization) throws CMException {
        getCategorizationProvider().setCategorization(categorization);
    }

    public ContentId getRelatedElementId() {
        try {
            ContentList relatedElements = getContentList(SLOT_ELEMENTS);
            ListIterator<ContentReference> iterator = relatedElements.getListIterator();
            while (iterator.hasNext()) {
                ContentReference ref = iterator.next();
                return ref.getReferredContentId();
            }
        } catch (CMException e) {
            return null;
        }
        return null;
    }
}
