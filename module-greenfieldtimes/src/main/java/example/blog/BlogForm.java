package example.blog;

import com.polopoly.cm.ContentId;

public class BlogForm {

    private final String blogName;
    private final String blogDescription;
    private final String blogAddress;
    private final ContentId parentPage;

    public BlogForm(String blogName,
                    String blogDescription,
                    String blogAddress,
                    ContentId parentPage)
    {
        this.blogName = blogName;
        this.blogDescription = blogDescription;
        this.blogAddress = blogAddress;
        this.parentPage = parentPage;
    }

    public String getBlogName() {
        return blogName;
    }

    public String getBlogDescription() {
        return blogDescription;
    }

    public String getBlogAddress() {
        return blogAddress;
    }

    public ContentId getParentPage() {
        return parentPage;
    }
}
