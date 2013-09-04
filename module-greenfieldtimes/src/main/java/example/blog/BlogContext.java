package example.blog;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.community.blog.BlogPostListFactory;
import com.polopoly.community.comment.CommentListFactory;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.membership.UserDataManager;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.user.server.User;

import example.blog.command.PostActionSplitter.Action;
import example.membership.UserHandler;
import example.util.Context;

public interface BlogContext
    extends Context
{
    void addBlogErrorMessageToLocalModel(String errorKey, String errorMessage);
    
    void addErrorShowBlogView(String errorMessageKey);
    
    void addErrorShowBlogEdit(String errorMessageKey);
    
    void addErrorShowBlogPostEdit(String errorMessageKey);

    void addErrorShowBlogPostCreate(String errorMessageKey);
    
    void addCommentErrorShowBlogPostView(String errorMessageKey);
    
    RenderRequest getRenderRequest();
    
    TopModel getTopModel();
    
    ModelWrite getLocalModel();
    
    ControllerContext getControllerContext();
    
    CmClient getCmClient();
    
    PolicyCMServer getPolicyCMServer();

    User getLoggedInUser();

    void setLoggedInUser(User loggedInUser);
    
    UserDataManager getUserDataManager();
    
    ContentId getBlogContentId();
    
    ContentId getBlogPostContentId();
    
    ContentId getBlogPostContentIdFromPath();
    
    UserHandler getUserHandler();

    BlogPostListFactory getBlogPostListFactory();

    boolean hasDateSelected();
    
    int getSelectedYear();
    
    void setSelectedYear(int selectedYear);
    
    int getSelectedMonth();
    
    void setSelectedMonth(int selectedMonth);

    CommentListFactory getCommentListFactory();

    Action getAction();

    ContentId getEditPostId();

}
