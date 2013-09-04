package example.blog.command;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.community.comment.CommentList;
import com.polopoly.community.comment.CommentListFactory;

import example.blog.AntiSamyHTMLValidator;
import example.blog.BlogContext;
import example.blog.CmRenderContext;
import example.util.Context;

public class AddBlogPostCommentCommand extends AddCommentCommand
{
    public AddBlogPostCommentCommand(AntiSamyHTMLValidator validator)
    {
        super(validator);
    }

    public void showError(Context context, String errorKey)
    {
        ((BlogContext) context).addCommentErrorShowBlogPostView(errorKey);
    }

    public CommentList getCommentList(Context context)
        throws CMException
    {
        BlogContext blogContext = (BlogContext) context;
        ContentId commentsId = getCommentsIdPostParameter(blogContext);
        CommentListFactory commentListFactory = blogContext.getCommentListFactory();
        CommentList commentList = commentListFactory.create(blogContext.getCmClient(), commentsId);
        return commentList;
    }
    
    protected boolean isPostingToThisContent(CmRenderContext cmContext)
    {
        ContentId idFromPath = ((BlogContext) cmContext).getBlogPostContentIdFromPath();
        return idFromPath != null && idFromPath.equalsIgnoreVersion(getCommentsIdPostParameter(cmContext));
    }
}
