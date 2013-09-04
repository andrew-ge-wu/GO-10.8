package example.content.file;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;

import junit.framework.TestCase;

import com.polopoly.cm.client.Content;

public class FileExistsCheckerTest extends TestCase {
    FileExistsChecker target;
    private Content content;

    
    
    protected void setUp() throws Exception {
        super.setUp();
        
        content = (Content) mock(Content.class);
        target = new FileExistsChecker(content);
                       
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testShouldReturnTrueWhenFileExists() throws Exception {
        String fileName = "name.png";
        
        content.getFileInfo(fileName);
        
        boolean exists = target.fileExists(fileName);
        
        assertTrue(exists);
        
    }

    
    public void testShouldReturnFalseWhenFileDoesNotExist() throws Exception {
        
        String fileName = "nonexisting.png";
        
        when(content.getFileInfo(fileName)).thenThrow(new FileNotFoundException());

        boolean exists = target.fileExists(fileName);
        
        assertFalse(exists);
        
    }

}
