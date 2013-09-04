package example.blog.command;

import example.blog.BlogContext;
import example.util.Command;
import example.util.Context;

public class PostActionSplitter implements Command
{
    private final Command _editBlogCommand;
    private final Command _createBlogPostCommand;
    private final Command _editBlogPostCommand;
    private final Command _cancelBlogPostCommand;
    private final Command _deleteBlogPostCommand;
    private final Command _createCommentCommand;
    private final Command _deleteCommentCommand;

    public enum Action {
        EDIT_BLOG,
        CREATE_BLOG_POST,
        EDIT_BLOG_POST,
        CANCEL_BLOG_POST,
        DELETE_BLOG_POST,
        CREATE_COMMENT,
        DELETE_COMMENT
    }
    
    public PostActionSplitter(Command editBlogCommand,
                              Command createBlogPostCommand,
                              Command editBlogPostCommand,
                              Command cancelBlogPostCommand,
                              Command deleteBlogPostCommand,
                              Command createCommentCommand,
                              Command deleteCommentCommand)
    {
        _editBlogCommand = editBlogCommand;
        _createBlogPostCommand = createBlogPostCommand;
        _editBlogPostCommand = editBlogPostCommand;
        _cancelBlogPostCommand = cancelBlogPostCommand;
        _deleteBlogPostCommand = deleteBlogPostCommand;
        _createCommentCommand = createCommentCommand;
        _deleteCommentCommand = deleteCommentCommand;
    }

    public boolean execute(Context context)
    {
        BlogContext blogContext = (BlogContext) context;
        Action action = blogContext.getAction();
        
        if (null == action) {
            return false;
        }
        
        switch (action) {
            case EDIT_BLOG:
                return _editBlogCommand.execute(context);
            case CREATE_BLOG_POST:
                return _createBlogPostCommand.execute(context);
            case EDIT_BLOG_POST:
                return _editBlogPostCommand.execute(context);
            case CANCEL_BLOG_POST:
                return _cancelBlogPostCommand.execute(context);
            case DELETE_BLOG_POST:
                return _deleteBlogPostCommand.execute(context);
            case CREATE_COMMENT:
                return _createCommentCommand.execute(context);
            case DELETE_COMMENT:
                return _deleteCommentCommand.execute(context);
        }

        return true;
    }
}
