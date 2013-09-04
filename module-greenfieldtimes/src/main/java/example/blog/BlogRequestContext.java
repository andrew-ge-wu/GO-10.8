package example.blog;

import java.util.ArrayList;
import java.util.List;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CmClient;
import com.polopoly.community.blog.BlogPostListFactory;
import com.polopoly.community.comment.CommentListFactory;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.membership.UserDataManager;
import com.polopoly.siteengine.membership.UserDataManagerImpl;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.model.request.ContentPath;
import com.polopoly.user.server.User;

import example.blog.command.PostActionSplitter.Action;
import example.membership.UserHandler;

public class BlogRequestContext extends CmRenderContext
    implements BlogContext
{
    private User _loggedInUser;
    private final BlogPostListFactory _blogPostListFactory;
    private int _selectedMonth;
    private int _selectedYear;
    private CommentListFactory _commentListFactory;
    private final UserDataManager _userDataManager;
    
    public BlogRequestContext(RenderRequest request,
                              TopModel topModel,
                              ControllerContext controllerContext,
                              CmClient cmClient,
                              UserHandler userHandler)
    {
        super(request, topModel, controllerContext, cmClient, userHandler);
        
        _selectedMonth = -1;
        _selectedYear = -1;
        
        _userDataManager = new UserDataManagerImpl(cmClient);
        _blogPostListFactory = new BlogPostListFactory();
        _commentListFactory = new CommentListFactory();
    }
    
    @SuppressWarnings("unchecked")
    public void addBlogErrorMessageToLocalModel(String errorKey,
                                                String errorMessage)
    {
        ModelWrite model = getLocalModel();
        List<String> errorList = (List<String>) model.getAttribute(errorKey);
        if (errorList == null) {
            errorList = new ArrayList<String>();
            model.setAttribute(errorKey, errorList);
        }
        errorList.add(errorMessage);
    }

    @SuppressWarnings("unchecked")
    private void addErrorMessageToStack(String errorMessageKey)
    {
        ModelWrite model = getTopModel().getStack();
        List<String> errorList = (List<String>) model.getAttribute("error");
        if (errorList == null) {
            errorList = new ArrayList<String>();
            model.setAttribute("error", errorList);
        }
        errorList.add(errorMessageKey);
    }
    
    private void addBlogError(String errorMessageKey)
    {
        addBlogErrorMessageToLocalModel("error", errorMessageKey);
        getLocalModel().setAttribute(RenderControllerBlog.HAS_BLOG_ERROR, Boolean.TRUE);
    }
    
    public void addErrorShowBlogView(String errorMessageKey)
    {
        addBlogError(errorMessageKey);
        getLocalModel().setAttribute(RenderControllerBlog.IS_BLOG_EDIT, Boolean.FALSE);
        getLocalModel().setAttribute(RenderControllerBlog.IS_BLOG_POST_EDIT, Boolean.FALSE);
        getLocalModel().setAttribute(RenderControllerBlog.IS_BLOG_POST_CREATE, Boolean.FALSE);
    }
    
    public void addErrorShowBlogEdit(String errorMessageKey)
    {
        addBlogError(errorMessageKey);
        getLocalModel().setAttribute(RenderControllerBlog.IS_BLOG_EDIT, Boolean.TRUE);
    }
    
    public void addErrorShowBlogPostEdit(String errorMessageKey)
    {
        addBlogError(errorMessageKey);
        getLocalModel().setAttribute(RenderControllerBlog.IS_BLOG_POST_EDIT, Boolean.TRUE);
    }
    
    public void addErrorShowBlogPostCreate(String errorMessageKey)
    {
        addBlogError(errorMessageKey);
        getLocalModel().setAttribute(RenderControllerBlog.IS_BLOG_POST_CREATE, Boolean.TRUE);
    }
    
    public void addCommentErrorShowBlogPostView(String errorMessageKey)
    {
        addErrorMessageToStack(errorMessageKey);
        getTopModel().getStack().setAttribute(RenderControllerBlog.HAS_COMMENT_ERROR, Boolean.TRUE);
    }

    public User getLoggedInUser()
    {
        return _loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser)
    {
        _loggedInUser = loggedInUser;
    }

    public UserDataManager getUserDataManager()
    {
        return _userDataManager;
    }

    public ContentId getBlogContentId()
    {
        return getCurrentContentId();
    }

    public ContentId getBlogPostContentId()
    {
        return getContentIdFromRequest(RenderControllerBlog.BLOG_POST_ID);
    }

    public ContentId getBlogPostContentIdFromPath()
    {
        ContentPath pathAfterPage = getTopModel().getContext().getPage().getPathAfterPage();
        if (pathAfterPage.size() >= 2) {
            return pathAfterPage.get(1);
        }
        
        return null;
    }
    
    public ContentId getEditPostId()
    {
        return getContentIdFromRequest(RenderControllerBlog.PARAMETER_EDIT_POST);
    }

    public BlogPostListFactory getBlogPostListFactory()
    {
        return _blogPostListFactory;
    }

    public int getSelectedMonth() {
        return _selectedMonth;
    }

    public int getSelectedYear() {
        return _selectedYear;
    }

    public boolean hasDateSelected() {
        return _selectedMonth > -1 &&
               _selectedYear > -1;
    }

    public void setSelectedMonth(int selectedMonth) {
        _selectedMonth = selectedMonth;
    }

    public void setSelectedYear(int selectedYear) {
        _selectedYear = selectedYear;
        
    }

    public CommentListFactory getCommentListFactory()
    {
        return _commentListFactory;
    }

    public Action getAction()
    {
        String actionString = this.getRenderRequest().getParameter("action");
        
        try {
            return Action.valueOf(actionString);
        }
        catch (Exception e) {
            return null;
        }
    }
}
