package example.blog.image.command;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.fileupload.FileItem;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.policy.PolicyCMServer;

import example.blog.BlogPostPolicy;
import example.blog.image.BlogImageRequestContext;
import example.blog.image.FckEditorUploadResponse;
import example.util.Command;
import example.util.Context;

public class EnsureBlogPostPresentCommand
    implements Command
{
    private static final Logger LOG = Logger.getLogger(EnsureBlogPostPresentCommand.class.getName());
    
    private final ContentId blogPostItExternalId = new ExternalContentId("example.BlogPost");
    
    public boolean execute(Context context)
    {
        BlogImageRequestContext blogImageContext = (BlogImageRequestContext) context;
        ContentId blogId = blogImageContext.getBlogContentId();
        PolicyCMServer cmServer = blogImageContext.getPolicyCMServer();
        Map<String, FileItem> fileItemMap = blogImageContext.getFileItemMap();
        
        FckEditorUploadResponse fckEditorResponse = blogImageContext.getFckEditorResponse();
        
        int communityMajor = blogImageContext.getCommunityMajor();
        
        try {
            FileItem blogPostIdItem = fileItemMap.get("blogPostId");
            BlogPostPolicy blogPost;
                
            if (null == blogPostIdItem || blogPostIdItem.getString("UTF-8").trim().length() == 0) {
                blogPost = (BlogPostPolicy) cmServer.createContent(communityMajor, blogId, blogPostItExternalId);
            } else {
                String blogPostIdString = blogPostIdItem.getString("UTF-8");
                ContentId requestedBlogPostContentId = ContentIdFactory.createContentId(blogPostIdString);

                int requestedBlogPostVersion = requestedBlogPostContentId.getVersion();
                if (requestedBlogPostVersion == VersionedContentId.UNDEFINED_VERSION) {
                    // update previously committed content
                    VersionedContentId latestCommitted = requestedBlogPostContentId.getLatestCommittedVersionId();
                    blogPost = (BlogPostPolicy) cmServer.createContentVersion(latestCommitted);
                } else {
                    // in-edit content
                    blogPost = (BlogPostPolicy) cmServer.getPolicy(requestedBlogPostContentId);
                    
                    if (null == blogPost.getContent().getLockInfo()) {
                        blogPost.lock();
                    }
                }
            }
            
            blogImageContext.setBlogPost(blogPost);
            
            Content blogPostContent = blogPost.getContent();
            
            fckEditorResponse.setBlogPostContentId(blogPostContent.getContentId());
            
            return true;
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Error while creating blog post content.", e);
            fckEditorResponse.setStatus(FckEditorUploadResponse.Status.SERVER_ERROR);
        } catch (IllegalArgumentException e) {
            LOG.log(Level.WARNING, "Invalid blog post content id in request.", e);
            fckEditorResponse.setStatus(FckEditorUploadResponse.Status.BAD_INPUT);
        } catch (UnsupportedEncodingException e) {
            LOG.log(Level.WARNING, "Program bug, should never happen.", e);
            fckEditorResponse.setStatus(FckEditorUploadResponse.Status.SERVER_ERROR);
        }
        
        return false;
    }
}
