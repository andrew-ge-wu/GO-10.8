package example.blog.command;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.policy.PolicyCMServer;

import example.blog.BlogContext;
import example.blog.BlogPostPolicy;
import example.blog.RenderControllerBlog;
import example.util.Context;

public class DeleteBlogPostCommand extends BlogPostCommandBase {
    private static Logger LOG = Logger.getLogger(DeleteBlogPostCommand.class.getName());
    
    public boolean execute(Context context)
    {
        BlogContext blogContext = (BlogContext) context;
        PolicyCMServer cmServer = blogContext.getPolicyCMServer();
        ContentId blogPostContentId = blogContext.getBlogPostContentId();

        BlogPostPolicy blogPost = null;
        try {
            VersionedContentId latestCommittedVersionId =
                blogPostContentId.getLatestCommittedVersionId();
            blogPost = (BlogPostPolicy) cmServer.createContentVersion(latestCommittedVersionId);
            blogPost.delete();
            cmServer.commitContent(blogPost);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Unable to delete blog post", e);
            blogContext.addErrorShowBlogView(RenderControllerBlog.DELETE_BLOG_POST_ERROR);
            return false;
        }
    }
}
