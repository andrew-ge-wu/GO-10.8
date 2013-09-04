package example.blog.image;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.polopoly.application.servlet.ApplicationServletUtil;
import com.polopoly.cm.DefaultMajorNames;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.CmClientBase;

import example.blog.image.command.AssertBlogContentIdCommand;
import example.blog.image.command.AssertMultipartContentRequestCommand;
import example.blog.image.command.AssertUserAllowedCommand;
import example.blog.image.command.EnsureBlogPostPresentCommand;
import example.blog.image.command.ImportBlogImageCommand;
import example.blog.image.command.MultipartRequestExtractorCommand;
import example.membership.UserHandlerImpl;
import example.util.ChainImpl;

@SuppressWarnings("serial")
public class BlogImageUploadServlet extends BlogImageUploadServletBase {

    @Override
    public void init(ServletConfig config)
        throws ServletException
    {
        super.init(config);
        
        _userHandler = new UserHandlerImpl();
        
        _cmClient = ((CmClient) ApplicationServletUtil
            .getApplication(config.getServletContext())
            .getApplicationComponent(CmClientBase.DEFAULT_COMPOUND_NAME));
        
        _cmServer = _cmClient.getPolicyCMServer();
        
        _chain = new ChainImpl();
        
        _chain.addCommand(new AssertMultipartContentRequestCommand());
        _chain.addCommand(new MultipartRequestExtractorCommand());
        _chain.addCommand(new AssertBlogContentIdCommand());
        _chain.addCommand(new AssertUserAllowedCommand());
        _chain.addCommand(new EnsureBlogPostPresentCommand());
        _chain.addCommand(new ImportBlogImageCommand());
        
        try {
            _communityMajor = _cmServer.getMajorByName(DefaultMajorNames.COMMUNITY);
        } catch (CMException e) {
            throw new ServletException("Cannot retrieve community major from cm server.", e);
        }
    }

    @Override
    BlogImageContext createBlogImageContext(HttpServletRequest request)
    {
        return new BlogImageRequestContext(request,
                                           _userHandler,
                                           _cmServer,
                                           _communityMajor);
    }
}
