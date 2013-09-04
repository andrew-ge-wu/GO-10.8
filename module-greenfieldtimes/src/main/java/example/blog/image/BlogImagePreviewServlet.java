package example.blog.image;

import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.community.blog.servlet.BlogPostPreviewFileServlet;
import com.polopoly.user.server.UserId;

import example.blog.BlogPostPolicy;

/**
 * Extends the abstract {@link BlogPostPreviewFileServlet} with the owner
 * concept from Greenfield Times {@link example.blog.BlogPolicy}.
 */
@SuppressWarnings("serial")
public class BlogImagePreviewServlet extends BlogPostPreviewFileServlet {
    
    protected boolean isCorrectUser(VersionedContentId latestVersionId,
                                    PolicyCMServer policyCmServer)
        throws CMException
    {
        UserId userId = policyCmServer.getCurrentCaller().getUserId();
        
        Policy policy = policyCmServer.getPolicy(latestVersionId);
        if ( policy instanceof BlogPostPolicy ){
            BlogPostPolicy blogPost = (BlogPostPolicy)policy;
            if (blogPost.isAllowedToEdit(userId)) {
                return true;
            }
        }
        
        return false;
    }


}
