package example.content;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.metadata.Metadata;
import com.polopoly.metadata.MetadataAware;

/**
 * Base policy for resources.
 *
 */
public abstract class ResourceBasePolicy extends ContentBasePolicy
    implements ResourceContent
{
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    public String getByline() {
        return getChildValue("byline", null);
    }

    public String[] getKeywords() {
        String keywords = getChildValue("keywords");
        return (keywords != null && keywords.length() > 0) ? keywords.split("[ |,] *") : EMPTY_STRING_ARRAY;
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
    public void setMetadata(Metadata arg0) {
        getMetadataAware().setMetadata(arg0);
    }
}
