/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.plugins.brightcove.BrightcoveConfigPolicy;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.siteengine.structure.PagePolicy;

public class ConfigurationUtil {
    private final Logger LOGGER = Logger.getLogger(ConfigurationUtil.class.getName());

    PolicyCMServer cmServer;

    public ConfigurationUtil(PolicyCMServer cmServer) {
        this.cmServer = cmServer;
    }

    public String getConfigReadToken(ContentId id) {
        try {
            BrightcoveConfigPolicy cfgPolicy = getBrightcoveConfigPolicy();
            String siteId = getCurrentRootSiteId(id);
            return cfgPolicy.getReadToken(siteId);
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Fail to retrieve read token from config " + e);
        }
        return null;
    }

    public String getConfigPlayerId(ContentId id) {
        try {
            BrightcoveConfigPolicy cfgPolicy = getBrightcoveConfigPolicy();
            String siteId = getCurrentRootSiteId(id);
            return cfgPolicy.getPreviewPlayerId(siteId);
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Fail to retrieve player id from config " + e);
        }
        return null;
    }

    public ContentId getConfigWebTVDepartment(ContentId id) {
        try {
            BrightcoveConfigPolicy cfgPolicy = getBrightcoveConfigPolicy();
            String siteId = getCurrentRootSiteId(id);
            return cfgPolicy.getDepartment(siteId);
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Fail to retrieve web tv department from config " + e);
        }
        return null;
    }

    public String getCurrentRootSiteId(ContentId id) {
        try {
            ContentId topParentId = cmServer.getContent(id).getSecurityParentId();
            Policy policy = cmServer.getPolicy(topParentId);
            if (policy instanceof PagePolicy) {
                PagePolicy sitePolicy = (PagePolicy) policy;
                ContentId[] ids = sitePolicy.getParentIds();
                if (ids.length==0) {
                    return topParentId.getContentIdString();
                }
                return ids[0].getContentIdString();
            }
            return null;
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Fail to retrieve player id from config " + e);
        }
        return null;
    }

    protected BrightcoveConfigPolicy getBrightcoveConfigPolicy() throws CMException {
        return (BrightcoveConfigPolicy)cmServer.getPolicy(BrightcoveConfigPolicy.CONTENT_ID);
    }

}
