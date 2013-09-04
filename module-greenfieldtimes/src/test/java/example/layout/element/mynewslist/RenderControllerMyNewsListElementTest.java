package example.layout.element.mynewslist;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mock;
import org.mockito.internal.matchers.Any;

import com.polopoly.cm.ContentId;
import com.polopoly.model.Model;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

import example.MockitoBase;

public class RenderControllerMyNewsListElementTest extends MockitoBase
{
    @Mock
    LegacyModelUtil modelUtil;
    
    @Mock
    LegacyContentIdCreator contentIdCreator;
    
    @Mock
    RenderRequest renderRequest;
    
    @Mock
    Model contentModel;
    
    @Mock
    ModelWrite localModel;
    
    @Mock
    TopModel topModel;
    
    @Mock
    ControllerContext context;

    @Mock
    TopicListWorker topicListWorker;
    
    @Mock
    CacheInfo cacheInfo;
    
    private RenderControllerMyNewsListElement toTest;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        toTest = new RenderControllerMyNewsListElement();
        toTest.setTopicListWorker(topicListWorker);
    }
    
    public void testGetTopicContentIdFromRequest()
        throws Exception
    {
        ContentId actualContentId = new ContentId(1, 124);
        
        TopicListWorker topicListWorker = new TopicListWorker(modelUtil, contentIdCreator);
        
        when(renderRequest.getParameter(RenderControllerMyNewsListElement.PARAMETER_TOPIC)).thenReturn("1.124");
        when(contentIdCreator.createContentId("1.124")).thenReturn(actualContentId);
        
        ContentId resultingContentId = topicListWorker.getTopicContentIdFromRequest(renderRequest);

        verify(renderRequest).getParameter(RenderControllerMyNewsListElement.PARAMETER_TOPIC);
        verify(contentIdCreator).createContentId("1.124");
        
        assertEquals(resultingContentId, actualContentId);
    }
    
    public void testGetTopicContentIdFromRequestNoTopic()
        throws Exception
    {
        TopicListWorker topicListWorker = new TopicListWorker(modelUtil, contentIdCreator);
        
        when(renderRequest.getParameter(RenderControllerMyNewsListElement.PARAMETER_TOPIC)).thenReturn(null);
        
        ContentId resultingContentId = topicListWorker.getTopicContentIdFromRequest(renderRequest);
    
        verify(renderRequest, times(1)).getParameter(RenderControllerMyNewsListElement.PARAMETER_TOPIC);
        verify(contentIdCreator, never()).createContentId("1.124");
        
        assertNull(resultingContentId);
    }
    
    public void testGetTopicContentIdFromRequestInvalidTopic()
        throws Exception
    {
        TopicListWorker topicListWorker = new TopicListWorker(modelUtil, contentIdCreator);
        
        when(renderRequest.getParameter(RenderControllerMyNewsListElement.PARAMETER_TOPIC)).thenReturn("thisIsNotAValidContentId");
        when(contentIdCreator.createContentId("thisIsNotAValidContentId")).thenThrow(new IllegalArgumentException());

        ContentId resultingContentId = topicListWorker.getTopicContentIdFromRequest(renderRequest);
        
        verify(renderRequest,
               times(1)).getParameter(RenderControllerMyNewsListElement.PARAMETER_TOPIC);
        verify(contentIdCreator,
               times(1)).createContentId("thisIsNotAValidContentId");
        
        assertNull(resultingContentId);
    }
    
    public void testPopulateLocalModelWithTopicList()
        throws Exception
    {
        TopicListWorker topicListWorker =
            new TopicListWorker(modelUtil, contentIdCreator);
        
        List<Model> list = new ArrayList<Model>();
        
        Model model1 = mock(Model.class);
        Model model2 = mock(Model.class);
        Model model3 = mock(Model.class);

        list.add(model1);
        list.add(model2);
        list.add(model3);
        
        Model contentModel = mock(Model.class);
        ContentId topicContentId = new ContentId(1, 124);

        when(modelUtil.get(this.contentModel,
                           "publishingQueues/list")).thenReturn((Object) list);
        
        when(modelUtil.get(model1, "contentId")).thenReturn(new ContentId(1, 130));      
        when(modelUtil.get(model2, "contentId")).thenReturn(new ContentId(1, 131));

        when(modelUtil.get(model3, "contentId")).thenReturn(new ContentId(1, 124));
        when(model3.getAttribute("content")).thenReturn(contentModel);
        when(modelUtil.get(contentModel, "contentList")).thenReturn(Any.ANY);
        when(modelUtil.get(contentModel, "name")).thenReturn(Any.ANY);

        topicListWorker.populateLocalModelWithTopicList(this.contentModel,
                                                        localModel,
                                                        topicContentId);
        
        verify(localModel,
               times(1)).setAttribute(RenderControllerMyNewsListElement.ATTRIBUTE_TOPIC_MODEL_NAME,
                                      Any.ANY);
        verify(localModel,
               times(1)).setAttribute(RenderControllerMyNewsListElement.ATTRIBUTE_TOPIC_NAME,
                                      Any.ANY);
    }
    
    public void testPopulateLocalModelWithTopicListEmptyList()
        throws Exception
    {
        TopicListWorker topicListWorker =
            new TopicListWorker(modelUtil, contentIdCreator);
        
        List<Model> list = new ArrayList<Model>();
        
        ContentId topicContentId = new ContentId(1, 124);
    
        when(modelUtil.get(this.contentModel,
                           "publishingQueues/list")).thenReturn((Object) list);
        
        topicListWorker.populateLocalModelWithTopicList(this.contentModel,
                                                        localModel,
                                                        topicContentId);
        
        verify(localModel,
               never()).setAttribute(RenderControllerMyNewsListElement.ATTRIBUTE_TOPIC_MODEL_NAME,
                                     Any.ANY);
        verify(localModel,
               never()).setAttribute(RenderControllerMyNewsListElement.ATTRIBUTE_TOPIC_NAME,
                                     Any.ANY);
    }
    
    public void testPopulateLocalModelWithTopicListNoMatch()
        throws Exception
    {
        TopicListWorker topicListWorker =
            new TopicListWorker(modelUtil, contentIdCreator);
        
        List<Model> list = new ArrayList<Model>();
        
        Model model1 = mock(Model.class);
        Model model2 = mock(Model.class);
        Model model3 = mock(Model.class);

        list.add(model1);
        list.add(model2);
        list.add(model3);
        
        ContentId topicContentId = new ContentId(1, 124);

        when(modelUtil.get(this.contentModel,
                           "publishingQueues/list")).thenReturn(list);
        
        when(modelUtil.get(model1, "contentId")).thenReturn(new ContentId(1, 130));      
        when(modelUtil.get(model2, "contentId")).thenReturn(new ContentId(1, 131));
        when(modelUtil.get(model3, "contentId")).thenReturn(new ContentId(1, 132));
        
        topicListWorker.populateLocalModelWithTopicList(this.contentModel,
                                                        localModel,
                                                        topicContentId);
        
        verify(localModel,
               never()).setAttribute(RenderControllerMyNewsListElement.ATTRIBUTE_TOPIC_MODEL_NAME,
                                     Any.ANY);
        verify(localModel,
               never()).setAttribute(RenderControllerMyNewsListElement.ATTRIBUTE_TOPIC_NAME,
                                     Any.ANY);
    }
    
    public void testIsAjaxRequestMode()
        throws Exception
    {
        TopicListWorker topicListWorker =
            new TopicListWorker(modelUtil, contentIdCreator);
        
        when(renderRequest.getParameter(RenderControllerMyNewsListElement.PARAMETER_MODE)).thenReturn("ajax");

        boolean isAjaxMode = topicListWorker.isAjaxRequestMode(renderRequest);
        
        assertTrue("Should be ajax mode.", isAjaxMode);
    }
    
    public void testIsAjaxRequestModeNotAjaxMode()
        throws Exception
    {
        TopicListWorker topicListWorker =
            new TopicListWorker(modelUtil, contentIdCreator);
        
        when(renderRequest.getParameter(RenderControllerMyNewsListElement.PARAMETER_MODE)).thenReturn("www");
    
        boolean isAjaxMode = topicListWorker.isAjaxRequestMode(renderRequest);
        
        assertFalse("Should not be ajax mode.", isAjaxMode);
    }
    
    public void testIsAjaxRequestModeNoMode()
        throws Exception
    {
        TopicListWorker topicListWorker =
            new TopicListWorker(modelUtil, contentIdCreator);
        
        when(renderRequest.getParameter(RenderControllerMyNewsListElement.PARAMETER_MODE)).thenReturn(null);
    
        boolean isAjaxMode = topicListWorker.isAjaxRequestMode(renderRequest);
        
        assertFalse("Should not be ajax mode.", isAjaxMode);
    }
    
    public void testPopulateModelAfterCacheKey()
        throws Exception
    {
        when(topicListWorker.isAjaxRequestMode(renderRequest)).thenReturn(true);
        when(topModel.getLocal()).thenReturn(localModel);
        when(context.getContentModel()).thenReturn(contentModel);
        
        when(topicListWorker.getTopicContentIdFromRequest(renderRequest)).thenReturn(new ContentId(1, 124));

        toTest.populateModelAfterCacheKey(renderRequest, topModel, cacheInfo, context);
        
        verify(topicListWorker,
               times(1)).populateLocalModelWithTopicList(contentModel,
                                                         localModel,
                                                         new ContentId(1, 124));
    }
    
    public void testPopulateModelAfterCacheKeyNotAjaxMode()
        throws Exception
    {
        when(topicListWorker.isAjaxRequestMode(renderRequest)).thenReturn(false);
    
        toTest.populateModelAfterCacheKey(renderRequest, topModel, cacheInfo, context);
       
        verify(topicListWorker,
               never()).populateLocalModelWithTopicList(contentModel,
                                                        localModel,
                                                        new ContentId(1, 124));
    }
    
    public void testPopulateModelAfterCacheKeyNoTopic()
        throws Exception
    {
        when(topModel.getLocal()).thenReturn(localModel);
        when(context.getContentModel()).thenReturn(contentModel);
        
        when(topicListWorker.getTopicContentIdFromRequest(renderRequest)).thenReturn(null);
    
        toTest.populateModelAfterCacheKey(renderRequest, topModel, cacheInfo, context);
        
        verify(topicListWorker,
               never()).populateLocalModelWithTopicList(contentModel,
                                                        localModel,
                                                        new ContentId(1, 124));
    }
    
    public void testGetModeStringFromRequest()
        throws Exception
    {
        TopicListWorker topicListWorker =
            new TopicListWorker(modelUtil, contentIdCreator);
        
        when(renderRequest.getParameter(RenderControllerMyNewsListElement.PARAMETER_MODE)).thenReturn("ajax");
    
        String modeString = topicListWorker.getModeStringFromRequest(renderRequest);
        
        assertEquals(modeString, "ajax");
    }

    public void testPopulateModelBeforeCacheKey()
        throws Exception
    {
        when(topicListWorker.getTopicContentIdFromRequest(renderRequest)).thenReturn(new ContentId(1, 124));
        when(topicListWorker.getModeStringFromRequest(renderRequest)).thenReturn("ajax");
        
        when(topModel.getLocal()).thenReturn(localModel);
        
        toTest.populateModelBeforeCacheKey(renderRequest, topModel, context);
        
        verify(topicListWorker,
               times(1)).getTopicContentIdFromRequest(renderRequest);
        verify(topicListWorker,
               times(1)).getModeStringFromRequest(renderRequest);
        
        verify(localModel,
               times(1)).setAttribute(RenderControllerMyNewsListElement.ATTRIBUTE_REQUEST_TOPIC,
                                      new ContentId(1, 124));
        verify(localModel,
               times(1)).setAttribute(RenderControllerMyNewsListElement.ATTRIBUTE_REQUEST_MODE,
                                      "ajax");
    }
    
}
