package example.content.editorialblog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ReadOnlyContentIdMap;
import com.polopoly.cm.policy.Policy;
import com.polopoly.model.ModelTypeDescription;

import example.content.ContentBasePolicy;

/**
 * Policy for an Editorial Blog.
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
public class BlogPolicy extends ContentBasePolicy
    implements ModelTypeDescription, Blog
{
    private static final Logger LOG = Logger.getLogger(BlogPolicy.class.getName());
    private static final int MAX_RESULTING_POSTINGS = 100;

    public static final String BLOG_TIMEZONE_COMPONENT = "blogTimeZone";

    private List<BlogYear> blogTree = null;
    private List<ContentId> blogPostRefs = null;

    private Pattern postingListNamePattern = null;

    private TimeZone blogTimeZone = null;

    @Override
    protected void initSelf()
    {
        super.initSelf();

        postingListNamePattern = Pattern.compile("postings/([0-9]{4})/([0-9]{2})");

        try {
            String blogTimeZoneComponent = getComponent(BLOG_TIMEZONE_COMPONENT);

            if (blogTimeZoneComponent != null) {
                blogTimeZone = TimeZone.getTimeZone(blogTimeZoneComponent);
            }
        } catch (CMException cme) {
            LOG.log(Level.WARNING, "Error while retrieving Blog Time information!", cme);
        }

        try {
            getBlogYears();
        } catch (CMException cme) {
            LOG.log(Level.WARNING, "Cannot get blog posting years", cme);
        }

        getBlogPostingRefs();
    }

    /**
     * Gets the years that the blog has postings for.
     *
     * @return a list with the blogs blog years.
     * @throws com.polopoly.cm.client.CMException
     */
    public List<BlogYear> getBlogYears()
        throws CMException
    {
        if (blogTree == null) {
            blogTree = new ArrayList<BlogYear>();
            List<String> contentRefGroupNames = getSortedContentReferenceGroupNames();

            for (String groupName : contentRefGroupNames) {
                Matcher matcher = postingListNamePattern.matcher(groupName);

                if (matcher.matches()) {
                    String year = matcher.group(1);

                    int yearNum = Integer.parseInt(year);

                    // Find the months to go in the blog year object.
                    List<BlogMonth> blogMonths = getBlogPostingMonths(yearNum);

                    BlogYear blogYear = new BlogYear(yearNum, blogMonths);

                    if (!blogTree.contains(blogYear)) {
                        blogTree.add(blogYear);
                    }
                }
            }
        }

        return blogTree;
    }

    String getBlogTimeZoneComponent()
        throws CMException
    {
        return getComponent(BLOG_TIMEZONE_COMPONENT);
    }

    void setBlogTimeZoneComponent(final String blogZoneComponent)
        throws CMException
    {
        setComponent(BLOG_TIMEZONE_COMPONENT, blogZoneComponent);
    }

    public TimeZone getBlogTimeZone()
    {
        if (blogTimeZone != null) {
            return blogTimeZone;
        }

        return TimeZone.getDefault();
    }

    /**
     * Gets the month for a given year and blog.
     *
     * @param year the year of interest, e.g. 2008
     * @return a List with BlogMonth's
     * @throws com.polopoly.cm.client.CMException
     */
    private List<BlogMonth> getBlogPostingMonths(int year)
        throws CMException
    {
        List<BlogMonth> months = new ArrayList<BlogMonth>();
        List<String> contentRefGroupNames = getSortedContentReferenceGroupNames();

        for (String groupName : contentRefGroupNames) {
            Matcher matcher = postingListNamePattern.matcher(groupName);

            if (matcher.matches()) {
                String matchedYear = matcher.group(1);
                String matchedMonth = matcher.group(2);

                int matchedMonthNum = Integer.parseInt(matchedMonth);

                if (!Integer.toString(year).equals(matchedYear)) {
                    continue;
                }

                List<ContentId> postings = getBlogPostingsForMonth(year, matchedMonthNum);
                BlogMonth blogMonth = new BlogMonth(year, matchedMonthNum, getGregorianCalendar(), postings);

                if (!months.contains(blogMonth)) {
                    months.add(blogMonth);
                }
            }
        }

        return months;
    }

    private void getBlogPostingRefs()
    {
        blogPostRefs = new ArrayList<ContentId>();
        for (BlogYear year: blogTree) {
            blogPostRefs.addAll(year.getPostingIds());
        }
    }

    private List<String> getSortedContentReferenceGroupNames()
        throws CMException
    {
        List<String> contentRefGroupNames =
            Arrays.asList(getContent().getContentReferenceGroupNames());

        Collections.sort(contentRefGroupNames);
        Collections.reverse(contentRefGroupNames);

        return contentRefGroupNames;
    }

    /**
     * Get a list of ContentId's representing the postings for a
     * given year-month.
     *
     * @param year the year get posting for, e.g. 2008
     * @param month the month in the year to get posting for. The first month of
     *        the year is 0 (JANUARY).
     * @return a list of ContentId's
     * @throws com.polopoly.cm.client.CMException
     */
    private List<ContentId> getBlogPostingsForMonth(int year, int month)
        throws CMException
    {
        String monthString = null;

        if (month < 10) {
            monthString = "0" + month;
        } else {
            monthString = Integer.toString(month);
        }

        List<ContentId> postings = new ArrayList<ContentId>();
        String contentListName = "postings/" + year + "/" + monthString;

        ReadOnlyContentIdMap contentIdMap =
                new ReadOnlyContentIdMap(getContent(), contentListName, new TreeMap<String, ContentId>(
                        new LongComparator()));

        addPostingsToList(postings, contentIdMap.values().iterator());

        return postings;
    }

    private class LongComparator implements Comparator<String>
    {
        public int compare(String arg0, String arg1)
        {
            return (new Long(arg0).compareTo(new Long(arg1)));
        }
    }

    private void addPostingsToList(List<ContentId> list,
                                   Iterator<ContentId> postings)
    {
        while (postings.hasNext()) {
            list.add(0, postings.next());
        }
    }

    /**
     * Gets the latest posting with a maximum count of 100.
     *
     * @return a list of ContentId's
     * @throws com.polopoly.cm.client.CMException
     */
    public List<ContentId> getLatestPostingIds()
        throws CMException
    {
        return getLatestPostingIds(MAX_RESULTING_POSTINGS);
    }

    /**
     * Gets the latest posting with a maximum count of limit.
     *
     * @param limit the maximum number of postings to be returned
     * @return the postings
     * @throws com.polopoly.cm.client.CMException
     */
    public List<ContentId> getLatestPostingIds(int limit)
        throws CMException
    {
        return blogPostRefs.subList(0, Math.min(limit, blogPostRefs.size()));
    }

    /**
     * Gets this object for easy access in rendering layer.
     *
     * @return this object.
     */
    public Object getBlogBean()
    {
        return this;
    }

    /**
     * Gets a BlogMonth.
     *
     * @param year the year, e.g. 2008
     * @param month the month. The first month of the year is 0 (JANUARY).
     */
    public BlogMonth getBlogMonth(int year, int month)
    {
        BlogYear blogYear = getBlogYear(year);

        for (BlogMonth blogMonth : blogYear.getMonths()) {
            if (blogMonth.getMonth() == month) {
                return blogMonth;
            }
        }

        return null;
    }

    /**
     * Gets a BlogYear
     *
     * @param year the year, e.g. 2008
     * @return the found BlogYear, or <code>null</code>
     */
    public BlogYear getBlogYear(int year)
    {
        for (BlogYear blogYear: blogTree) {
            if (blogYear.getYear() == year) {
                return blogYear;
            }
        }

        return null;
    }

    /**
     * Gets the BlogPosting with a given ContentId.
     *
     * @param contentId the ContentId
     * @return the BlogPosting
     * @throws com.polopoly.cm.client.CMException
     */
    public BlogPosting getBlogPosting(ContentId contentId)
        throws CMException
    {
        Policy policy = getCMServer().getPolicy(contentId);

        if (policy instanceof BlogPosting) {
            return (BlogPosting) policy;
        }

        return null;
    }

    /**
     * Get a {@link GregorianCalendar} instance based on the platform
     * {@link TimeZone} and the US locale.
     * @return a {@link GregorianCalendar} instance.
     */
    public Calendar getGregorianCalendar()
    {
        return GregorianCalendar.getInstance(getBlogTimeZone(), Locale.US);
    }
}
