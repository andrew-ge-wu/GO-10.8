package example.blog.command;

import com.polopoly.cm.ContentId;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;

import example.blog.BlogContext;
import example.blog.RenderControllerBlog;
import example.util.Command;
import example.util.Context;

public class ViewSplitter implements Command
{
    private final Command _viewBlogChain;
    private final Command _editBlogChain;
    private final Command _viewBlogPostChain;
    private final Command _editBlogPostChain;
    private final Command _createBlogPostChain;

    public ViewSplitter(Command viewBlogChain,
                        Command editBlogChain,
                        Command viewBlogPostChain,
                        Command createBlogPostChain,
                        Command editBlogPostChain)
    {
        _viewBlogChain = viewBlogChain;
        _editBlogChain = editBlogChain;
        _viewBlogPostChain = viewBlogPostChain;
        _createBlogPostChain = createBlogPostChain;
        _editBlogPostChain = editBlogPostChain;
    }

    public boolean execute(Context context)
    {
        BlogContext blogContext = (BlogContext) context;
        ModelWrite localModel = blogContext.getLocalModel();
        RenderRequest request = blogContext.getRenderRequest();
        
        if (null != request.getParameter(RenderControllerBlog.PARAMETER_EDIT_BLOG)) {
            localModel.setAttribute(RenderControllerBlog.IS_BLOG_EDIT, Boolean.TRUE);
            return _editBlogChain.execute(context);
        }
        
        if (null != request.getParameter(RenderControllerBlog.PARAMETER_CREATE_POST)) {
            localModel.setAttribute(RenderControllerBlog.IS_BLOG_POST_CREATE, Boolean.TRUE);
            return _createBlogPostChain.execute(context);
        }
        
        ContentId editPostId = blogContext.getEditPostId();
        if (null != editPostId) {
            localModel.setAttribute(RenderControllerBlog.IS_BLOG_POST_EDIT, Boolean.TRUE);
            localModel.setAttribute(RenderControllerBlog.BLOG_POST_ID, editPostId.getContentId().getContentIdString());
            return _editBlogPostChain.execute(context);
        }
        
        ContentId blogPostIdFromPath = blogContext.getBlogPostContentIdFromPath();
        if (null != blogPostIdFromPath) {
            localModel.setAttribute(RenderControllerBlog.IS_SINGLE_POST_VIEW, Boolean.TRUE);
            localModel.setAttribute(RenderControllerBlog.BLOG_POST_ID, blogPostIdFromPath);
            return _viewBlogPostChain.execute(context);
        }
        
        return _viewBlogChain.execute(context);
    }

}
