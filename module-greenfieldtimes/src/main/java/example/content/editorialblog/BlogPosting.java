package example.content.editorialblog;

/**
 * Interface representing a Blog Posting.
 */
public interface BlogPosting {

    /**
     * Returns the year in which this  posting was published.
     * 
     * @return the year
     * @see java.util.GregorianCalendar
     */
    public int getPublishYear();
    
    /**
     * Returns the month in which this posting was published.
     * 
     * @return the month. The first month of the year is 0 (JANUARY).
     * @see java.util.GregorianCalendar
     */
    public int getPublishMonth();
    
    /**
     * Returns the title of this posting using a maximum length of the given
     * maxLength. If title is longer it will be cut and padded with the given
     * padding.
     * 
     * @param maxLength the max number of chars in the title
     * @param padding the padding to use when title is too long
     * @return the possibly shorter and padded title of this posting
     */
    public String getShortTitle(int maxLength, String padding);
    
    /**
     * Returns this blog posting as a bean object. Necessary since we sometimes
     * want to execute "non-bean" methods from for example Velocity.
     * 
     * @return this blog posting as a bean
     */
    public Object getBlogPostingBean();
    
}
