package example.content.file;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.atex.plugins.baseline.url.UrlResolver;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.servlet.URLBuilder;

/**
 * An file URL resolver to be used when creating URL to a file.
 *
 */
public class FileURLResolver implements UrlResolver {
    
    private static final Logger LOG = Logger.getLogger(FileURLResolver.class.getName());
    
    URLBuilder urlBuilder;
    private HttpServletRequest request;
    
    public FileURLResolver(HttpServletRequest request) {
        this.request = request;
        urlBuilder = new URLBuilder();
    }
    
    public String getFileUrl(ContentId contentId, String filePath) {
        String fileURL = null;
        try {
            fileURL = urlBuilder.createFileUrl(contentId, filePath, request);
            
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Failed to create file URL", e);
        }
        
        return fileURL;
    }

}
