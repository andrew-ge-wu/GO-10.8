package example.blog;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.cm.DefaultMajorNames;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.render.RenderRequest;

public class BlogPostSubmitWorker
{
    private static final String REQUEST_PARAMETER_BLOG_POST_HEADING = "blog_post_heading";

    private static final String REQUEST_PARAMETER_BLOG_POST_TEXT = "blog_post_text";
    
    public String getBlogPostHeadingFromRequest(RenderRequest request)
    {
        return request.getParameter(REQUEST_PARAMETER_BLOG_POST_HEADING);
    }
    
    public String getBlogPostTextFromRequest(RenderRequest request)
    {
        return request.getParameter(REQUEST_PARAMETER_BLOG_POST_TEXT);
    }

    public String getMethodFromRequest(HttpServletRequest request)
    {
        return request.getMethod();
    }
    
    public int getCommunityMajorFromCmServer(PolicyCMServer cmServer)
        throws CMException
    {
        return cmServer.getMajorByName(DefaultMajorNames.COMMUNITY);
    }
    
}
