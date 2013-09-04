package example.content;

import com.atex.plugins.baseline.url.UrlResolver;
import com.polopoly.metadata.MetadataAware;

/**
 * Interface for resource content.
 *
 */
public interface ResourceContent
    extends MetadataAware
{
    
    /**
     * Get thumbnail image for the resource.
     *
     * @param resolver an <code>UrlResolver</code> value
     * @return path or <code>null</code> if no thumbnail available
     */
    String getThumbnailPath(UrlResolver resolver);


    /**
     * Get preview url path for the resource.
     *
     * @param resolver an <code>UrlResolver</code> value
     * @return path or <code>null</code> if no preview available
     */
    String getPreviewPath(UrlResolver resolver);

    /**
     * Get resource byline.
     *
     * @return byline or <code>null</code> if no byline available
     */
    String getByline();
    

    /**
     * Get resource index keywords
     *
     * @return an array of keywords or an empty array if no keyword
     * present
     */
    String[] getKeywords();
}
