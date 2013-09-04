package example.blog.command;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.render.RenderRequest;

import example.blog.BlogContext;
import example.util.Command;

public abstract class BlogPostCommandBase
    implements Command
{
    protected String getBlogPostName(RenderRequest request)
    {
        return request.getParameter("blog_post_heading");
    }
    
    protected String getBlogPostText(RenderRequest request)
    {
        return request.getParameter("blog_post_text");
    }

    protected boolean imageUploadCreatedNewContentVersion(ContentId blogPostId)
    {
        return blogPostId.getVersion() != VersionedContentId.UNDEFINED_VERSION;
    }

    protected boolean isMobileMode(BlogContext blogContext)
    {
        return "mobile".equals(blogContext.getControllerContext().getMode());
    }
 }
