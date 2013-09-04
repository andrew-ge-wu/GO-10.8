package example.layout.element.mynewslist;

import com.polopoly.cm.ContentId;
import com.polopoly.model.Model;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

import example.content.RenderControllerExtended;

/**
 * Render controller for the MyNewsList element.
 */
public class RenderControllerMyNewsListElement
    extends RenderControllerExtended
{
    public static final String PARAMETER_TOPIC = "topic";
    public static final String PARAMETER_MODE = "mode";

    public static final String ATTRIBUTE_TOPIC_MODEL_NAME = "topicList";
    public static final String ATTRIBUTE_TOPIC_NAME = "topicName";
    
    public static final String ATTRIBUTE_REQUEST_TOPIC = "requestTopic";
    public static final String ATTRIBUTE_REQUEST_MODE = "requestMode";
    
    public static final String MODE_AJAX = "ajax";

    private TopicListWorker topicListWorker;

    public RenderControllerMyNewsListElement()
    {
        topicListWorker = new TopicListWorker();
    }
    
    public void populateModelBeforeCacheKey(RenderRequest request, TopModel m,
            ControllerContext context) {
        super.populateModelBeforeCacheKey(request, m, context);
        
        ContentId topic = topicListWorker.getTopicContentIdFromRequest(request);
        String mode = topicListWorker.getModeStringFromRequest(request);
        
        m.getLocal().setAttribute(ATTRIBUTE_REQUEST_TOPIC, topic);
        m.getLocal().setAttribute(ATTRIBUTE_REQUEST_MODE, mode);
    }

    public void populateModelAfterCacheKey(RenderRequest request,
                                           TopModel m,
                                           CacheInfo cacheInfo,
                                           ControllerContext context)
    {
        if (topicListWorker.isAjaxRequestMode(request)) {
            ModelWrite localModel = m.getLocal();
            Model contentModel = context.getContentModel();
            
            ContentId topicContentId =
                topicListWorker.getTopicContentIdFromRequest(request);
            
            if (topicContentId != null) {
                topicListWorker.populateLocalModelWithTopicList(contentModel,
                                                                localModel,
                                                                topicContentId);
            }
            
        }
    }

    void setTopicListWorker(TopicListWorker topicListWorker)
    {
        this.topicListWorker = topicListWorker;
    }
}
