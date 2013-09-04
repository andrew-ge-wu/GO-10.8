/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.app.policy.ConfigurableContentListWrapper;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

public class ConfigListWrapper extends ConfigurableContentListWrapper {

    private static final Logger LOGGER = Logger.getLogger(ConfigListWrapper.class.getName());

    PolicyCMServer cmServer ;

    protected void getCmServer() {
        cmServer = getWrapperDefinition().getPolicyCMServer();;
    }

    protected Map<String, String> loadSiteIntoMap(ContentList contentList) {
        Map<String, String> siteMap = new HashMap<String, String>();
        StringBuilder sb = new StringBuilder() ;
        for (int i = 0 ; i<contentList.size() ; i++ ) {
            ContentReference contRef;
            try {
                contRef = contentList.getEntry(i);
                ContentId id = contRef.getReferredContentId();
                Policy policy = cmServer.getPolicy(id);
                if (policy instanceof SiteConfigPolicy) {
                    SiteConfigPolicy configPolicy = (SiteConfigPolicy) policy;
                    String key = configPolicy.getSite().getContentId().getContentIdString();
                    String value = id.getContentId().getContentIdString();
                    sb.append(key + "," +value);
                    sb.append(" ");
                    siteMap.put(key, value);
                }
            } catch (CMException e) {
                e.printStackTrace();
            }
        }
        return siteMap;
    }

    protected String getSiteIdStringByConfigId(ContentId id) throws CMException {
        String result = null;
        try {
            Policy policy = cmServer.getPolicy(id);
            if (policy instanceof SiteConfigPolicy) {
                SiteConfigPolicy configPolicy = (SiteConfigPolicy) policy;
                ContentId siteId = ((BrightcoveSiteConfigPolicy) configPolicy).getSite();
                result = siteId.getContentId().getContentIdString();
            }
        } catch (CMException e) {
            throw e;
        }
        return result;
    }

    @Override
    public boolean isAddAllowed(ContentId id) throws CMException {
        getCmServer();
//        TODO: Avoid duplicated sites
//        Map<String, String> existingConfig = loadSiteIntoMap(getWrappedContentList());
        LOGGER.log(Level.INFO, id.getContentIdString());
//        String currentSiteId = getSiteIdStringByConfigId(id);
//        LOGGER.log(Level.INFO, "site id: " + currentSiteId );
//        if (currentSiteId==null || existingConfig.containsKey(currentSiteId) ) {
//            throw new CMException("The site selected is already configured.", 
//                    "com.atex.plugins.brightcove.site.config.configExist.err");
//        }
        return super.isAddAllowed(id);
    }

}
