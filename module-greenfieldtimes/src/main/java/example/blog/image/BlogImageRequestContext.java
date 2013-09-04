package example.blog.image;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.policy.PolicyCMServer;

import example.blog.BlogPostPolicy;
import example.membership.UserHandler;
import example.util.ContextImplBase;

/**
 * 
 */
public class BlogImageRequestContext extends ContextImplBase
    implements BlogImageContext
{
    private final HttpServletRequest _request;
    
    private final UserHandler _userHandler;
    private final PolicyCMServer _cmServer;
    
    private final int _communityMajor;
    
    private ContentId _blogContentId;
    private BlogPostPolicy _blogPost;
    private Map<String, FileItem> _fileItemMap;
    
    private final FckEditorUploadResponse _fckEditorResponse;

    private ServletFileUpload _servletFileUpload;

    public BlogImageRequestContext(HttpServletRequest request,
                                   UserHandler userHandler,
                                   PolicyCMServer cmServer,
                                   int communityMajor)
    {
        _request = request;
        _userHandler = userHandler;
        _cmServer = cmServer;
        _communityMajor = communityMajor;
        
        _fckEditorResponse = new FckEditorUploadResponse();
        _servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
    }

    public HttpServletRequest getRequest()
    {
        return _request;
    }

    public UserHandler getUserHandler()
    {
        return _userHandler;
    }

    public PolicyCMServer getPolicyCMServer()
    {
        return _cmServer;
    }
    
    public void setBlogContentId(ContentId blogContentId)
    {
        _blogContentId = blogContentId;
    }

    public ContentId getBlogContentId()
    {
        return _blogContentId;
    }
    
    public void setBlogPost(BlogPostPolicy blogPost)
    {
        _blogPost = blogPost;
    }

    public BlogPostPolicy getBlogPost()
    {
        return _blogPost;
    }

    public int getCommunityMajor()
    {
        return _communityMajor;
    }
    
    public Map<String, FileItem> getFileItemMap()
    {
        return _fileItemMap;
    }

    public void setFileItemMap(Map<String, FileItem> fileItemMap)
    {
        _fileItemMap = fileItemMap;
    }

    public FckEditorUploadResponse getFckEditorResponse()
    {
        return _fckEditorResponse;
    }
    
    public ServletFileUpload getServletFileUpload()
    {
        return _servletFileUpload;
    }
}
