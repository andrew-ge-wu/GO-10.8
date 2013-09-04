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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.policy.ConfigurableContentListWrapper;
import com.polopoly.cm.app.policy.ContentListWrapperPolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListRead;
import com.polopoly.cm.collections.ContentListSimple;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

public class ExtendedConfigurableContentListWrapper extends ConfigurableContentListWrapper{

    private static final Logger LOGGER = Logger.getLogger(ExtendedConfigurableContentListWrapper.class.getName());

    public static final String CREATOR_TEMPLATE = "allowedTemplates";

    public static final String TOP_PQ_LIST_EXT_ID = "p.siteengine.PublishingQueueContentRepositoryTemplates";

    protected PolicyCMServer cmServer;

    @Override
    protected void initSelf() throws CMException {
        super.initSelf();
        cmServer = getCmServer();
    }

    @Override
    public boolean isAddAllowed(ContentId referredContentId) throws CMException {
        ContentRead content = getContent(referredContentId);
        ContentRead inputTemplateContent = getContent(content.getInputTemplateId());
        ContentListRead externalIds = getAllowedInputTemplates();
        for (int i = 0; externalIds!=null && i <externalIds.size(); i++) {
            ContentId entryId = externalIds.getEntry(i).getReferredContentId();
            ContentRead cr = getCmServer().getContent(entryId);
            String entryExtId = cr.getExternalId().getExternalId();
            if (entryExtId.equalsIgnoreCase(inputTemplateContent.getExternalId().getExternalId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ContentListRead getAllowedInputTemplates() {
        return getAllAllowedIT();
    }

    /**
     * @return List of ContentId of publishing queue under 
     * "p.siteengine.PublishingQueueContentRepositoryTemplates"
     */
    protected List<ContentId> getAvailablePqs() {
        List<ContentId> contentIds = new ArrayList<ContentId>();
        try {
            Policy topPqPolicy = cmServer.getPolicy(new ExternalContentId(TOP_PQ_LIST_EXT_ID));
            ContentListWrapperPolicy clWrapperPolicy = (ContentListWrapperPolicy) topPqPolicy.getChildPolicy("templateList");
            ContentList pqList = clWrapperPolicy.getContentList();
            for (int j = 0; pqList != null && j < pqList.size(); j++) {
                ContentId cid = pqList.getEntry(j).getReferredContentId();
                Policy policy = cmServer.getPolicy(cid);
                ExternalContentId extId = policy.getContent().getExternalId();
                String extIdStr = getPqListEntryExtId(extId);
                if (extIdStr!=null) {
                    contentIds.add(cid.getContentId());
                }
            }
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
        return contentIds;
    }

    /**
     * @return ContentList which combines the parent allowed input templates
     * list and the system available publishing queue list
     */
    protected ContentListRead getAllAllowedIT() {
        ContentListRead rawResults = getParentAllowedITList();
        ContentListRead results;
        List<ContentId> contentIds = new ArrayList<ContentId>();
        try {
            contentIds.addAll(getAvailablePqs());
            for (int i = 0; rawResults != null && i < rawResults.size(); i++) {
                contentIds.add(rawResults.getEntry(i).getReferredContentId());
            }
            results = new ContentListSimple(contentIds);
            return results;
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
        return new ContentListSimple(new ArrayList<ContentId>());
    }

    /**
     * @param extId The External Content Id to be extracted
     * @return String of the external content id if exist, else return null
     */
    protected String getPqListEntryExtId(ExternalContentId extId) {
        String extIdStr = null;
        if (extId!=null) {
            extIdStr = extId.getExternalId();
            if (extIdStr!=null && !extIdStr.isEmpty()) {
                return extIdStr;
            }
        }
        return null;
    }

    // make unit test easier
    protected ContentRead getContent(ContentId contentId) throws CMException {
        return getCmServer().getContent(contentId);
    }

    protected ContentListRead getParentAllowedITList(){
        ContentListRead parentAllowedITList = super.getAllowedInputTemplates();
        return parentAllowedITList;
    }

    protected PolicyCMServer getCmServer() {
        return getWrapperDefinition().getPolicyCMServer();
    }
}
