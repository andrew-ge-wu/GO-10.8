package example.blog;

import com.polopoly.cm.ContentId;

public class BlogDescription
{
    private final ContentId contentId;
    private final String title;
    
    public BlogDescription(ContentId contentId,
                           String title)
    {
        this.contentId = contentId;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public ContentId getContentId() {
        return contentId;
    }

}
