package example.blog.image;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;

import example.membership.UserHandler;
import example.util.Chain;

@SuppressWarnings("serial")
public abstract class BlogImageUploadServletBase extends HttpServlet
{
    protected UserHandler _userHandler;

    protected PolicyCMServer _cmServer;
    protected CmClient _cmClient;
    
    protected int _communityMajor;
    
    protected Chain _chain;
    
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
        throws ServletException,
               IOException
    {
        BlogImageContext blogImageContext = createBlogImageContext(request);
        
        _chain.execute(blogImageContext);
                
        response.setContentType("text/html; charset=utf-8");
        
        // add no-cache headers
        response.setHeader("Cache-Control", "private, no-store, no-cache");
        response.addHeader("Cache-Control", "must-revalidate");
        response.addHeader("Cache-Control", "max-age=0, s-max-age=0");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
                
        FckEditorUploadResponse fckEditorResponse = blogImageContext.getFckEditorResponse();
        
        PrintWriter out = response.getWriter();
        out.print(fckEditorResponse.toString());
        
        out.flush();
        out.close();
    }
    
    abstract BlogImageContext createBlogImageContext(HttpServletRequest request);

}
