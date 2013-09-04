package example.blog.image.command;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.fileupload.FileItem;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;

import example.blog.image.BlogImageRequestContext;
import example.blog.image.FckEditorUploadResponse;
import example.util.Command;
import example.util.Context;

public class AssertBlogContentIdCommand
    implements Command
{
    private static final Logger LOG = Logger.getLogger(AssertBlogContentIdCommand.class.getName());
    
    public boolean execute(Context context)
    {
        BlogImageRequestContext blogImageContext = (BlogImageRequestContext) context;
        
        Map<String, FileItem> fileItemMap = blogImageContext.getFileItemMap();
        FckEditorUploadResponse fckEditorResponse = blogImageContext.getFckEditorResponse();
        
        String blogContentIdString;
        
        try {
            FileItem blogIdFileItem = fileItemMap.get("blogId");
            
            if (blogIdFileItem == null) {
                LOG.log(Level.WARNING, "Missing blog content id in request.");
                fckEditorResponse.setStatus(FckEditorUploadResponse.Status.BAD_INPUT);
                
                return false;
            }

            blogContentIdString = blogIdFileItem.getString("UTF-8");
            
            ContentId blogContentId = ContentIdFactory.createContentId(blogContentIdString);
            blogImageContext.setBlogContentId(blogContentId);
                
            return true;
        } catch (IllegalArgumentException e) {
            LOG.log(Level.WARNING, "Illegal blog content id in request.", e);
            fckEditorResponse.setStatus(FckEditorUploadResponse.Status.BAD_INPUT);
        } catch (UnsupportedEncodingException e) {
            LOG.log(Level.SEVERE, "Program bug, should never happen.", e);
            fckEditorResponse.setStatus(FckEditorUploadResponse.Status.SERVER_ERROR);
        }
        
        return false;
    }
}
