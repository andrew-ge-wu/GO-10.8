package example.blog.image;

import junit.framework.TestCase;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionedContentId;

public class FckEditorUploadResponseTest
    extends TestCase
{
    private FckEditorUploadResponse _toTest;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        _toTest = new FckEditorUploadResponse();
    }
    
    public void testDefaultStatus() throws Exception
    {
        assertTrue("Wrong default status returned.",
                   _toTest.getStatus() == FckEditorUploadResponse.Status.OK);
    }

    public void testToString() throws Exception    
    {
        ContentId blogPostContentId = new ContentId(19, 100);
        String imageFilePath = "images/123456789.jpg";        
        
        _toTest.setStatus(FckEditorUploadResponse.Status.SERVER_ERROR);
        _toTest.setBlogPostContentId(blogPostContentId);        
        _toTest.setImageFilePath(imageFilePath);
        
        String expected = "<script type=\"text/javascript\">\n(function(){"
                          + "var d=document.domain;while (true){try{"
                          + "var A=window.parent.document.domain;break;}catch(e) {};"
                          + "d=d.replace(/.*?(?:\\.|$)/,'');if (d.length==0) break;"
                          + "try{document.domain=d;}catch (e){break;}}})();\n"
                          + "window.parent.OnUploadCompleted("
                          + FckEditorUploadResponse.Status.SERVER_ERROR.getCode()
                          + ",'"
                          + "19.100"
                          + "','"
                          + imageFilePath
                          + "');\n</script>";
        
        assertEquals("Wrong javascript returned.", expected, _toTest.toString());
    }
    
    public void testToStringWithVersionedContentId() throws Exception    
    {
        ContentId blogPostContentId = new VersionedContentId(19, 100, 12345);
        String imageFilePath = "images/123456789.jpg";        
        
        _toTest.setStatus(FckEditorUploadResponse.Status.PERMISSION_DENIED);
        _toTest.setBlogPostContentId(blogPostContentId);        
        _toTest.setImageFilePath(imageFilePath);
        
        String expected = "<script type=\"text/javascript\">\n(function(){"
                        + "var d=document.domain;while (true){try{"
                        + "var A=window.parent.document.domain;break;}catch(e) {};"
                        + "d=d.replace(/.*?(?:\\.|$)/,'');if (d.length==0) break;"
                        + "try{document.domain=d;}catch (e){break;}}})();\n"
                        + "window.parent.OnUploadCompleted("
                          + FckEditorUploadResponse.Status.PERMISSION_DENIED.getCode()
                          + ",'"
                          + "19.100.12345"
                          + "','"
                          + imageFilePath
                          + "');\n</script>";
        
        assertEquals("Wrong javascript returned for versioned content id.", expected, _toTest.toString());
    }
}
