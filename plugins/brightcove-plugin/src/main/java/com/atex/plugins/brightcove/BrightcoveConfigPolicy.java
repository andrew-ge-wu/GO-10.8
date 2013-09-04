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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

public class BrightcoveConfigPolicy extends ContentPolicy implements ConfigPolicy {

    protected static final String CONFIG_LIST = "brightcoveConfigs";

    public static final String TEMPLATE = "com.atex.plugins.brightcove.BrightcoveConfigHome";
    public static final ContentId CONTENT_ID = new ExternalContentId(TEMPLATE);

    private PolicyCMServer cmServer;
    private static final Logger LOGGER = Logger.getLogger(BrightcoveConfigPolicy.class.getName());

    protected void getPolicyCmServer() {
        cmServer = getCMServer();
    }

    public String getSiteId() {
        List<String> siteIds = getSiteIds();
        if (siteIds.size()>0) {
            return siteIds.get(0);
        }
        return null;
    }

    protected List<String> getSiteIds() {
        List<String> siteIds = new ArrayList<String>();
        Set<String> keySet = getConfigPolicies().keySet();
        siteIds.addAll(keySet);
        return siteIds;
    }

    protected Map<String, SiteConfigPolicy> getConfigPolicies() {
        Map<String, SiteConfigPolicy> configPolicies = new LinkedHashMap<String, SiteConfigPolicy>();
        ContentList contentList;
        try {
            contentList = getContentList(CONFIG_LIST);
            for (int i = 0; i < contentList.size(); i++) {
                ContentReference contentRef = contentList.getEntry(i);
                ContentId cfgId = contentRef.getReferredContentId();
                Policy policy = cmServer.getPolicy(cfgId);
                SiteConfigPolicy configPolicy = (SiteConfigPolicy) policy;
                ContentId siteId = configPolicy.getSite();
                String key = siteId.getContentIdString();
                configPolicies.put(key, configPolicy);
            }
        } catch (CMException e) {
            LOGGER.log(Level.INFO, "Failed to retrieve content list " 
                    + CONFIG_LIST + " " + e.getMessage());
        }
        return configPolicies;
    }

    public SiteConfigPolicy getConfigPolicy() {
        return getConfigPolicy(getSiteId());
    }

    public SiteConfigPolicy getConfigPolicy(String siteId) {
        Map<String, SiteConfigPolicy> configPolicies = new LinkedHashMap<String, SiteConfigPolicy>();
        configPolicies = getConfigPolicies();
        // return null if there is no single configuration available
        if (configPolicies.isEmpty()) {
            return null;
        }
        // use the default siteId if siteId is null value
        if (siteId==null) {
            siteId = getSiteId();
        }
        SiteConfigPolicy siteConfigPolicy = configPolicies.get(siteId);
        // return the default siteConfigPolicy if the siteId not found
        if (siteConfigPolicy==null) {
            siteConfigPolicy = (SiteConfigPolicy) configPolicies.get(getSiteId());
        }
        return siteConfigPolicy;
    }

    @Override
    protected void initSelf() {
        super.initSelf();
        getPolicyCmServer();
    }

    public String getPublisherId() throws CMException {
        return getPublisherId(getSiteId());
    }

    public String getPublisherId(String siteId) throws CMException {
        BrightcoveSiteConfigPolicy configPolicy = (BrightcoveSiteConfigPolicy) getConfigPolicy(siteId);
        if (configPolicy==null) {
            return "";
        }
        return configPolicy.getPublisherId();
    }

    public String getReadToken() throws CMException {
        return getReadToken(getSiteId());
    }

    public String getReadToken(String siteId) throws CMException {
        BrightcoveSiteConfigPolicy configPolicy = (BrightcoveSiteConfigPolicy) getConfigPolicy(siteId);
        if (configPolicy==null) {
            return "";
        }
        return configPolicy.getReadToken();
        
    }

    public String getReadTokenUrl() throws CMException {
        return getReadTokenUrl(getSiteId());
    }

    public String getReadTokenUrl(String siteId) throws CMException {
        BrightcoveSiteConfigPolicy configPolicy = (BrightcoveSiteConfigPolicy) getConfigPolicy(siteId);
        if (configPolicy==null) {
            return "";
        }
        return configPolicy.getReadTokenUrl();
    }

    public String getWriteToken() throws CMException {
        return getWriteToken(getSiteId());
    }

    public String getWriteToken(String siteId) throws CMException {
        BrightcoveSiteConfigPolicy configPolicy = (BrightcoveSiteConfigPolicy) getConfigPolicy(siteId);
        if (configPolicy==null) {
            return "";
        }
        return configPolicy.getWriteToken();
    }

    public String getPreviewPlayerId() throws CMException {
        return getPreviewPlayerId(getSiteId());
    }

    public String getPreviewPlayerId(String siteId) throws CMException {
        BrightcoveSiteConfigPolicy configPolicy = (BrightcoveSiteConfigPolicy) getConfigPolicy(siteId);
        if (configPolicy==null) {
            return "";
        }
        return configPolicy.getPreviewPlayerId();
    }

    public ContentId getDepartment() throws CMException {
        return getDepartment(getSiteId());
    }

    public ContentId getDepartment(String siteId) throws CMException {
        BrightcoveSiteConfigPolicy configPolicy = (BrightcoveSiteConfigPolicy) getConfigPolicy(siteId);
        if (configPolicy==null) {
            return null;
        }
        return configPolicy.getDepartment();
    }

    public Map<String, String> getMappings() throws CMException {
        return getMappings(getSiteId());
    }

    public Map<String, String> getMappings(String siteId) throws CMException {
        BrightcoveSiteConfigPolicy configPolicy = (BrightcoveSiteConfigPolicy) getConfigPolicy(siteId);
        if (configPolicy==null) {
            return new HashMap<String, String>();
        }
        return configPolicy.getMappings();
    }
}
