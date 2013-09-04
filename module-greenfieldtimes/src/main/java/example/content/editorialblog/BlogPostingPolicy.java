package example.content.editorialblog;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.DefaultMajorNames;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.PublishingDateTime;
import com.polopoly.cm.app.policy.DateTimePolicy;
import com.polopoly.cm.app.util.LockUtil;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.policy.PrepareResult;
import com.polopoly.cm.policy.UserDataPolicy;
import com.polopoly.model.ModelTypeDescription;
import com.polopoly.siteengine.standard.feed.Feedable;

import example.content.ContentBasePolicy;
import example.content.rss.Rssable;

/**
 * Policy for an Editorial Blog Posting.
 *
 * <p>
 * WARNING: The Editorial Blog functionality contained in Greenfield Times is
 * intended to support a reasonably small flow of editorial blog content. It is
 * NOT intended as a large-scale publishing tool where the amount of postings
 * for any single blog becomes more than trivial.
 * </p>
 *
 * <p>
 * An individual blog should not be used for more than a few hundred postings.
 * Anything more will inevitably lead to a rapidly decreasing performance, mainly
 * experienced while saving new blog postings.
 * </p>
 */
public class BlogPostingPolicy extends ContentBasePolicy
    implements ModelTypeDescription, Rssable, BlogPosting, PublishingDateTime
{
    private final static Logger LOG = Logger.getLogger(BlogPostingPolicy.class.getName());

    public static final String COMMENT = "comment";
    public static final String COMMENTS_GROUP = "comments";
    private static final String COMPONENT_AUTHOR = "author";

    private TimeZone blogTimeZone;

    @Override
    protected void initSelf()
    {
        super.initSelf();

        BlogPolicy blogPolicy = getBlogPolicy();

        if (blogPolicy != null) {
            blogTimeZone = blogPolicy.getBlogTimeZone();
        }

        if (blogTimeZone == null) {
            LOG.log(Level.WARNING, "No Blog Time information found, using platform defaults!");
            blogTimeZone = TimeZone.getDefault();
        }
    }

    private BlogPolicy getBlogPolicy()
    {
        ContentId parentId = getContent().getSecurityParentId();

        if (parentId != null) {
            try {
                Policy parentPolicy = getCMServer().getPolicy(parentId);

                if (parentPolicy != null && parentPolicy instanceof BlogPolicy) {
                    return (BlogPolicy) parentPolicy;
                }
            } catch (CMException e) {
                LOG.log(Level.WARNING, "Error while retrieving Blog policy!", e);
            }
        }

        return null;
    }

    /**
     * Standard Orchid method used to make sure that the Blog is
     * available for editing.
     *
     * @return the standard PrepareResult
     * @throws com.polopoly.cm.client.CMException
     */
    @Override
    public PrepareResult prepareSelf()
        throws CMException
    {
        PrepareResult result = super.prepareSelf();
        addToParent(result);

        return result;
    }

    /**
     * Standard Orchid method used to ensure that comments are setup
     * properly for this posting.
     *
     * @throws com.polopoly.cm.client.CMException
     */
    @Override
    public void preCommitSelf()
        throws CMException
    {
        super.preCommitSelf();

        PolicyCMServer cmServer = getCMServer();

        // Check if we already have a comments element for this posting
        ContentId commentId = getContentReference(COMMENTS_GROUP, COMMENT);

        if (commentId == null) {
            // Create a content comments element

            int major = cmServer.getMajorByName(DefaultMajorNames.LAYOUTELEMENT);
            ContentId itContentId = new ExternalContentId("example.CommentsElement");

            Policy commentsElementPolicy = cmServer.createContent(major, itContentId);
            commentsElementPolicy.getContent().setSecurityParentId(getContentId());

            cmServer.commitContent(commentsElementPolicy);

            // insert it as a content reference
            setContentReference(COMMENTS_GROUP, COMMENT,
                                commentsElementPolicy.getContentId().getContentId());
        }
    }

    public TimeZone getBlogTimeZone()
    {
        return blogTimeZone;
    }

    public long getPublishingDateTime()
    {
        return getPublishDate().getTime();
    }

    public Date getPublishDate()
    {
        try {
            Policy publishDatePolicy = getChildPolicy("publishDate");

            if (publishDatePolicy instanceof DateTimePolicy) {
                return ((DateTimePolicy) publishDatePolicy).getDate();
            }
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Could not retrieve publication date.", e);
        }

        return new Date(getContentCreationTime());
    }

    private void addToParent(PrepareResult result)
    {
        PolicyCMServer cmServer = getCMServer();

        BlogPolicy blogPolicy = getBlogPolicy();
        if (blogPolicy == null) {
            result.setError(true);
            result.setMessage("Security parent must be a " + BlogPolicy.class.getName());
            return;
        }

        Calendar calendar = blogPolicy.getGregorianCalendar();
        calendar.setTime(getPublishDate());

        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month = String.valueOf(calendar.get(Calendar.MONTH));

        if (month.length() == 1) {
            month = "0" + month;
        }

        ContentId blogId = getContent().getSecurityParentId();
        String contentListName = "postings/" + year + "/" + month;

        try {
            boolean needAddToParent = !hasReferenceToThis(blogPolicy, contentListName);

            if (needAddToParent) {
                if (!LockUtil.canCallerGetLock(blogPolicy.getContentId(), getCMServer().getCurrentCaller(), getCMServer())) {
                    result.setError(true);
                    result.setMessage("Can not get lock on blog content. "
                            + "Please wait some time, then retry operation.");
                    return;
                }

                BlogPolicy writableBlogPolicy = (BlogPolicy)
                    cmServer.createContentVersion(blogId.getLatestCommittedVersionId());

                if (hasReferenceToThis(writableBlogPolicy, contentListName)) {
                    cmServer.abortContent(writableBlogPolicy);
                } else {
                    clearReferencesToThis(writableBlogPolicy);
                    long publishTime = getPublishingDateTime();

                    writableBlogPolicy.setContentReference(contentListName,
                                                           String.valueOf(publishTime),
                                                           getContentId().getContentId());

                    cmServer.commitContent(writableBlogPolicy);
                }
            }
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Error while adding posting " + getContent() + " to parent" + blogId, e);
            result.setError(true);
            result.setMessage("Unable to add posting to blog. " + "Please wait some time, then retry the operation");
        }
    }

    private void clearReferencesToThis(ContentPolicy parentPolicy)
        throws CMException
    {
        String[] groupNames = parentPolicy.getContentReferenceGroupNames();

        if (groupNames != null) {
            for (int i = 0; i < groupNames.length; i++) {
                if (groupNames[i].startsWith("postings/")) {
                    String[] refNames = parentPolicy.getContentReferenceNames(groupNames[i]);
                    for (int j = 0; j < refNames.length; j++) {
                        ContentId id = parentPolicy.getContentReference(groupNames[i], refNames[j]);
                        if (getContentId().equalsIgnoreVersion(id)) {
                            parentPolicy.setContentReference(groupNames[i], refNames[j], null);
                        }
                    }
                }
            }
        }
    }


    private boolean hasReferenceToThis(ContentPolicy parentPolicy, String referenceGroup)
        throws CMException
    {
        String[] refNames = parentPolicy.getContentReferenceNames(referenceGroup);
        for (int j = 0; j < refNames.length; j++) {
            ContentId id = parentPolicy.getContentReference(referenceGroup, refNames[j]);
            if (getContentId().equalsIgnoreVersion(id)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the Author.
     *
     * @return the author of the posting
     * @throws com.polopoly.cm.client.CMException
     */
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
     * Gets the publish month.
     *
     * @return the month that the posting was published.
     */
    public int getPublishMonth()
    {
        BlogPolicy blogPolicy = getBlogPolicy();
        Calendar calendar = blogPolicy.getGregorianCalendar();
        calendar.setTime(getPublishDate());

        return calendar.get(GregorianCalendar.MONTH);
    }

    /**
     * Gets the Publish Year.
     *
     * @return the year that the posting was published.
     */
    public int getPublishYear()
    {
        BlogPolicy blogPolicy = getBlogPolicy();
        Calendar calendar = blogPolicy.getGregorianCalendar();
        calendar.setTime(getPublishDate());

        return calendar.get(GregorianCalendar.YEAR);
    }

    /**
     * Truncates the title if needed and adds the right-padding. The user has to
     * take into account the space of the padding.
     *
     * @param maxLength
     *   The number of characters to retain
     *
     * @param rightPadding
     *   The padding the right end after truncation, if
     *   <code>null</code> then no padding is performed.
     *
     * @return the short title
     */
    public String getShortTitle(int maxLength, String rightPadding)
    {
        String title = null;

        try {
            title = getName();

            if (title.length() > maxLength) {
                title = truncateProper(title, maxLength);

                if (rightPadding != null) {
                    title = title + rightPadding;
                }
            }
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Failed to get Short title", e);
        }

        return title;
    }

    String truncateProper(String str, int maxLength)
    {
        StringBuffer shortTitle = new StringBuffer(maxLength + 10);
        RegexTokenizer tokenizer = new RegexTokenizer(str, "\\W", true);

        while (tokenizer.hasNext()) {
            String nextToken = tokenizer.nextToken();
            if (shortTitle.length() + nextToken.length() > maxLength) {
                break;
            }
            shortTitle.append(nextToken);
        }

        return shortTitle.toString().trim();
    }

    public Object getBlogPostingBean()
    {
        return this;
    }

    public Date getItemPublishedDate()
    {
        return getPublishDate();
    }

    /**
     * @see Feedable
     */
    public String getItemTitle()
    {
        String title = null;

        try {
            title = getName();
        } catch (CMException cme) {
            LOG.log(Level.WARNING, "Failed to retrieve Feed Item title", cme);
        }

        return title;
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

    /**
     * @see Rssable
     */
    public String getItemDescription()
    {
        return null;
    }

    public ContentId getCommentsElementId() throws CMException
    {
        return getContentReference(COMMENTS_GROUP, COMMENT);
    }
}
