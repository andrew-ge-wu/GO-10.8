package example.content.file;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;

/**
 * Class for testing existence of files.
 */
public class FileExistsChecker {

    private final Content content;

    public FileExistsChecker(Content content) {
        this.content = content;
    }

    /**
     * Checks if a file exists.
     * 
     * @param fileName
     * @return <code>true</code> if the file exists, <code>false</code>
     * otherwise
     * @throws CMException
     * @throws IOException
     */
    public boolean fileExists(String fileName) throws CMException, IOException {
        
        boolean exists;
        try {
            content.getFileInfo(fileName);
            exists = true;
        } catch (FileNotFoundException e) {
            exists = false;
        }
                
        return exists;
    }

}
