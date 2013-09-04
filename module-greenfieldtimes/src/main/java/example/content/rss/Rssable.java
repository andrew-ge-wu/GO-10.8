package example.content.rss;

import com.polopoly.cm.ContentId;
import com.polopoly.siteengine.standard.feed.Feedable;

/**
 * Interface to mark content that can be added in a RSS feed.
 */
public interface Rssable
    extends Feedable
{
    /**
     * @return description of the feed entry
     */
    public String getItemDescription();

    /**
     * @return contentId for this content
     */
    public ContentId getItemContentId();

    /**
     * @return the path for the content
     */
    public ContentId[] getItemParentIds();
}
