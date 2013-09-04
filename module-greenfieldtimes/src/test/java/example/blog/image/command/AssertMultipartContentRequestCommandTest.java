package example.blog.image.command;

import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.mockito.Mock;

import example.MockitoBase;
import example.blog.image.BlogImageContext;
import example.blog.image.FckEditorUploadResponse;

public class AssertMultipartContentRequestCommandTest
    extends MockitoBase
{
    private AssertMultipartContentRequestCommand toTest = null;
    
    @Mock private BlogImageContext blogImageContext;
    @Mock private HttpServletRequest request;
    @Mock private FckEditorUploadResponse fckEditorUploadResponse;
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        toTest = new AssertMultipartContentRequestCommand();        
    }
    
    public void testIsMultipart() throws Exception
    {
        when(blogImageContext.getRequest()).thenReturn(request);
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("multipart/form-data");
        
        boolean doContinue = toTest.execute(blogImageContext);
        
        assertTrue("Not a multipart request.", doContinue);
    }
    
    public void testIsNotMultipart() throws Exception
    {
        when(blogImageContext.getRequest()).thenReturn(request);
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorUploadResponse);
        
        boolean doContinue = toTest.execute(blogImageContext);
        
        assertFalse("Was a multipart request.", doContinue);
    }
}
