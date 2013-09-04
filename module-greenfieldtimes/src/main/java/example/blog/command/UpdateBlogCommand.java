package example.blog.command;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.alias.InvalidUrlPathSegmentException;
import com.polopoly.cm.alias.UrlPathSegmentAlreadyExistsException;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;

import example.blog.BlogContext;
import example.blog.BlogForm;
import example.blog.BlogPolicy;
import example.blog.HTMLValidator;
import example.blog.RenderControllerBlog;
import example.util.Context;

public class UpdateBlogCommand extends BlogCommandBase {
    private static final Logger LOG = Logger.getLogger(UpdateBlogCommand.class.getName());

    public UpdateBlogCommand(HTMLValidator htmlValidator) {
        super(htmlValidator);
    }

    public boolean execute(Context context)
    {
        BlogContext blogContext = (BlogContext) context;
        RenderRequest renderRequest = blogContext.getRenderRequest();

        PolicyCMServer cmServer = blogContext.getPolicyCMServer();

        ContentId blogId = blogContext.getBlogContentId();

        BlogForm blogForm = populateBlogForm(renderRequest);
        ModelWrite localModel = blogContext.getTopModel().getLocal();
        localModel.setAttribute("blogForm", blogForm);

        if (!(blogFormValidator.validate(blogContext, blogForm))) {
            return false;
        }

        BlogPolicy blog = null;
        try {
            blog = (BlogPolicy) cmServer.createContentVersion(blogId.getLatestCommittedVersionId());

            blog.setName(blogForm.getBlogName());
            blog.setPathSegmentString(blogForm.getBlogAddress());
            blog.setDescription(blogForm.getBlogDescription());

            cmServer.commitContent(blog);

            blogContext.getLocalModel().setAttribute("content", blog);

        }
        catch (UrlPathSegmentAlreadyExistsException e) {
            handleError(blogContext, blog, RenderControllerBlog.WEB_ALIAS_EXISTS_ERROR);
        }
        catch (InvalidUrlPathSegmentException e) {
            handleError(blogContext, blog, RenderControllerBlog.FIELD_REQUIRED_BLOG_ADDRESS);
        }
        catch (CMException e) {
            LOG.log(Level.WARNING, "Error while updating blog content.", e);
            handleError(blogContext, blog, RenderControllerBlog.INTERNAL_SERVER_ERROR);
        }
        return true;
    }

    private void handleError(BlogContext context,
                             BlogPolicy blog,
                             String errorMessageKey)
    {
        context.addErrorShowBlogEdit(errorMessageKey);

        try {
            if (blog != null) {
                context.getPolicyCMServer().abortContent(blog);
            }
        }
        catch (CMException e) {
            LOG.log(Level.WARNING, "Error while updating blog content.", e);
        }
    }
}
