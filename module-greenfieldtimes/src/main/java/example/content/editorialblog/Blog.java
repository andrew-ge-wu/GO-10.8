package example.content.editorialblog;

import java.util.List;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;

/**
 * Interface representing a Blog.
 */
public interface Blog {
    /**
     * Returns a list of {@link BlogYear}s for which this blog has postings.
     * 
     * @return the list of years
     * 
     * @throws CMException if an error occurs
     */
    public List<BlogYear> getBlogYears() throws CMException;

    /**
     * Returns a list containing content ids of the latest {@link BlogPosting}s
     * in this blog. Will return a maximum of 100 postings by default.
     * 
     * @return the list of posting content ids
     * 
     * @throws CMException if an error occurs
     */
    public List<ContentId> getLatestPostingIds() throws CMException;
    
    /**
     * Returns a list containing content ids of the latest {@link BlogPosting}s
     * in this blog, with a maximum size of the given limit.
     * 
     * @param limit the maximum number of postings to return
     * @return the list of posting content ids
     * 
     * @throws CMException if an error occurs
     */
    public List<ContentId> getLatestPostingIds(int limit) throws CMException;
       
    /**
     * Finds the {@link BlogMonth} representing the given year and the given
     * month, if one exists.
     * 
     * @param year the year the blog month is for, e.g. 2008
     * @param month the month the blog month is for. The first month of the year
     *        is 0 (JANUARY).
     * @return the month, if it exists in the blog. Otherwise null.
     * @see java.util.GregorianCalendar
     */
    public BlogMonth getBlogMonth(int year, int month);
    
    /**
     * Finds the {@link BlogYear} representing the given year, if one exists.
     * 
     * @param year the year the blog year is for
     * @return the year, if it exists in the blog. Otherwise null.
     * @see java.util.GregorianCalendar
     */
    public BlogYear getBlogYear(int year);
    
    /**
     * Returns the {@link BlogPosting} represented by the given content id, or
     * null if the content id does not represent a posting.
     * 
     * @param contentId the content id representing the posting
     * @return the posting
     * 
     * @throws CMException if an error occurs
     */
    public BlogPosting getBlogPosting(ContentId contentId) throws CMException;
}
