package example.layout.element.mynewslist;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.model.Model;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;

public class TopicListWorker
{
    private static final String CLASS = TopicListWorker.class.getName();
    private static final Logger LOG = Logger.getLogger(CLASS);
    
    private final LegacyModelUtil modelUtil;
    private final LegacyContentIdCreator contentIdCreator;
    
    public TopicListWorker()
    {
        this(new LegacyModelUtil(), new LegacyContentIdCreator());
    }
    
    public TopicListWorker(LegacyModelUtil modelUtil,
                           LegacyContentIdCreator contentIdCreator)
    {
        this.modelUtil = modelUtil;
        this.contentIdCreator = contentIdCreator;
    }
    
    public boolean isAjaxRequestMode(RenderRequest renderRequest)
    {
        String mode =
            renderRequest.getParameter(RenderControllerMyNewsListElement.PARAMETER_MODE);
        
        if (RenderControllerMyNewsListElement.MODE_AJAX.equals(mode)) {
            return true;
        }
        
        return false;
    }
    
    public String getModeStringFromRequest(RenderRequest renderRequest)
    {
        return renderRequest.getParameter(RenderControllerMyNewsListElement.PARAMETER_MODE);
    }
    
    public ContentId getTopicContentIdFromRequest(RenderRequest renderRequest)
    {
        String parameter =
            renderRequest.getParameter(RenderControllerMyNewsListElement.PARAMETER_TOPIC);
        
        ContentId topicContentId = null;
        
        if (parameter != null) {
            try {
                topicContentId = contentIdCreator.createContentId(parameter);
            } catch (IllegalArgumentException iae) {
                LOG.log(Level.FINE, "Could not render topic with content id '" + parameter
                                + "'. Illegal content id.", iae);
            }
        }
        
        return topicContentId;
    }

    public void populateLocalModelWithTopicList(Model contentModel,
                                                ModelWrite localModel,
                                                ContentId topicContentId)
    {
        List<?> list = (List<?>) modelUtil.get(contentModel, "publishingQueues/list");

        for (Object model : list)
        {
            Model pqReferenceModel = (Model) model;

            ContentId pqContentId = (ContentId) modelUtil.get(pqReferenceModel, "contentId");

            if (topicContentId.equalsIgnoreVersion(pqContentId))
            {
                Model pqModel = (Model) pqReferenceModel.getAttribute("content");

                localModel.setAttribute(RenderControllerMyNewsListElement.ATTRIBUTE_TOPIC_MODEL_NAME,
                                        modelUtil.get(pqModel, "contentList"));
                localModel.setAttribute(RenderControllerMyNewsListElement.ATTRIBUTE_TOPIC_NAME,
                                        modelUtil.get(pqModel, "name"));

                return;
            }
        }
    }
    
}