package example.collection;

import com.polopoly.cm.app.policy.ConfigurableContentListWrapper;
import com.polopoly.cm.client.CMException;

/**
 * ContentListWrapper adding ability to set max size for list.
 *
 */
public class ContentListWrapperLimited extends ConfigurableContentListWrapper {
    /**
     * Returns <code>false</code> since we also implement {@link #getMaxSize()} and otherwise
     * would have to manage deleting elements from the list ourselves on insert.
     */
    public boolean allowAddToFullList() throws CMException {
        return false;
    }
    
    
    
}
