package example.blog.image.command;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.mockito.Mock;

import com.polopoly.cm.ContentId;

import example.MockitoBase;
import example.blog.image.BlogImageRequestContext;
import example.blog.image.FckEditorUploadResponse;

public class AssertBlogContentIdCommandTest
    extends MockitoBase
{
    private AssertBlogContentIdCommand toTest = null;
    
    @Mock private BlogImageRequestContext blogImageContext;
    @Mock private HttpServletRequest request;
    
    @Mock FileItem blogIdFileItem;
    @Mock Map<String, FileItem> fileItemMap;
    
    @Mock FckEditorUploadResponse fckEditorResponse;
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        toTest = new AssertBlogContentIdCommand();
    }
    
    public void testValidContentId()
        throws Exception
    {
        when(blogImageContext.getRequest()).thenReturn(request);
        when(blogImageContext.getFileItemMap()).thenReturn(fileItemMap);
        
        when(fileItemMap.get("blogId")).thenReturn(blogIdFileItem);
        when(blogIdFileItem.getString("UTF-8")).thenReturn("19.110");
        
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorResponse);
        
        boolean doContinue = toTest.execute(blogImageContext);
        
        assertTrue("Returned false when it should have returned true.", doContinue);
        
        verify(blogImageContext).getFileItemMap();
        verify(fileItemMap).get("blogId");
        verify(blogIdFileItem).getString("UTF-8");
        verify(blogImageContext).setBlogContentId(new ContentId(19, 110));
        verify(fckEditorResponse, never()).setStatus(FckEditorUploadResponse.Status.OK);
    }
    
    public void testNullContentId()
        throws Exception
    {
        when(blogImageContext.getRequest()).thenReturn(request);
        when(blogImageContext.getFileItemMap()).thenReturn(fileItemMap);
        
        when(fileItemMap.get("blogId")).thenReturn(null);      
        
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorResponse);
                
        boolean doContinue = toTest.execute(blogImageContext);
        
        assertFalse("Returned true when it should have returned false.", doContinue);
        
        verify(blogImageContext).getFileItemMap();
        verify(fileItemMap).get("blogId");
        verify(blogIdFileItem, never()).getString("UTF-8");
        verify(blogImageContext, never()).setBlogContentId(any(ContentId.class));
        verify(fckEditorResponse).setStatus(FckEditorUploadResponse.Status.BAD_INPUT);        
    }
    
    public void testInvalidContentId()
        throws Exception
    {
        when(blogImageContext.getRequest()).thenReturn(request);
        when(blogImageContext.getFileItemMap()).thenReturn(fileItemMap);
        
        when(fileItemMap.get("blogId")).thenReturn(blogIdFileItem);
        when(blogIdFileItem.getString("UTF-8")).thenReturn("invalidContentId");
        
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorResponse);
        
        boolean doContinue = toTest.execute(blogImageContext);
        
        assertFalse("Returned true when it should have returned false.", doContinue);
        
        verify(blogImageContext).getFileItemMap();
        verify(fileItemMap).get("blogId");
        verify(blogIdFileItem).getString("UTF-8");
        verify(blogImageContext, never()).setBlogContentId(any(ContentId.class));
        verify(fckEditorResponse).setStatus(FckEditorUploadResponse.Status.BAD_INPUT);
    }
    
    public void testEmptyContentId()
        throws Exception
    {
        when(blogImageContext.getRequest()).thenReturn(request);
        when(blogImageContext.getFileItemMap()).thenReturn(fileItemMap);
        
        when(fileItemMap.get("blogId")).thenReturn(blogIdFileItem);
        when(blogIdFileItem.getString("UTF-8")).thenReturn("");
        
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorResponse);
        
        boolean doContinue = toTest.execute(blogImageContext);
        
        assertFalse("Returned true when it should have returned false.", doContinue);
        
        verify(blogImageContext).getFileItemMap();
        verify(fileItemMap).get("blogId");
        verify(blogIdFileItem).getString("UTF-8");
        verify(blogImageContext, never()).setBlogContentId(any(ContentId.class));
        verify(fckEditorResponse).setStatus(FckEditorUploadResponse.Status.BAD_INPUT);
    }
}
