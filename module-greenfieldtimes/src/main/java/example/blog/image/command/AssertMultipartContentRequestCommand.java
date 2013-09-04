package example.blog.image.command;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import example.blog.image.BlogImageContext;
import example.blog.image.FckEditorUploadResponse;
import example.util.Command;
import example.util.Context;

public class AssertMultipartContentRequestCommand
    implements Command
{
    private static final Logger LOG = Logger.getLogger(AssertMultipartContentRequestCommand.class.getName());
    
    public boolean execute(Context context)
    {
        BlogImageContext blogImageContext = (BlogImageContext) context;
        FckEditorUploadResponse fckEditorResponse = blogImageContext.getFckEditorResponse();
        
        @SuppressWarnings("deprecation")
		boolean isMultipart = ServletFileUpload.isMultipartContent(blogImageContext.getRequest());
        
        if (!isMultipart) {
            LOG.log(Level.WARNING, "Wrong encoding type of form in request.");
            fckEditorResponse.setStatus(FckEditorUploadResponse.Status.BAD_INPUT);
        }
        
        return isMultipart;
    }
}
