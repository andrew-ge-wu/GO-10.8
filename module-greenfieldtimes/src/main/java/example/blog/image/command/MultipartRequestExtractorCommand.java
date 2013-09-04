package example.blog.image.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import example.blog.image.BlogImageRequestContext;
import example.blog.image.FckEditorUploadResponse;
import example.util.Command;
import example.util.Context;

public class MultipartRequestExtractorCommand
    implements Command
{
    private static final Logger LOG =
        Logger.getLogger(MultipartRequestExtractorCommand.class.getName());

    @SuppressWarnings("unchecked")
    public boolean execute(Context context)
    {
        BlogImageRequestContext blogImageContext = (BlogImageRequestContext) context;
        HttpServletRequest request = blogImageContext.getRequest();
        
        FckEditorUploadResponse fckEditorResponse = blogImageContext.getFckEditorResponse();
        
        ServletFileUpload upload = blogImageContext.getServletFileUpload();
        upload.setSizeMax(768 * 1024);

        Map<String, FileItem> fileItemMap = new HashMap<String, FileItem>();
        
        try {
            List<FileItem> files = upload.parseRequest(request);
                
            for (FileItem fileItem : files) {
                fileItemMap.put(fileItem.getFieldName(), fileItem);
            }
            
            blogImageContext.setFileItemMap(fileItemMap);
            
            return true;
        } catch (FileUploadBase.SizeLimitExceededException e) {
            LOG.log(Level.FINE, "User tried to upload a too large image (in bytes).", e);
            fckEditorResponse.setStatus(FckEditorUploadResponse.Status.IMAGE_TOO_LARGE);
        } catch (FileUploadException e) {
            LOG.log(Level.WARNING, "Error while uploading image.", e);
            fckEditorResponse.setStatus(FckEditorUploadResponse.Status.SERVER_ERROR);
        }
        
        return false;
    }
}
