package example.layout.element.mynewslist;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;

public class LegacyContentIdCreator
{
    public ContentId createContentId(String contentIdString)
    {
        return ContentIdFactory.createContentId(contentIdString);
    }
}
