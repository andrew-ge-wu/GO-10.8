package example.blog.image.command;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.mockito.Mock;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;

import example.MockitoBase;
import example.blog.image.BlogImageRequestContext;
import example.blog.image.FckEditorUploadResponse;

public class MultipartRequestExtractorCommandTest
    extends MockitoBase
{
    private MultipartRequestExtractorCommand _toTest;
    
    @Mock private BlogImageRequestContext blogImageContext;
    @Mock private HttpServletRequest request;
    @Mock private FckEditorUploadResponse fckEditorResponse;
    @Mock private ServletFileUpload servletFileUpload;
    @Mock private FileItem fileItem;
        
    ContentId blogPostContentId = new ContentId(19, 100);
    ContentId blogPostItExternalId = new ExternalContentId("example.BlogPost");
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        _toTest = new MultipartRequestExtractorCommand();
    }
    
    public void testValidUpload() throws Exception
    {
        final String fieldName = "myFieldName";
        
        when(blogImageContext.getRequest()).thenReturn(request);
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorResponse);
        when(blogImageContext.getServletFileUpload()).thenReturn(servletFileUpload);
        List<FileItem> files = new ArrayList<FileItem>();
        files.add(fileItem);
        when(fileItem.getFieldName()).thenReturn(fieldName);
        when(servletFileUpload.parseRequest(request)).thenReturn(files);
        
        boolean doContinue = _toTest.execute(blogImageContext);
        
        assertTrue(doContinue);
        
        Map<String, FileItem> fileItemMap = new HashMap<String, FileItem>();
        fileItemMap.put(fieldName, fileItem);
        
        verify(blogImageContext).setFileItemMap(eq(fileItemMap));
        verify(fckEditorResponse, never()).setStatus(any(FckEditorUploadResponse.Status.class));        
    }
    
    @SuppressWarnings("unchecked")
    public void testSizeLimitExceededException() throws Exception
    {        
        when(blogImageContext.getRequest()).thenReturn(request);
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorResponse);
        when(blogImageContext.getServletFileUpload()).thenReturn(servletFileUpload);
        when(servletFileUpload.parseRequest(request)).thenThrow(new SizeLimitExceededException());
        
        boolean doContinue = _toTest.execute(blogImageContext);
        
        assertFalse(doContinue);
        
        verify(blogImageContext, never()).setFileItemMap(any(Map.class));
        verify(fckEditorResponse).setStatus(FckEditorUploadResponse.Status.IMAGE_TOO_LARGE); 
    }
    
    @SuppressWarnings("unchecked")
    public void testFileUploadException() throws Exception
    {        
        when(blogImageContext.getRequest()).thenReturn(request);
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorResponse);
        when(blogImageContext.getServletFileUpload()).thenReturn(servletFileUpload);
        when(servletFileUpload.parseRequest(request)).thenThrow(new FileUploadException());
        
        boolean doContinue = _toTest.execute(blogImageContext);
        
        assertFalse(doContinue);
        
        verify(blogImageContext, never()).setFileItemMap(any(Map.class));
        verify(fckEditorResponse).setStatus(FckEditorUploadResponse.Status.SERVER_ERROR); 
    }
}
