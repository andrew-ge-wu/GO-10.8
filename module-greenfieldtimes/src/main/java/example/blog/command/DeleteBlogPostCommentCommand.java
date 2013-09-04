package example.blog.command;

import com.polopoly.cm.ContentId;

import example.blog.BlogContext;
import example.blog.CmRenderContext;
import example.util.Context;

public class DeleteBlogPostCommentCommand extends DeleteCommentCommand
{

    public void showError(Context context, String errorKey)
    {
        ((BlogContext) context).addCommentErrorShowBlogPostView(errorKey);
    }
    
    protected boolean isPostingToThisContent(CmRenderContext cmContext)
    {
        ContentId idFromPath = ((BlogContext) cmContext).getBlogPostContentIdFromPath();
        return idFromPath != null && idFromPath.equalsIgnoreVersion(getCommentsIdPostParameter(cmContext));
    }
}
