package example.blog.image.command;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.fileupload.FileItem;

import com.polopoly.cm.app.imagemanager.ImageFormatException;
import com.polopoly.cm.app.imagemanager.ImageManagerPolicy;
import com.polopoly.cm.app.imagemanager.ImageTooBigException;
import com.polopoly.cm.client.CMException;

import example.blog.BlogPostPolicy;
import example.blog.image.BlogImageContext;
import example.blog.image.FckEditorUploadResponse;
import example.util.Command;
import example.util.Context;

public class ImportBlogImageCommand
    implements Command
{
    private static final Logger LOG =
        Logger.getLogger(ImportBlogImageCommand.class.getName());

    public boolean execute(Context context)
    {
        BlogImageContext blogImageContext = (BlogImageContext) context;
        
        BlogPostPolicy blogPost = blogImageContext.getBlogPost();
        Map<String, FileItem> fileItemMap = blogImageContext.getFileItemMap();
        
        FckEditorUploadResponse fckEditorResponse = blogImageContext.getFckEditorResponse();
        
        try {
            ImageManagerPolicy imageManager =
                (ImageManagerPolicy) blogPost.getChildPolicy("images");
            
            FileItem imageItem = fileItemMap.get("blog_post_image");
            String imagePath =
                imageManager.importImage(imageItem.getName(), imageItem.getInputStream());
            
            blogPost.flushCache();
            fckEditorResponse.setImageFilePath(imagePath);

            return true;
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Error while communicating with cm system.", e);
            fckEditorResponse.setStatus(FckEditorUploadResponse.Status.SERVER_ERROR);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error while importing image.", e);
            fckEditorResponse.setStatus(FckEditorUploadResponse.Status.SERVER_ERROR);
        } catch (ImageFormatException e) {
            LOG.log(Level.FINE, "Error while reading image stream.", e);
            fckEditorResponse.setStatus(FckEditorUploadResponse.Status.BAD_INPUT);
        } catch (ImageTooBigException e) {
            LOG.log(Level.FINE, "User tried to upload a too large image (too many pixels).", e);
            fckEditorResponse.setStatus(FckEditorUploadResponse.Status.IMAGE_TOO_LARGE);
        }

        return false;
    }
}
