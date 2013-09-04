package example.content.editorialblog;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;

/**
 * Class representing a Blog Month.
 */
public class BlogMonth
{
    private final int year;
    private final int month;

    private final Date date;

    private List<ContentId> postings;

    /**
     * Constructs a new BlogMonth instance using the
     * given {@link Blog}, year and month.
     *
     * @param year the year this month belongs to, e.g. 2008
     * @param month the month this month belongs to. The first month of the year
     *              is 0 (JANUARY).
     *
     * @param postings the list of postings for this blog month
     *
     * @see java.util.GregorianCalendar
     */
    public BlogMonth(int year, int month, Calendar calendarToUse, List<ContentId> postings)
    {
        this.month = month;
        this.year = year;
        this.postings = postings;

        Calendar calendar = calendarToUse;

        calendar.set(GregorianCalendar.YEAR, year);
        calendar.set(GregorianCalendar.MONTH, month);

        calendar.set(GregorianCalendar.DAY_OF_MONTH, 1);

        calendar.set(GregorianCalendar.HOUR, 0);
        calendar.set(GregorianCalendar.MINUTE, 0);
        calendar.set(GregorianCalendar.SECOND, 0);
        calendar.set(GregorianCalendar.MILLISECOND, 0);

        date = calendar.getTime();
    }

    /**
     * Returns the month this month represents. The first month of the year is 0
     * (JANUARY).
     *
     * @return the month
     */
    public int getMonth()
    {
        return month;
    }

    /**
     * Returns the year this month represents, e.g. 2008.
     *
     * @return the year
     */
    public int getYear()
    {
        return year;
    }

    /**
     * Returns the date (year, month) this month represents.
     *
     * @return the date
     */
    public Date getDate()
    {
        return date;
    }

    /**
     * Returns a list of content ids representing {@link BlogPosting}s from
     * the owning {@link Blog} given the time period this month represents.
     *
     * @return the list of content ids
     * @throws CMException
     */
    public List<ContentId> getPostingIds()
        throws CMException
    {
        return postings;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;

        int result = 1;
        result = prime * result + month;

        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof BlogMonth)) {
            return false;
        }

        return ((BlogMonth) obj).month == this.month;
    }
}
