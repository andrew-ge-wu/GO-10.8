package example.content.link;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.path.ContentPathCreator;
import com.polopoly.cm.servlet.RequestPreparator;
import com.polopoly.cm.servlet.URLBuilder;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

import example.content.RenderControllerExtended;

/**
 * Render controller for Link Resource.
 *
 * <p>
 * If the request attribute ATTR_NAME_LINK_TEXT is present,
 * it is used as link text.
 *
 * <p>
 * If the request attribute ATTR_NAME_LINK_ATTRS is present, all entries
 * in this map will be uses as attributes on the a tag. Note that the href
 * attribute will never be inherited.
 */
public class RenderControllerLinkResource extends RenderControllerExtended
{
    static final String ATTR_NAME_LINK_TEXT = "linkText";
    static final String ATTR_NAME_LINK_ATTRS = "linkAttrs";

    private static final Logger LOG = Logger.getLogger(RenderControllerLinkResource.class.getName());

    @SuppressWarnings("unchecked")
    @Override
    public void populateModelBeforeCacheKey(RenderRequest request,
                                            TopModel m,
                                            ControllerContext context)
    {
        ModelWrite localModel = m.getLocal();

        // Get link name
        String linkText = (String) request.getAttribute(ATTR_NAME_LINK_TEXT);

        if (linkText == null) {
            linkText = (String) ModelPathUtil.get(localModel, "content/name");
        }

        HashMap<String, String> attrs = new HashMap<String, String>();

        // Inherit attributes
        HashMap<String, String> inheritedAttrs = (HashMap<String, String>) request.getAttribute(ATTR_NAME_LINK_ATTRS);

        if (inheritedAttrs != null) {
            attrs.putAll(inheritedAttrs);
        }

        // Get/create href and put in attrs
        String linkType = (String) ModelPathUtil.get(localModel, "content/link/selectedName");
        String href = null;

        if (!"internal".equals(linkType)) {
            href = (String) ModelPathUtil.get(localModel, "content/link/selected/href/value");

            if (linkText == null) {
                linkText = href;
            }
        } else {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            URLBuilder urlBuilder = RequestPreparator.getURLBuilder(httpRequest);

            ContentId contentId = (ContentId) ModelPathUtil.get(localModel, "content/link/selected/content/contentId");
            ContentPathCreator path = RequestPreparator.getPathCreator(httpRequest);

            try {
                href = urlBuilder.createUrl(path.createPath(contentId,
                                                            _policyCMServerProvider.getPolicyCMServer(context)),
                                                            null,
                                                            httpRequest);

                if (linkText == null) {
                    linkText = _policyCMServerProvider.getPolicyCMServer(context).getContent(contentId).getName();
                }
            } catch (CMException e) {
                LOG.log(Level.WARNING, "Could not create internal link.", e);
            }
        }

        attrs.put("href", href);

        // Get title and put in attrs
        if (attrs.get("title") == null) {
            String title = (String) ModelPathUtil.get(localModel, "content/title/value");

            if (title != null) {
                attrs.put("title", title);
            }
        }

        // Put link text and attributes in model
        localModel.setAttribute("text", linkText);
        localModel.setAttribute("attrs", attrs);
    }
}
