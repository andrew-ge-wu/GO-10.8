package example.content.link;

import com.atex.plugins.baseline.url.UrlResolver;
import com.polopoly.cm.app.policy.ContentReferencePolicy;
import com.polopoly.cm.app.policy.SelectableSubFieldPolicy;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;

import example.content.ContentBasePolicy;
import example.content.ResourceBasePolicy;

/**
 * Policy representing a link resource.
 */
public class LinkResourcePolicy
    extends ResourceBasePolicy
{

    public String getName()
        throws CMException
    {
        // As default, use the name of the link content itself
        
        String contentName = super.getName();

        if (null != contentName && contentName.trim().length() > 0) {
            return contentName;
        }
        
        // Fallback on the external URL or the name of the linked content
        
        SelectableSubFieldPolicy subFieldPolicy =
            (SelectableSubFieldPolicy) getChildPolicy("link");

        if (null != subFieldPolicy) {
            String selected = subFieldPolicy.getSelectedSubFieldName();
            
            if (null != selected) {
                ContentBasePolicy selectedFieldPolicy = (ContentBasePolicy) subFieldPolicy.getChildPolicy(selected);
                
                if ("internal".equals(selected)) {
                  ContentReferencePolicy contentRef =
                      (ContentReferencePolicy) selectedFieldPolicy.getChildPolicy("content");
                  
                  return getInternalLinkName(contentRef);
                } else if ("external".equals(selected)) {
                    SingleValuePolicy urlPolicy =
                        (SingleValuePolicy) selectedFieldPolicy.getChildPolicy("href");
                    
                    return getExternalLinkName(urlPolicy);
                }
            }
        }

        return "";
    }
    
    private String getExternalLinkName(SingleValuePolicy urlLinkPolicy)
        throws CMException {
        
        if (null != urlLinkPolicy) {
            String url = urlLinkPolicy.getValue();

            if (url.startsWith("http://")) {
                url = url.substring(7);
            }

            return url;
        }
        
        return "";
    }
    
    private String getInternalLinkName(ContentReferencePolicy contentSelectPolicy)
        throws CMException {
        
        if (null != contentSelectPolicy) {
            return getCMServer().getContent(
                    contentSelectPolicy.getReference()).getName();
        }

        return "";
    }
    
    public String getThumbnailPath(UrlResolver urlResolver)
    {
        return null;
    }
    
    public String getPreviewPath(UrlResolver urlResolver)
    {
        return null;
    }

    public String getPathSegmentString() throws CMException {
        return null;
    }
}
