package example.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.tools.view.context.ViewContext;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.servlet.RequestPreparator;
import com.polopoly.siteengine.preview.InteractivePreviewUtil;

public class PreviewUtilTool {

    private InteractivePreviewUtil _interactivePreviewUtil;

    /**
     * Initializes the tool by getting cm server from request from initParam
     * which should be of type ViewContext.
     * 
     * @param initParam
     *            the current ViewContext
     */
    public void init(Object initParam) {
        if (initParam instanceof ViewContext) {
            HttpServletRequest request = ((ViewContext) initParam).getRequest();
            _interactivePreviewUtil =
                new InteractivePreviewUtil(RequestPreparator.getCMServer(request));
        } else {
            throw new IllegalArgumentException("initParam not of type ViewContext. Need request to get cm server.");
        }
    }

    public String getInteractivePreviewEditableObjectName(ContentId contentId) throws CMException {
        return _interactivePreviewUtil.getInteractivePreviewEditableObjectName(contentId);
    }
}
