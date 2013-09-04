package example.content.rss;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

import example.content.RenderControllerExtended;

/**
 * ModelBuilder used to construct the RSS link whcih will be included in the
 * HTML <code>HEAD</code> declaration.
 */
public class RenderControllerRssLink
    extends RenderControllerExtended
{
    public void populateModelBeforeCacheKey(RenderRequest request, TopModel m, ControllerContext context)
    {
        // Site Url used to create absolute links
        ModelPathUtil.set(m.getLocal(), "siteUrl", getSiteUrl((HttpServletRequest) request));
    }

    /**
     * Method to get the absolute site url (temporary solution)
     *
     * @param req
     * @return <code><protocol>://<address>:<port></code>
     */
    protected String getSiteUrl(HttpServletRequest req)
    {
        StringBuffer ret = new StringBuffer();
        ret.append(req.getScheme());
        ret.append("://");
        ret.append(req.getServerName());
        if (req.getServerPort() != 80) {
            ret.append(":");
            ret.append(req.getServerPort());
        }

        return ret.toString();
    }
}
