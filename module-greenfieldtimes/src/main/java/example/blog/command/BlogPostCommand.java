package example.blog.command;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.DefaultMajorNames;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.community.blog.BlogPostList;
import com.polopoly.community.blog.BlogPostListFactory;
import com.polopoly.management.ServiceNotAvailableException;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.util.StringUtil;

import example.blog.BlogContext;
import example.blog.BlogPostPolicy;
import example.blog.HTMLValidator;
import example.blog.InvalidHTMLException;
import example.blog.RenderControllerBlog;
import example.blog.command.PostActionSplitter.Action;
import example.util.Context;

public class BlogPostCommand
    extends BlogPostCommandBase
{
    private static final Logger LOG = Logger.getLogger(BlogPostCommand.class.getName());
    private final ExternalContentId _blogPostItExternalId = new ExternalContentId("example.BlogPost");
    
    private final HTMLValidator _validator;
    
    public BlogPostCommand(HTMLValidator htmlValidator) {
        _validator = htmlValidator;
    }

    public boolean execute(Context context)
    {
        BlogContext blogContext = (BlogContext) context;
        RenderRequest renderRequest = blogContext.getRenderRequest();
        
        ModelWrite localModel = blogContext.getLocalModel();

        Action action = blogContext.getAction();
        boolean create;
        if (action == PostActionSplitter.Action.CREATE_BLOG_POST) {
            create = true;
        }
        else if (action == PostActionSplitter.Action.EDIT_BLOG_POST) {
            create = false;
        }
        else {
            return false;
        }
        
        CmClient cmClient = blogContext.getCmClient();
        PolicyCMServer cmServer = blogContext.getPolicyCMServer();
        
        ContentId blogId = blogContext.getBlogContentId();

        String blogPostName = getBlogPostName(renderRequest);
        String blogPostText = getBlogPostText(renderRequest);

        localModel.setAttribute(RenderControllerBlog.BLOG_POST_ECHO_NAME, blogPostName);
        localModel.setAttribute(RenderControllerBlog.BLOG_POST_ECHO_TEXT, blogPostText);

        if (StringUtil.isEmpty(blogPostName)) {
            if (create) {
                blogContext.addErrorShowBlogPostCreate(RenderControllerBlog.BLOG_POST_NAME_EMPTY);
            } else {
                blogContext.addErrorShowBlogPostEdit(RenderControllerBlog.BLOG_POST_NAME_EMPTY);
            }
            return false;
        }
        
        BlogPostPolicy blogPost;
        try {
            int major = cmServer.getMajorByName(DefaultMajorNames.COMMUNITY);
            
            ContentId blogPostId = blogContext.getBlogPostContentId();
            if (null == blogPostId) {
                blogPost = (BlogPostPolicy) cmServer.createContent(major, blogId, _blogPostItExternalId);
            }
            else {
                if (imageUploadCreatedNewContentVersion(blogPostId)) {
                    blogPost = (BlogPostPolicy) cmServer.getPolicy(blogPostId);
                }
                else {
                    blogPost = (BlogPostPolicy) cmServer.createContentVersion(blogPostId.getLatestCommittedVersionId());
                }
            }
                        
            blogPost.setName(_validator.stripAllHTML(blogPostName).trim());
            blogPost.setText(_validator.getCleanHTML(blogPostText).trim());
            
            cmServer.commitContent(blogPost);

            if (create) {
                BlogPostListFactory blogPostListFactory = blogContext.getBlogPostListFactory();
                BlogPostList blogPostList = blogPostListFactory.create(cmClient, blogId);
                Content blogPostContent = blogPost.getContent();
                ContentId blogPostContentId = blogPostContent.getContentId().getContentId();

                blogPostList.addFirst(blogPostContentId);
            }
            
            return true;
        } catch (CMException e) {
            handleServerError(blogContext, e, create);
        } catch (ServiceNotAvailableException e) {
            handleServerError(blogContext, e, create);
        }  catch (InvalidHTMLException e) {
            handleServerError(blogContext, e, create);
        }
        return false;
    }

    private void handleServerError(BlogContext blogContext, Exception e, boolean create)
    {
        LOG.log(Level.WARNING, "Error while creating/editing blog post.", e);
        if (create) {
            blogContext.addErrorShowBlogPostCreate(RenderControllerBlog.CREATE_BLOG_POST_ERROR);
        }
        else {
            blogContext.addErrorShowBlogPostCreate(RenderControllerBlog.UPDATE_BLOG_POST_ERROR);
        }
    }
}
