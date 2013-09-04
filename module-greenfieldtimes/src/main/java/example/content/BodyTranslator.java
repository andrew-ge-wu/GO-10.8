package example.content;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.servlet.HtmlPathUtil;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.path.ContentPathCreator;
import com.polopoly.cm.servlet.RequestPreparator;
import com.polopoly.cm.servlet.URLBuilder;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;

/**
 * Translator for Polopoly content links and images.
 */
public class BodyTranslator {

    private static final Logger LOG = Logger.getLogger(BodyTranslator.class.getName());

    /**
     * Translate the body text part (body/value) of the given model so that
     * Polopoly content links and images are re-written to valid URLs.
     *
     * @param request the request
     * @param localModel the model containing the text
     * @param inPreviewMode
     */
    public void translateBody(RenderRequest request,
                              ModelWrite localModel,
                              String bodyKey,
                              boolean inPreviewMode)
    {
        Model contentModel = (Model) ModelPathUtil.get(localModel, "content");
        String body = (String) ModelPathUtil.get(contentModel, bodyKey);

        if (body != null) {
            ContentId contentId = (ContentId) ModelPathUtil.get(contentModel, "contentId");

            String parsedBody = translateBody(request,
                                              contentId,
                                              body,
                                              inPreviewMode,
                                              inPreviewMode);

            localModel.setAttribute("parsedbody", parsedBody);
        }
    }

    public String translateBody(RenderRequest request,
                                ContentId contentId,
                                String body,
                                boolean inPreviewMode,
                                boolean keepPolopolyAttrs)
    {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        ContentPathCreator pathCreator =
            RequestPreparator.getPathCreator(httpServletRequest);

        URLBuilder builder =
            RequestPreparator.getURLBuilder(httpServletRequest);

        // Use latest version when previewing
        VersionedContentId latestVersionContentId = null;

        if (inPreviewMode) {
            latestVersionContentId = contentId.getLatestVersionId();
        }

        try {
            if (keepPolopolyAttrs) {
                return HtmlPathUtil.pathify(body, pathCreator, builder,
                                            httpServletRequest,
                                            latestVersionContentId, true);
            } else {
                return HtmlPathUtil.pathify(body, pathCreator, builder,
                                            httpServletRequest,
                                            latestVersionContentId);
            }
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Could not parse body.", e);

            return body;
        }
    }
}
