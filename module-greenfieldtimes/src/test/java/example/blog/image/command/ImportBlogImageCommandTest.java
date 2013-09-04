package example.blog.image.command;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.mockito.Mock;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.imagemanager.ImageFormatException;
import com.polopoly.cm.app.imagemanager.ImageManagerPolicy;
import com.polopoly.cm.app.imagemanager.ImageTooBigException;
import com.polopoly.cm.client.CMException;

import example.MockitoBase;
import example.blog.BlogPostPolicy;
import example.blog.image.BlogImageRequestContext;
import example.blog.image.FckEditorUploadResponse;

public class ImportBlogImageCommandTest
    extends MockitoBase
{
    private ImportBlogImageCommand _toTest;
    
    @Mock private BlogImageRequestContext blogImageContext;
    @Mock private FckEditorUploadResponse fckEditorResponse;
    @Mock private FileItem fileItem;
    @Mock private BlogPostPolicy blogPostPolicy;
    @Mock ImageManagerPolicy imageManager;
    @Mock InputStream inputStream;
    
    ContentId blogPostContentId = new ContentId(19, 100);
    ContentId blogPostItExternalId = new ExternalContentId("example.BlogPost");
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        _toTest = new ImportBlogImageCommand();
    }
    
    public void testSuccessfulImport() throws Exception
    {
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorResponse);
        when(blogImageContext.getBlogPost()).thenReturn(blogPostPolicy);                
        
        final String fieldName = "blog_post_image";
        Map<String, FileItem> fileItemMap = new HashMap<String, FileItem>();
        fileItemMap.put(fieldName, fileItem);
        when(blogImageContext.getFileItemMap()).thenReturn(fileItemMap);
        
        final String fileName = "my-image.jpg";
        when(fileItem.getName()).thenReturn(fileName);
        when(fileItem.getFieldName()).thenReturn(fieldName);
        when(fileItem.getInputStream()).thenReturn(inputStream);
        
        when(blogPostPolicy.getChildPolicy("images")).thenReturn(imageManager);
        
        final String imagePath = "images/123456789.jpg";
        when(imageManager.importImage(fileName, inputStream)).thenReturn(imagePath);        
        
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorResponse);
        
        boolean doContinue = _toTest.execute(blogImageContext);
        
        assertTrue(doContinue);
        
        verify(fckEditorResponse, never()).setStatus(any(FckEditorUploadResponse.Status.class));        
        verify(blogPostPolicy).flushCache();
        verify(fckEditorResponse).setImageFilePath(imagePath);
    }
  
    public void testCMException() throws Exception
    {
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorResponse);
        when(blogImageContext.getBlogPost()).thenReturn(blogPostPolicy);        
        when(blogPostPolicy.getChildPolicy("images")).thenThrow(new CMException(""));
        
        boolean doContinue = _toTest.execute(blogImageContext);
        
        assertFalse(doContinue);
        
        verify(fckEditorResponse).setStatus(FckEditorUploadResponse.Status.SERVER_ERROR); 
    }
    
    public void testIOException() throws Exception
    {
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorResponse);
        when(blogImageContext.getBlogPost()).thenReturn(blogPostPolicy);
        
        final String fieldName = "blog_post_image";
        Map<String, FileItem> fileItemMap = new HashMap<String, FileItem>();
        fileItemMap.put(fieldName, fileItem);
        when(blogImageContext.getFileItemMap()).thenReturn(fileItemMap);
        
        final String fileName = "my-image.jpg";
        when(fileItem.getName()).thenReturn(fileName);
        when(fileItem.getFieldName()).thenReturn(fieldName);
        when(fileItem.getInputStream()).thenReturn(inputStream);
        
        when(blogPostPolicy.getChildPolicy("images")).thenReturn(imageManager);
        
        when(imageManager.importImage(fileName, inputStream)).thenThrow(new IOException()); 
        
        boolean doContinue = _toTest.execute(blogImageContext);
        
        assertFalse(doContinue);
        
        verify(fckEditorResponse).setStatus(FckEditorUploadResponse.Status.SERVER_ERROR); 
    }
    
    public void testImageFormatException() throws Exception
    {
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorResponse);
        when(blogImageContext.getBlogPost()).thenReturn(blogPostPolicy);
        
        final String fieldName = "blog_post_image";
        Map<String, FileItem> fileItemMap = new HashMap<String, FileItem>();
        fileItemMap.put(fieldName, fileItem);
        when(blogImageContext.getFileItemMap()).thenReturn(fileItemMap);
        
        final String fileName = "my-image.jpg";
        when(fileItem.getName()).thenReturn(fileName);
        when(fileItem.getFieldName()).thenReturn(fieldName);
        when(fileItem.getInputStream()).thenReturn(inputStream);
        
        when(blogPostPolicy.getChildPolicy("images")).thenReturn(imageManager);
        
        when(imageManager.importImage(fileName, inputStream)).thenThrow(new ImageFormatException()); 
        
        boolean doContinue = _toTest.execute(blogImageContext);
        
        assertFalse(doContinue);
        
        verify(fckEditorResponse).setStatus(FckEditorUploadResponse.Status.BAD_INPUT); 
    }
    
    public void testImageTooBigException() throws Exception
    {
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorResponse);
        when(blogImageContext.getBlogPost()).thenReturn(blogPostPolicy);
        
        final String fieldName = "blog_post_image";
        Map<String, FileItem> fileItemMap = new HashMap<String, FileItem>();
        fileItemMap.put(fieldName, fileItem);
        when(blogImageContext.getFileItemMap()).thenReturn(fileItemMap);
        
        final String fileName = "my-image.jpg";
        when(fileItem.getName()).thenReturn(fileName);
        when(fileItem.getFieldName()).thenReturn(fieldName);
        when(fileItem.getInputStream()).thenReturn(inputStream);
        
        when(blogPostPolicy.getChildPolicy("images")).thenReturn(imageManager);
        
        when(imageManager.importImage(fileName, inputStream)).thenThrow(new ImageTooBigException()); 
        
        boolean doContinue = _toTest.execute(blogImageContext);
        
        assertFalse(doContinue);
        
        verify(fckEditorResponse).setStatus(FckEditorUploadResponse.Status.IMAGE_TOO_LARGE); 
    }
}
