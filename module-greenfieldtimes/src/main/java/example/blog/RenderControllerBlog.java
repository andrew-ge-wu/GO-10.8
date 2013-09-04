package example.blog;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.community.blog.BlogPostList;
import com.polopoly.community.blog.BlogPostListFactory;
import com.polopoly.community.list.ContentIdListSlice;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

import example.blog.command.AddBlogPostCommentCommand;
import example.blog.command.AssertBlogPostBelongsToBlogCommand;
import example.blog.command.AssertUserAllowedCommand;
import example.blog.command.BlogPostCommand;
import example.blog.command.BreakChainUnlessHttpPostCommand;
import example.blog.command.CSRFValidationCommand;
import example.blog.command.DeleteBlogPostCommand;
import example.blog.command.DeleteBlogPostCommentCommand;
import example.blog.command.HandleBlogPostCancelCommand;
import example.blog.command.PopulateBlogFieldDataCommand;
import example.blog.command.PopulateBlogNavigationCommand;
import example.blog.command.PopulateBlogPostFieldDataCommand;
import example.blog.command.PostActionSplitter;
import example.blog.command.PrepareBlogViewCommand;
import example.blog.command.UpdateBlogCommand;
import example.blog.command.ViewSplitter;
import example.content.RenderControllerExtended;
import example.membership.UserHandlerImpl;
import example.util.Chain;
import example.util.ChainImpl;
import example.util.RequestParameterUtil;
import example.util.UrlBuilder;

public class RenderControllerBlog extends RenderControllerExtended
{
    private static final Logger LOG = Logger.getLogger(RenderControllerBlog.class.getName());

    // Error message keys.
    public static final String INTERNAL_SERVER_ERROR = "internalServerError";
    public static final String PERMISSION_DENIED = "permissionDenied";
    public static final String FIELD_REQUIRED_BLOG_NAME = "fieldRequiredBlogName";
    public static final String FIELD_REQUIRED_BLOG_ADDRESS = "fieldRequiredBlogAddress";
    public static final String FIELD_INVALID_BLOG_ADDRESS = "fieldInvalidBlogAddress";
    public static final String WEB_ALIAS_EXISTS_ERROR = "webAliasExistsError";
    public static final String CREATE_BLOG_POST_ERROR = "createBlogPostError";
    public static final String UPDATE_BLOG_POST_ERROR = "updateBlogPostError";
    public static final String BLOG_POST_NAME_EMPTY = "blogPostNameEmpty";
    public static final String CANCEL_BLOG_POST_UPDATE_ERROR = "cancelBlogPostUpdateError";
    public static final String DELETE_BLOG_POST_ERROR = "deleteBlogPostError";
    public static final String ADD_COMMENT_ERROR = "addCommentError";
    public static final String DELETE_COMMENT_ERROR = "deleteCommentError";
    public static final String COMMENT_AUTHOR_EMPTY = "commentAuthorEmpty";
    public static final String COMMENT_TEXT_EMPTY = "commentTextEmpty";
    public static final String COMMENT_BAD_CAPTCHA = "badCaptcha";

    // Model variables names.
    public static final String BLOG_POST_ID = "blogPostId";
    public static final String IS_BLOG_EDIT = "isBlogEdit";
    public static final String IS_BLOG_POST_EDIT = "isBlogPostEdit";
    public static final String IS_BLOG_POST_CREATE = "isBlogPostCreate";
    public static final String IS_SINGLE_POST_VIEW = "isSinglePostView";
    public static final String HAS_BLOG_ERROR = "hasBlogError";
    public static final String HAS_COMMENT_ERROR = "hasCommentError";
    public static final String COMMENT_ECHO_AUTHOR = "commentEchoAuthor";
    public static final String COMMENT_ECHO_TEXT = "commentEchoText";
    public static final String BLOG_POST_ECHO_TEXT = "blogPostEchoText";
    public static final String BLOG_POST_ECHO_NAME = "blogPostEchoName";

    // Request parameter names.
    public static final String PARAMETER_EDIT_BLOG = "editBlog";
    public static final String PARAMETER_CREATE_POST = "createPost";
    public static final String PARAMETER_EDIT_POST = "editPost";
    public static final String COMMENTS_ID = "commentsId";

    private final BlogPostSubmitWorker _blogPostSubmitWorker = new BlogPostSubmitWorker();
    private final BlogStatistics _blogStatistics;
    private final Chain _viewChain;
    private final Chain _handlePostChain;

    public RenderControllerBlog()
    {
        _viewChain = createViewChain();
        _handlePostChain = createHandlePostChain();
        _blogStatistics = new BlogStatistics();
    }

    Chain createHandlePostChain()
    {
        AntiSamyHTMLValidator htmlValidator = new AntiSamyHTMLValidator();

        Chain editBlogChain = new ChainImpl();
        editBlogChain.addCommand(new AssertUserAllowedCommand());
        editBlogChain.addCommand(new UpdateBlogCommand(htmlValidator));

        Chain createBlogPostChain = new ChainImpl();
        createBlogPostChain.addCommand(new AssertUserAllowedCommand());
        createBlogPostChain.addCommand(new BlogPostCommand(htmlValidator));

        Chain editBlogPostChain = new ChainImpl();
        editBlogPostChain.addCommand(new AssertUserAllowedCommand());
        editBlogPostChain.addCommand(new AssertBlogPostBelongsToBlogCommand());
        editBlogPostChain.addCommand(new BlogPostCommand(htmlValidator));

        Chain cancelBlogPostChain = new ChainImpl();
        cancelBlogPostChain.addCommand(new AssertUserAllowedCommand());
        cancelBlogPostChain.addCommand(new HandleBlogPostCancelCommand());

        Chain deleteBlogPostChain = new ChainImpl();
        deleteBlogPostChain.addCommand(new AssertUserAllowedCommand());
        deleteBlogPostChain.addCommand(new AssertBlogPostBelongsToBlogCommand());
        deleteBlogPostChain.addCommand(new DeleteBlogPostCommand());

        ChainImpl createCommentChain = new ChainImpl();
        createCommentChain.addCommand(new AddBlogPostCommentCommand(htmlValidator));

        ChainImpl deleteCommentChain = new ChainImpl();
        deleteCommentChain.addCommand(new AssertUserAllowedCommand());
        deleteCommentChain.addCommand(new AssertBlogPostBelongsToBlogCommand());
        deleteCommentChain.addCommand(new DeleteBlogPostCommentCommand());

        Chain httpPostMethodChain = new ChainImpl();
        httpPostMethodChain.addCommand(new BreakChainUnlessHttpPostCommand());
        httpPostMethodChain.addCommand(new CSRFValidationCommand());
        httpPostMethodChain.addCommand(new PostActionSplitter(editBlogChain,
                                                              createBlogPostChain,
                                                              editBlogPostChain,
                                                              cancelBlogPostChain,
                                                              deleteBlogPostChain,
                                                              createCommentChain,
                                                              deleteCommentChain));

        return httpPostMethodChain;
    }

    Chain createViewChain()
    {
        Chain viewBlogChain = new ChainImpl();
        viewBlogChain.addCommand(new PopulateBlogNavigationCommand());
        viewBlogChain.addCommand(new PrepareBlogViewCommand());

        Chain editBlogChain = new ChainImpl();
        editBlogChain.addCommand(new AssertUserAllowedCommand());
        editBlogChain.addCommand(new PopulateBlogFieldDataCommand());

        Chain viewBlogPostChain = new ChainImpl();
        viewBlogPostChain.addCommand(new PopulateBlogNavigationCommand());

        Chain createBlogPostChain = new ChainImpl();
        createBlogPostChain.addCommand(new AssertUserAllowedCommand());

        Chain editBlogPostChain = new ChainImpl();
        editBlogPostChain.addCommand(new AssertUserAllowedCommand());
        editBlogPostChain.addCommand(new PopulateBlogPostFieldDataCommand());


        Chain viewChain = new ChainImpl();
        viewChain.addCommand(new ViewSplitter(viewBlogChain,
                                              editBlogChain,
                                              viewBlogPostChain,
                                              createBlogPostChain,
                                              editBlogPostChain));
        return viewChain;
    }

    @Override
    public void populateModelBeforeCacheKey(RenderRequest request,
                                            TopModel m,
                                            ControllerContext context)
    {
        m.getLocal().setAttribute("requestIndex", getIndex(request));

        BlogPostList blogPostList =
            new BlogPostListFactory().create(getCmClient(context),
                                             context.getContentId());

        try {
            ContentIdListSlice slice = blogPostList.getSlice(0, 1);
            List<ContentId> postList = slice.getContentIds();

            ContentId latestPostId = null;

            if (postList.size() > 0) {
                latestPostId = postList.get(0);
            }

            m.getLocal().setAttribute("latestPostId", latestPostId);
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Could not get latest post id.", e);
        }

        storeBlogUrlInStack(request, m, context);
        _blogStatistics.populateStatisticsContext(m);
    }

    private void storeBlogUrlInStack(RenderRequest request,
                                     TopModel m,
                                     ControllerContext context)
    {
        try {
            String blogUrl =
                new UrlBuilder().buildUrl(m.getRequest().getOriginalContentPath(),
                                          context.getContentId(),
                                          request);
            m.getStack().setAttribute("blogUrl", blogUrl);
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Could not get blog url.", e);
        }
    }

    @Override
    public Object getCacheKey(RenderRequest request,
                              TopModel m,
                              Object defaultKey,
                              ControllerContext context)
    {
        if (isPost(request) || isEditMode(request)) {
            return null;
        }

        return defaultKey;
    }

    private int getIndex(RenderRequest request)
    {
        return new RequestParameterUtil().getInt(request, "index", 0, 0, Integer.MAX_VALUE);
    }

    public void populateModelAfterCacheKey(RenderRequest request,
                                           TopModel m,
                                           CacheInfo cacheInfo,
                                           ControllerContext context)
    {
        super.populateModelAfterCacheKey(request, m, cacheInfo, context);

        long cacheTime = 1 * 60 * 1000;
        if (isEditMode(request) || isPost(request)) {
            cacheTime = 0;
        }

        cacheInfo.setCacheTime(cacheTime);

        CmClient cmClient = getCmClient(context);

        BlogContext ctx = new BlogRequestContext(request, m, context, cmClient, new UserHandlerImpl());

        _handlePostChain.execute(ctx);
        _viewChain.execute(ctx);
    }

    private boolean isPost(RenderRequest request)
    {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String method = _blogPostSubmitWorker.getMethodFromRequest(httpServletRequest);

        return BreakChainUnlessHttpPostCommand.REQUEST_METHOD_POST.equals(method);
    }

    private boolean isEditMode(RenderRequest request)
    {
        return request.getParameter(PARAMETER_CREATE_POST) != null
               || request.getParameter(PARAMETER_EDIT_POST) != null
               || request.getParameter(PARAMETER_EDIT_BLOG) != null;
    }
}
