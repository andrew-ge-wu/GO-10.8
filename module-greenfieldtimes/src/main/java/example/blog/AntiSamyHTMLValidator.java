package example.blog;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;

public class AntiSamyHTMLValidator implements HTMLValidator {
    
    private static final Logger LOG = Logger.getLogger(AntiSamyHTMLValidator.class.getName());
    private static final String BLOG_CONTENT_WHITELIST_XML = "blog-content-whitelist.xml";
    private static final String COMMENT_WHITELIST_XML      = "comment-whitelist.xml";
    
    private AtomicReference<Policy> lenientPolicy = new AtomicReference<Policy>();
    private AtomicReference<Policy> strictPolicy = new AtomicReference<Policy>();
    
    private final AntiSamy antiSamy = new AntiSamy();
    
    public String getCleanHTML(String dirtyHTML)
    throws InvalidHTMLException {
      setupHTMLValidator();
      try {
          CleanResults cr = antiSamy.scan(dirtyHTML, lenientPolicy.get());
          return cr.getCleanHTML();
      } catch (PolicyException e) {
          throw new InvalidHTMLException(e);
      } catch (ScanException e) {
          throw new InvalidHTMLException(e);
      }
  }
    
    public String stripAllHTML(String dirtyHTML)
    throws InvalidHTMLException {
        setupHTMLValidator();
        try {
            CleanResults cr = antiSamy.scan(dirtyHTML, strictPolicy.get());
            return cr.getCleanHTML();
        } catch (PolicyException e) {
            throw new InvalidHTMLException(e);
        } catch (ScanException e) {
            throw new InvalidHTMLException(e);
        }
    }

    private void setupHTMLValidator() {
        if (lenientPolicy.get() == null) {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            InputStream lenientPolicyFile = contextClassLoader.getResourceAsStream(BLOG_CONTENT_WHITELIST_XML);
            InputStream strictPolicyFile = contextClassLoader.getResourceAsStream(COMMENT_WHITELIST_XML);
            try {
                lenientPolicy.set(Policy.getInstance(lenientPolicyFile));
                strictPolicy.set(Policy.getInstance(strictPolicyFile));
            } catch (PolicyException e) {
                LOG.log(Level.SEVERE, "Error while creating blog post.", e);
            }
      }
  }
}
