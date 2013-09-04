package example.blog.command;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.cm.DefaultMajorNames;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.alias.InvalidUrlPathSegmentException;
import com.polopoly.cm.alias.UrlPathSegmentAlreadyExistsException;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.membership.UserDataConcurrentModificationException;
import com.polopoly.siteengine.membership.UserDataManager;
import com.polopoly.siteengine.membership.UserDataOperationFailedException;
import com.polopoly.user.server.User;
import com.polopoly.user.server.UserId;

import example.blog.BlogContext;
import example.blog.BlogForm;
import example.blog.BlogPolicy;
import example.blog.HTMLValidator;
import example.blog.UserBlogsData;
import example.membership.RenderControllerProfileElement;
import example.util.Context;

public class CreateBlogCommand extends BlogCommandBase
{
    private static final Logger LOG = Logger.getLogger(CreateBlogCommand.class.getName());

    private final ExternalContentId blogItExternalId = new ExternalContentId("example.Blog");

    public CreateBlogCommand(HTMLValidator htmlValidator) {
        super(htmlValidator);
    }

    public boolean execute(Context context)
    {
        if (context instanceof BlogContext) {
            BlogContext blogContext = (BlogContext)context;

            HttpServletRequest httpServletRequest = (HttpServletRequest) blogContext.getRenderRequest();

            try {
                handlePost(httpServletRequest, blogContext);
            } catch (Exception e) {
                ModelWrite localModel = blogContext.getLocalModel();
                if (localModel.getAttribute("hasCreateError") == null) {
                    localModel.setAttribute("hasCreateError", true);

                    LOG.log(Level.WARNING, "Unable to create blog", e);
                    blogContext.addBlogErrorMessageToLocalModel("error", "createBlogError");
                }
                return false;
            }
        }

        return true;
    }

    private void handlePost(HttpServletRequest httpServletRequest,
                            BlogContext blogContext)
        throws CMException,
               RemoteException,
               UserDataConcurrentModificationException,
               UserDataOperationFailedException
    {
        RenderRequest renderRequest = blogContext.getRenderRequest();
        ModelWrite localModel = blogContext.getTopModel().getLocal();

        BlogForm blogForm = populateBlogForm(renderRequest);
        localModel.setAttribute("blogForm", blogForm);


        if (!(blogFormValidator.validate(blogContext, blogForm))) {
            localModel.setAttribute("hasCreateError", true);
            return;
        }

        BlogPolicy blog = createBlog(blogForm, blogContext);

        UserDataManager userDataManager = blogContext.getUserDataManager();

        User loggedInUser = blogContext.getLoggedInUser();

        UserBlogsData serviceBean = updateUserServiceData(loggedInUser,
                                                          userDataManager,
                                                          blog);

        try {
            userDataManager.commitServiceBean(loggedInUser.getUserId(),
                                              RenderControllerProfileElement.SERVICE_ID_BLOG,
                                              serviceBean);

            localModel.setAttribute("newBlogId", blog.getContentId().getContentId().getContentIdString());
        } catch (UserDataConcurrentModificationException e) {
            LOG.log(Level.FINE, "User serivce data modified, attempting instant retry", e);

            serviceBean = updateUserServiceData(loggedInUser, userDataManager, blog);
            userDataManager.commitServiceBean(loggedInUser.getUserId(),
                                              RenderControllerProfileElement.SERVICE_ID_BLOG,
                                              serviceBean);
        }
    }

    private BlogPolicy createBlog(BlogForm blogForm,
                                  BlogContext blogContext)
        throws RemoteException, CMException
    {
        PolicyCMServer cmServer = blogContext.getPolicyCMServer();
        User loggedInUser = blogContext.getLoggedInUser();
        ModelWrite localModel = blogContext.getLocalModel();

        ExternalContentId userDataExternalId =
            new ExternalContentId("UserData-" + loggedInUser.getUserId().getPrincipalIdString());

        BlogPolicy blog = null;
        try {
            blog = (BlogPolicy) cmServer.createContent(getCommunityMajorFromCmServer(cmServer),
                                                       userDataExternalId, blogItExternalId);

            blog.setOwnerIds(Collections.singleton(loggedInUser.getUserId()));
            blog.setName(blogForm.getBlogName());
            blog.setDescription(blogForm.getBlogDescription());
            blog.setPathSegmentString(blogForm.getBlogAddress());
            blog.setInsertParent(blogForm.getParentPage());
            cmServer.commitContent(blog);

            return blog;
        }
        catch (UrlPathSegmentAlreadyExistsException e) {
            blogContext.addBlogErrorMessageToLocalModel("error", "webAliasExistsError");
            localModel.setAttribute("hasCreateError", true);
            cmServer.abortContent(blog);
            throw e;
        }
        catch (InvalidUrlPathSegmentException e) {
            blogContext.addBlogErrorMessageToLocalModel("error", "fieldRequiredBlogAddress");
            localModel.setAttribute("hasCreateError", true);
            cmServer.abortContent(blog);
            throw e;
        }
        catch (CMException cme) {
            LOG.log(Level.WARNING, "Blog creation failed", cme);
            blogContext.addBlogErrorMessageToLocalModel("error", "createBlogError");
            localModel.setAttribute("hasCreateError", true);
            cmServer.abortContent(blog);
            throw cme;
        }
    }

    public UserBlogsData updateUserServiceData(User loggedInUser,
                                               UserDataManager userDataManager,
                                               BlogPolicy blog)
        throws UserDataOperationFailedException,
               RemoteException
    {
        UserBlogsData serviceBean = (UserBlogsData)
            userDataManager.getOrCreateServiceBean(loggedInUser.getUserId(),
                                                   RenderControllerProfileElement.SERVICE_ID_BLOG);

        List<String> blogs = serviceBean.getBlogs();
        blogs.add(blog.getContentId().getContentId().getContentIdString());

        return serviceBean;
    }

    public int getCommunityMajorFromCmServer(PolicyCMServer cmServer)
        throws CMException
    {
        return cmServer.getMajorByName(DefaultMajorNames.COMMUNITY);
    }
}
