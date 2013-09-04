package example.content.flash;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.plugins.baseline.url.UrlResolver;
import com.polopoly.cm.app.policy.FilePolicy;
import com.polopoly.cm.app.policy.NumberInputPolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ComponentMap;
import com.polopoly.cm.policy.Policy;

import example.content.ResourceBasePolicy;

/**
 * Policy representing a flash resource.
 */

public class FlashResourcePolicy
    extends ResourceBasePolicy
{
    private static final Logger LOG = Logger.getLogger(FlashResourcePolicy.class.getName());

    private static final int DEFAULT_HEIGHT = 384;
    private static final int DEFAULT_WIDTH = 384;
    
    public String getThumbnailPath(UrlResolver urlResolver) {
        return null;
    }

    public String getPreviewPath(UrlResolver urlResolver) {
        try {
            String path = getFilePolicy().getFullFilePath();
            return (path != null)
                ? urlResolver.getFileUrl(getContentId(), path)
                : null;
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Unable to resolve file preview path", e);
        }
        return  null;
    }

    public int getWidth() throws CMException {
        return ((NumberInputPolicy) getChildPolicy("width")).
            getIntValue(DEFAULT_WIDTH);
    }

    public int getHeight() throws CMException {
        return ((NumberInputPolicy) getChildPolicy("height")).
            getIntValue(DEFAULT_HEIGHT);
    }

    public Map<?, ?> getParameters()
        throws CMException
    {
        Policy childPolicy = getChildPolicy("parameters");
        return new ComponentMap(childPolicy.getContent(), childPolicy.getPolicyName());
    }

    public String[] getParameterNames() throws CMException {
        return getChildPolicy("parameters").getComponentNames();
    }

    public String getParameterValue(String name) throws CMException {
        return getChildPolicy("parameters").getComponent(name);
    }

    public FilePolicy getFilePolicy() throws CMException {
        return (FilePolicy) getChildPolicy("file");
    }
}
