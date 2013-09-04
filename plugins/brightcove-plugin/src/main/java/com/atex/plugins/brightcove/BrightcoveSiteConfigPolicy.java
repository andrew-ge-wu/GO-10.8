/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove;

import java.util.Collections;
import java.util.Map;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.policy.CheckboxPolicy;
import com.polopoly.cm.app.policy.NameValuePolicy;
import com.polopoly.cm.app.policy.SingleReference;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;

public class BrightcoveSiteConfigPolicy extends ContentPolicy implements SiteConfigPolicy {
    protected static final String PUBLISHER_ID = "publisherId";
    protected static final String READ_TOKEN = "readToken";
    protected static final String READ_TOKEN_URL = "readTokenUrl";
    protected static final String WRITE_TOKEN = "writeToken";
    protected static final String PREVIEW_PLAYER_ID = "previewPlayerId";
    protected static final String DEPARTMENT = "department";
    protected static final String SITE = "site";
    protected static final String MAP_CUSTOM_FIELD = "mapCustomField";
    protected static final String CUSTOM_FIELDS = "mapping";

    public static final String TEMPLATE = "com.atex.plugins.brightcove.BrightcoveConfigHome";
    
    public static final ContentId CONTENT_ID = new ExternalContentId(TEMPLATE);

    public ContentId getSite() throws CMException {
        return ((SingleReference)getChildPolicy(SITE)).getReference();
    }

    public String getPublisherId() throws CMException {
        return ((SingleValuePolicy) getChildPolicy(PUBLISHER_ID)).getValue();
    }

    public void setPublisherId(String publisherId) throws CMException {
        ((SingleValuePolicy) getChildPolicy(PUBLISHER_ID)).setValue(publisherId);
    }

    public String getReadToken() throws CMException {
        return ((SingleValuePolicy) getChildPolicy(READ_TOKEN)).getValue();
    }

    public void setReadToken(String readToken) throws CMException {
        ((SingleValuePolicy) getChildPolicy(READ_TOKEN)).setValue(readToken);
    }

    public String getReadTokenUrl() throws CMException {
        return ((SingleValuePolicy) getChildPolicy(READ_TOKEN_URL)).getValue();
    }

    public void setReadTokenUrl(String readTokenUrl) throws CMException {
        ((SingleValuePolicy) getChildPolicy(READ_TOKEN_URL)).setValue(readTokenUrl);
    }

    public String getWriteToken() throws CMException {
        return ((SingleValuePolicy) getChildPolicy(WRITE_TOKEN)).getValue();
    }

    public void setWriteToken(String writeToken) throws CMException {
        ((SingleValuePolicy) getChildPolicy(WRITE_TOKEN)).setValue(writeToken);
    }

    public String getPreviewPlayerId() throws CMException {
        return ((SingleValuePolicy) getChildPolicy(PREVIEW_PLAYER_ID)).getValue();
    }
    
    public ContentId getDepartment() throws CMException {
        return ((SingleReference)getChildPolicy(DEPARTMENT)).getReference();
    }

    private boolean isMapCustomField() throws CMException {
        return ((CheckboxPolicy) getChildPolicy(MAP_CUSTOM_FIELD)).getChecked();
    }

    /**
     * Get the custom mapping defined by user at GUI
     * @return map of brightcove custom field (key) and polopoly metadata field (value)
     * @throws CMException
     */
    public Map<String, String> getMappings() throws CMException {
        if(isMapCustomField()) {
            NameValuePolicy nameValuePolicy = (NameValuePolicy) getChildPolicy(CUSTOM_FIELDS);
            return Collections.unmodifiableMap(nameValuePolicy.getNamesAndValues());
        } else {
            return Collections.emptyMap();
        }
    }
}
