/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */
package com.atex.plugins.twitter;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.path.PathSegment;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.siteengine.dispatcher.PathUtil;
import com.polopoly.siteengine.structure.Alias;
import com.polopoly.siteengine.structure.Page;
import com.polopoly.siteengine.structure.Site;
import com.polopoly.siteengine.structure.SitePolicy;
import com.polopoly.siteengine.structure.SiteRootPolicy;

public class URLBuilder {

    private PolicyCMServer policyCMServer;
    private ContentId contentId;

    public URLBuilder(ContentId contentId, PolicyCMServer policyCMServer) {
        this.contentId = contentId;
        this.policyCMServer = policyCMServer;
    }

    public String getFullUrl() throws CMException {
        ContentId[] contentIds = PathUtil.createPathToClass(this.contentId, SiteRootPolicy.class, this.policyCMServer);
        StringBuffer sb = new StringBuffer();

        for (ContentId cid : contentIds) {
            Policy policy =  this.policyCMServer.getPolicy(cid);

            if(policy instanceof Site) { 
                Alias alias = ((SitePolicy)policy).getMainAlias();

                if(alias == null) 
                    throw new CMException("Site Main Alias Not defined!");

                sb.append(this.validate(((SitePolicy)policy).getMainAlias().getUrl()));

            } else if(policy instanceof Page && policy instanceof PathSegment) {
                sb.append(((PathSegment)policy).getPathSegmentString());
                sb.append("/");

            } else if(policy instanceof PathSegment) {
                sb.append(validate(policy));

            } else {
                sb.append(policy.getContentId().getContentId().getContentIdString());
            }
        }
        return sb.toString();
    }

    private String validate(String url) {
        if (url.charAt(url.length()-1) != '/') {
            url += "/";
        }
        return url;
    }

    private String validate(Policy policy) throws CMException {
        String pathSegment = ((PathSegment)policy).getPathSegmentString();
        String contendId = policy.getContentId().getContentId().getContentIdString();

        return (pathSegment.contains(contendId)) 
               ? pathSegment 
               : pathSegment + "-" + contendId;
    }
}
