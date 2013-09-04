package example.blog.command;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.render.RenderRequest;

import example.blog.BlogForm;
import example.blog.BlogFormValidator;
import example.blog.HTMLValidator;
import example.blog.InvalidHTMLException;
import example.membership.RenderControllerProfileElement;
import example.util.Command;

public abstract class BlogCommandBase
    implements Command
{
    private static final Logger LOG = Logger.getLogger(BlogCommandBase.class.getName());
    protected final BlogFormValidator blogFormValidator = new BlogFormValidator();
    protected final HTMLValidator htmlValidator;

    public BlogCommandBase(HTMLValidator validator) {
        htmlValidator = validator;
    }

    protected String getBlogName(RenderRequest request)
    {
        try {
            String blogName = request.getParameter("blog_name");
            if (blogName != null) {
                return htmlValidator.stripAllHTML(blogName).trim();
            }
            return null;
        } catch (InvalidHTMLException e) {
            LOG.log(Level.WARNING, "Unable to get blog name", e);
            return null;
        }
    }

    protected String getBlogDescription(RenderRequest request)
    {
        try {
            String description = request.getParameter("blog_description");
            if (description != null) {
                return htmlValidator.stripAllHTML(description).trim();
            }
            return null;
        } catch (InvalidHTMLException e) {
            LOG.log(Level.WARNING, "Unable to get blog description", e);
            return null;
        }
    }

    protected BlogForm populateBlogForm(RenderRequest renderRequest) {
        
        String blogName = getBlogName(renderRequest);
        String blogDescription = getBlogDescription(renderRequest);
        String blogAddress = getBlogAddress(renderRequest);
        ContentId parentPage = getParentPage(renderRequest);
    
        return new BlogForm(blogName, blogDescription, blogAddress, parentPage);
    }

    public String getBlogAddress(RenderRequest request) {
        return request.getParameter(RenderControllerProfileElement.REQUEST_PARAMETER_BLOG_ADDRESS);
    }

    public ContentId getParentPage(RenderRequest renderRequest) {
        
        String parameter =
            renderRequest.getParameter(RenderControllerProfileElement.REQUEST_PARAMETER_PARENT_PAGE);
        
        if (parameter != null) {
            return ContentIdFactory.createContentId(parameter);
        } 
        return null;
    }
}
