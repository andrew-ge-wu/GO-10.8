/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove;

import java.io.IOException;
import java.util.ListIterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.atex.plugins.brightcove.util.CategorizationService;
import com.brightcove.commons.catalog.objects.CustomField;
import com.brightcove.commons.catalog.objects.Video;
import com.polopoly.cm.app.imagemanager.ContentImage;
import com.polopoly.cm.app.imagemanager.Image;
import com.polopoly.cm.app.imagemanager.ImageManagerPolicy;
import com.polopoly.cm.app.imagemanager.ImageSetPolicy;
import com.polopoly.cm.app.inbox.InboxFlags;
import com.polopoly.cm.app.policy.SelectableSubFieldPolicy;
import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.app.search.categorization.Categorization;
import com.polopoly.cm.app.search.categorization.CategorizationProvider;
import com.polopoly.cm.app.search.categorization.impl.CategorizationUtil;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.servlet.RequestPreparator;
import com.polopoly.cm.servlet.URLBuilder;
import com.polopoly.siteengine.standard.content.ContentBasePolicy;

public class BrightcoveVideoPolicy extends ContentBasePolicy 
    implements BrightcoveVideoModelTypeDescription, CategorizationProvider {

    public static final Logger LOG = Logger.getLogger(BrightcoveVideoPolicy.CLASS);
    public static final String ID = "video";
    public static final String SHORT_DESCRIPTION = "lead";
    public static final String LONG_DESCRIPTION = "body";
    public static final String CATEGORIZATION = "categorization";
    public static final String TYPE = "imageType";
    public static final String HTTP_IMAGE = "httpImage";
    public static final String IMAGE = "image";
    public static final String SLOT_ELEMENTS = "elements/slotElements";

    public String getId() {
        return getChildValue(ID);
    }

    public void setId(String value) throws CMException {
        setChildValue(ID, value);
    }

    public String getShortDescription() {
        return getChildValue(SHORT_DESCRIPTION);
    }

    public void setShortDescription(String value) throws CMException {
        setChildValue(SHORT_DESCRIPTION, value);
    }

    public String getLongDescription() {
        return getChildValue(LONG_DESCRIPTION);
    }

    public void setLongDescription(String value) throws CMException {
        setChildValue(LONG_DESCRIPTION, value);
    }

    public SelectableSubFieldPolicy getSubFieldPolicy() throws CMException {
        return (SelectableSubFieldPolicy) getChildPolicy(TYPE);
    }

    void setChildValue(String name, String value) throws CMException {
        Policy child = getChildPolicy(name);
        if (child instanceof SingleValued) {
            ((SingleValued) child).setValue(value);
        } else {
            throw new CMException("Unable to set value to child " + name + ", it is type of "
                    + (child == null ? "null" : child.getClass()));
        }
    }

    @Override
    public void postCreateSelf() throws CMException {
        // All articles should be in the Inbox by default.
        // If integrating with e.g. a print system, you might want to set this
        // only on articles arriving from the print system.
        new InboxFlags().setShowInInbox(this, true);
    }

    public String getUrl(HttpServletRequest request) {
        try {
            SelectableSubFieldPolicy p = getSubFieldPolicy();
            if (IMAGE.equals(p.getSelectedSubFieldName())) {
                ImageManagerPolicy subField = (ImageManagerPolicy) p.getChildPolicy(IMAGE);
                ImageSetPolicy imageSet = subField.getSelectedImage();
                if (imageSet==null) {
                    return null;
                }
                ContentImage image = imageSet.getImage();
                URLBuilder urlBuilder = RequestPreparator.getURLBuilder(request);
                return getUrl(urlBuilder, image.getPath(), request);
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
        catch (CMException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
        return null;
    }

    String getUrl(URLBuilder urlBuilder, String path, HttpServletRequest request) throws CMException {
        return urlBuilder.createFileUrl(getContentId(), path, request);
    }

    public long getPublishingDateTime() {
        long publishingDateTime = getContentCreationTime();
        return publishingDateTime;
    }

    public Object getVideoBean() {
        return this;
    }

    public String getImagePath() {
        String path = null;

        try {
            SelectableSubFieldPolicy subFieldPolicy = getSubFieldPolicy();

            if (IMAGE.equals(subFieldPolicy.getSelectedSubFieldName())) {
                ImageManagerPolicy imagePolicy = (ImageManagerPolicy) subFieldPolicy.getChildPolicy(IMAGE);
                ImageSetPolicy imageSet = imagePolicy.getSelectedImage();
                if (imageSet==null) {
                    return null;
                }
                Image image = imageSet.getImage("preview_320");

                path = "/polopoly_fs/" 
                       + image.getImageContentId().getContentIdString() 
                       + "!/" 
                       + image.getPath();
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
        return path;
    }

    protected CategorizationProvider getCategorizationProvider()
        throws CMException {
        CategorizationProvider categorizationProvider = (CategorizationProvider) getChildPolicy("categorization");
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

    public void mergeCategorization(List<String> tags, List<CustomField> customFields, Map<String, String> mappings) throws CMException {
        Categorization newTags = CategorizationService.getInstance().toCategorization(tags, customFields, mappings);
        Categorization oldTags = getCategorization();
        Categorization mergeTag = (oldTags == null || oldTags.isEmpty())
                ? newTags
                : new CategorizationUtil().unionOf(newTags, oldTags);
        setCategorization(mergeTag);
    }
    
    /**
     * create a Brightcove Video
     * @param mapping map polopoly categorization to Brighcove custom fields
     * @return video
     * @throws CMException 
     */
    public Video toVideo(Map<String, String> mapping) throws CMException {
        Video video = new Video();
        video.setName(getName());
        video.setLongDescription(getLongDescription());
        video.setShortDescription(getShortDescription());
        List<String> tags = CategorizationService.getInstance().getCategorizationTags(this);
        video.setTags(tags);
        CategorizationService.getInstance().setCategorizationToBrightcoveVideo(this, video, mapping);
        return video;
    }
}
