package example.content.editorialblog;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;

/**
 * Class representing a Blog Year.
 */
public class BlogYear
{
    private final static Logger LOG = Logger.getLogger(BlogYear.class.getName());

    private final int year;

    private int postingCount;

    private List<BlogMonth> months;
    private List<ContentId> postings;

    /**
     * Constructs a new BlogYear instance using the
     * given {@link Blog} and the given year.
     *
     * @param year the year this month belongs to
     * @param blogMonths the list of blog months for this blog year
     */
    public BlogYear(int year, List<BlogMonth> blogMonths)
    {
        this.year = year;
        this.months = blogMonths;
        this.postingCount = 0;

        this.postings = null;

        try {
            fetchAllPostingIds();
            this.postingCount = getPostingIds().size();
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Could not get post count for month", e);
        }
    }

    /**
     * Returns the year this month represents.
     *
     * @return the year
     */
    public int getYear()
    {
        return year;
    }

    /**
     * Returns a list of {@link BlogMonth}s contained in this year.
     *
     * @return the list of months
     * @throws CMException
     */
    public List<BlogMonth> getMonths()
    {
        return months;
    }

    /**
     * Returns a list of content ids representing {@link BlogPosting}s from the
     * owning {@link Blog} given the time period this year represents.
     *
     * @return the list of content ids
     * @throws CMException
     */
    public List<ContentId> getPostingIds()
    {
        return postings;
    }

    private void fetchAllPostingIds()
        throws CMException
    {
        if (postings == null) {
            postings = new ArrayList<ContentId>();

            for (BlogMonth month : months) {
                postings.addAll(month.getPostingIds());
            }
        }
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + year;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BlogYear other = (BlogYear) obj;
        if (year != other.year)
            return false;
        return true;
    }

    public int getPostingCount()
    {
        return postingCount;
    }
}
