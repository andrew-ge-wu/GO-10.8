package example.content.article;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.DefaultMajorNames;
import com.polopoly.cm.PublishingDateTime;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.inbox.InboxFlags;
import com.polopoly.cm.app.policy.ContentListInsertionHook;
import com.polopoly.cm.app.policy.DateTimePolicy;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListRead;
import com.polopoly.cm.collections.ContentListSimple;
import com.polopoly.cm.collections.ContentListUtil;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.policy.UserDataPolicy;
import com.polopoly.community.comment.CommentList;
import com.polopoly.metadata.Dimension;
import com.polopoly.metadata.Entity;
import com.polopoly.metadata.Metadata;
import com.polopoly.metadata.MetadataAware;
import com.polopoly.metadata.util.MetadataUtil;
import com.polopoly.metadata.util.MetadataUtil.Filtering;
import com.polopoly.paywall.ContentBundle;
import com.polopoly.paywall.PremiumContentAware;
import com.polopoly.paywall.PremiumContentSelectPolicy;
import com.polopoly.siteengine.layout.ContentRepresentative;
import com.polopoly.siteengine.standard.feed.Feedable;
import com.polopoly.textmining.TextRepresentation;

import example.content.ContentBasePolicy;
import example.content.rss.Rssable;
import example.layout.element.comments.CommentsElementPolicy;
import example.layout.element.teaser.TeaserPolicy;

/**
 * Policy for the standard article template.
 */
public class StandardArticlePolicy extends ContentBasePolicy
    implements StandardArticleModelTypeDescription,
               Rssable, PublishingDateTime,
               TextRepresentation, MetadataAware,
               ContentListInsertionHook, ContentRepresentative,
               PremiumContentAware
{
    private static final String GROUP_COMMENTS = "comments";

    private static String COMPONENT_LEAD = "lead";
    private static String COMPONENT_AUTHOR = "author";
    private static String COMPONENT_PRIORITY = "priority";

    private static Logger LOG = Logger.getLogger(StandardArticlePolicy.class.getName());

    public int getPriority()
    {
        Integer priority = Integer.parseInt(getChildValue(COMPONENT_PRIORITY, "0"));
        return priority;
    }

    public void setPriority(int priority)
    {
        try {
            setChildValue(COMPONENT_PRIORITY, Integer.toString(priority));
        } catch (CMException e) {
            throw new CMRuntimeException(e);
        }
    }

    public String getAuthor()
        throws CMException
    {
        String author = getChildValue(COMPONENT_AUTHOR, "");

        if (author.length() == 0) {
            UserDataPolicy user = getCreator();

            if (user != null) {
                if (user.getFirstname() != null) {
                    author = user.getFirstname();
                }

                if (user.getSurname() != null) {
                    author += " " + user.getSurname();
                }
            }
        }

        return author;
    }

    /**
     * @see Rssable
     */
    public String getItemDescription()
    {
        return getChildValue(COMPONENT_LEAD);
    }

    /**
     * The date should be dependant on workflow state/time state
     *
     * @see Rssable
     */
    public Date getItemPublishedDate()
    {
        Date ret = null;
        ret = new Date(getPublishingDateTime());

        return ret;
    }

    /**
     * @see Feedable
     */
    public String getItemTitle()
    {
        String ret = null;

        try {
            ret = getName();
        } catch (CMException cme) {
            LOG.log(Level.WARNING, "Failed to get Feed Item title", cme);
        }

        return ret;
    }

    @Override
    public void postCreateSelf()
        throws CMException
    {
        // All articles should be in the Inbox by default.
        // If integrating with e.g. a print system, you might want to set this only on articles arriving from the print system.
        new InboxFlags().setShowInInbox(this, true);
    }

    /**
     * @see Rssable
     */
    public ContentId getItemContentId()
    {
        return getContentId();
    }

    /**
     * @see Rssable
     */
    public ContentId[] getItemParentIds()
    {
        try {
            return getParentIds();
        } catch(CMException e) {
            return new ContentId[] { getContentId().getContentId() };
        }
    }

    @Override
    public ContentList getContentList(String contentListName)
        throws CMException
    {
        ContentList contentList = null;

        if (GROUP_COMMENTS.equals(contentListName)) {
            CommentsElementPolicy commentsElement = findCommentsElementFromMainSlot();

            List<ContentId> contentIdList;
            if (commentsElement != null) {
                CommentList commentList = commentsElement.getCommentList();
                contentIdList = commentList.getSlice(0, Integer.MAX_VALUE).getContentIds();
            } else {
                contentIdList = Collections.emptyList();
            }

            contentList = ContentListUtil
                                .unmodifiableContentList(
                                        new ContentListSimple(contentIdList, GROUP_COMMENTS));
        } else {
            contentList = super.getContentList(contentListName);
        }

        return contentList;
    }

    private CommentsElementPolicy findCommentsElementFromMainSlot()
        throws CMException
    {
        ContentListRead slotElements = getContentList("elements/slotElements");

        if (slotElements != null) {
            for (Iterator<?> iterator = slotElements.getListIterator(); iterator.hasNext(); ) {
                ContentReference contentReference = (ContentReference) iterator.next();
                ContentId elementId = contentReference.getReferredContentId();
                Policy element = getCMServer().getPolicy(elementId);

                if (element instanceof CommentsElementPolicy) {
                    return (CommentsElementPolicy) element;
                }
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see com.polopoly.cm.PublishingDateTime#getPublishingDateTime()
     */
    public long getPublishingDateTime()
    {
        long publishingDateTime = getContentCreationTime();

        try {
            DateTimePolicy policy = (DateTimePolicy) getChildPolicy("publishedDate");

            if (policy != null) {
                publishingDateTime = policy.getTimeMillis();
            }
        } catch (CMException e) {}

        return publishingDateTime;
    }

    public String getTextRepresentation()
    {
        String text = null;

        try {
            SingleValuePolicy titlePolicy = (SingleValuePolicy)getChildPolicy("name");
            SingleValuePolicy bodyPolicy = (SingleValuePolicy)getChildPolicy("body");
            SingleValuePolicy leadPolicy = (SingleValuePolicy)getChildPolicy("lead");

            String title = titlePolicy.getValue();

            String lead = leadPolicy.getValue();
            String body = bodyPolicy.getValue();

            text =
                (title != null ? title + " \n\n " :"")  +
                (lead != null ? lead  + " \n\n " :"") +
                (body != null ? body :"");
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Failed to get body policy", e);
        }

        return text;
    }

    private MetadataAware getMetadataAware()
    {
        MetadataAware categorizationProvider;
        try {
            categorizationProvider = (MetadataAware) getChildPolicy("categorization");
        } catch (CMException e) {
            throw new CMRuntimeException(e);
        }
        if (categorizationProvider == null) {
            try {
                throw new CMRuntimeException("No metadata for policy of type " + getExternalId().getExternalId());
            } catch (CMException e) {
                throw new CMRuntimeException("No metadata for policy of type " + getClass().getName());
            }
        }
        return categorizationProvider;
    }

    @Override
    public Metadata getMetadata() {
        return getMetadataAware().getMetadata();
    }

    @Override
    public void setMetadata(Metadata metadata) {
        getMetadataAware().setMetadata(metadata);
    }

    public String getPrimaryLocation()
        throws CMException
    {
        return getPrimaryCategoryFromTagDimension("department.categorydimension.tag.Location");
    }

    public String getPrimaryTag()
        throws CMException
    {
        return getPrimaryCategoryFromTagDimension("department.categorydimension.tag.Tag");
    }

    private String getPrimaryCategoryFromTagDimension(String dimensionName)
        throws CMException
    {
        Metadata categorization = getMetadata();
        if (categorization != null) {
            Dimension dimension = categorization.getDimensionById(dimensionName);
            if (dimension != null) {
                Iterator<List<Entity>> paths = MetadataUtil.traverseEntityPaths(dimension, Filtering.ONLY_LEAVES).iterator();
                if (paths.hasNext()) {
                    List<Entity> path = paths.next();
                    StringBuilder sb = new StringBuilder();
                    boolean first = true;
                    for (Entity entity : path) {
                        if (first) {
                            first = false;
                        } else {
                            sb.append("/");
                        }
                        sb.append(entity.getName());
                    }
                    return sb.toString();
                }
            }
        }
        return null;
    }

    public ContentId getDefaultReferredImage()
        throws CMException
    {
        ContentList topImages = getContentList("topimages");
        if(topImages.size() > 0) {
            return topImages.getEntry(0).getReferredContentId();
        }
        ContentList images = getContentList("images");
        if (images.size() > 0) {
            return images.getEntry(0).getReferredContentId();
        }
        return null;
    }

    public ContentId onInsert(VersionedContentId contentToInsertInto, String contentListName, int index)
        throws CMException
    {
        PolicyCMServer cmServer = getCMServer();
        int layoutElementMajor = cmServer.getMajorByName(DefaultMajorNames.LAYOUTELEMENT);
        TeaserPolicy teaser = (TeaserPolicy)
            cmServer.createContent(layoutElementMajor, contentToInsertInto, TeaserPolicy.TEASER_INPUT_TEMPLATE_ID);
        teaser.setArticleId(getContentId().getContentId());
        cmServer.commitContent(teaser);
        return teaser.getContentId().getContentId();
    }

    public List<ContentId> getRepresentedContent()
    {
        List<ContentId> containedIds = new ArrayList<ContentId>();

        addFromContentList(containedIds, "images");
        addFromContentList(containedIds, "topimages");
        addFromContentList(containedIds, "elements/slotElements");
        addFromContentList(containedIds, "rightColumn/slotElements");

        return containedIds;
    }

    private void addFromContentList(List<ContentId> containedIds, String contentListName)
    {
        try {
            ContentList images = getContentList(contentListName);
            ListIterator<ContentReference> iterator = images.getListIterator();
            while (iterator.hasNext()) {
                ContentReference ref = iterator.next();
                if (ref != null && ref.getReferredContentId() != null) {
                    containedIds.add(ref.getReferredContentId());
                }
            }
        } catch(CMException e) {
            LOG.log(Level.WARNING, "Failed to get content list " + contentListName + " from standard article " + getContentId().getContentIdString(), e);
        }
    }

    @Override
    public boolean isPremiumContent()
        throws CMException
    {
        return ((PremiumContentSelectPolicy) getChildPolicy("premiumContent")).isPremiumContent();
    }

    @Override
    public Collection<ContentBundle> getPremiumBundles()
        throws CMException
    {
        return ((PremiumContentSelectPolicy) getChildPolicy("premiumContent")).getPremiumBundles();
    }

    /**
     * Convenience method to get premium bundle names for presentation purposes.
     * @return Pipe separated string of premium bundle names.
     * @throws CMException if unable to fetch data.
     */
    public String getReadablePremiumBundles()
        throws CMException
    {
        Set<String> toReturn = new HashSet<String>();
        for (ContentBundle contentBundle : getPremiumBundles()) {
            try {
                toReturn.add(contentBundle.getName());
            } catch (CMException e) {
                LOG.log(Level.WARNING, "Unable to fetch data", e);
            }
        }
        return StringUtils.join(toReturn.iterator(), " | ");
    }
}
