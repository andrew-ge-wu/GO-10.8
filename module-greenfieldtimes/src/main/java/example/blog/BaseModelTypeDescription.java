package example.blog;

import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;

public interface BaseModelTypeDescription {
    String getName() throws CMException;
    VersionedContentId getContentId();
}
