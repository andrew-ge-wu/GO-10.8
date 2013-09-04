package example.blog.image;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.policy.PolicyCMServer;

import example.blog.BlogPostPolicy;
import example.membership.UserHandler;
import example.util.Context;

public interface BlogImageContext extends Context
{
    PolicyCMServer getPolicyCMServer();
    UserHandler getUserHandler();
    HttpServletRequest getRequest();

    BlogPostPolicy getBlogPost();
    int getCommunityMajor();
    
    Map<String, FileItem> getFileItemMap();
    ContentId getBlogContentId();
    
    FckEditorUploadResponse getFckEditorResponse();
    
    ServletFileUpload getServletFileUpload();
}
