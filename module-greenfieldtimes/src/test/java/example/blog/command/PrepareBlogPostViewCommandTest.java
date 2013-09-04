package example.blog.command;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mock;

import com.polopoly.cm.ContentId;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.model.context.ContextScope;
import com.polopoly.siteengine.model.context.PageScope;
import com.polopoly.siteengine.model.request.ContentPath;

import example.MockitoBase;
import example.blog.BlogContext;
import example.blog.RenderControllerBlog;
import example.util.RequestParameterUtil;

public class PrepareBlogPostViewCommandTest extends MockitoBase
{
    private PrepareBlogPostViewCommand _toTest;
    
    @Mock private BlogContext _blogContext;
    @Mock private TopModel _topModel;
    @Mock private ContextScope _contextScope;
    @Mock private PageScope _pageScope;
    @Mock private RenderRequest _renderRequest;
    @Mock private ControllerContext _controllerContext;
    @Mock private ContentPath _contentPath;
    @Mock private ModelWrite _localModel;

    private ContentId _blogPostId = new ContentId(19, 4321);
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        _toTest = new PrepareBlogPostViewCommand();
    }
    
    public void testWwwMode() throws Exception
    {
        when(_blogContext.getTopModel()).thenReturn(_topModel);
        when(_topModel.getContext()).thenReturn(_contextScope);
        when(_blogContext.getRenderRequest()).thenReturn(_renderRequest);
        
        when(_blogContext.getControllerContext()).thenReturn(_controllerContext);
        when(_controllerContext.getMode()).thenReturn(null);
        when(_contextScope.getPage()).thenReturn(_pageScope);
        when(_pageScope.getPathAfterPage()).thenReturn(_contentPath);                
        
        // Case 1 - List view (part of blog)
        when(_contentPath.size()).thenReturn(1);        
        assertFalse("Chain should terminate in www mode.",
                    _toTest.execute(_blogContext));
        verify(_localModel, never()).setAttribute(eq(RenderControllerBlog.BLOG_POST_ID),
                                                  eq(_blogPostId));
        
        // Case 2 - Single view
        when(_contentPath.size()).thenReturn(2);
        when(_contentPath.get(1)).thenReturn(_blogPostId);             
        when(_blogContext.getLocalModel()).thenReturn(_localModel);
        assertFalse("Chain should terminate in www mode.",
                    _toTest.execute(_blogContext));
        verify(_localModel).setAttribute(eq(RenderControllerBlog.BLOG_POST_ID),
                                         eq(_blogPostId));
    }
    
    public void testAjaxMode() throws Exception
    {
        when(_blogContext.getTopModel()).thenReturn(_topModel);
        when(_topModel.getContext()).thenReturn(_contextScope);
        when(_blogContext.getRenderRequest()).thenReturn(_renderRequest);
        when(_renderRequest.getParameter(RequestParameterUtil.PARAMETER_AJAX)).thenReturn("true");        
        
        assertTrue("Chain should continue in ajax mode.",
                   _toTest.execute(_blogContext));
        verify(_localModel, never()).setAttribute(any(String.class),
                                                  any(ContentId.class));
    }
    
    public void testMobileMode() throws Exception
    {
        when(_blogContext.getTopModel()).thenReturn(_topModel);
        when(_topModel.getContext()).thenReturn(_contextScope);
        when(_blogContext.getRenderRequest()).thenReturn(_renderRequest);
        
        when(_blogContext.getControllerContext()).thenReturn(_controllerContext);
        when(_controllerContext.getMode()).thenReturn("mobile");
        when(_contextScope.getPage()).thenReturn(_pageScope);
        when(_pageScope.getPathAfterPage()).thenReturn(_contentPath);
        
        // Case 1 - List view (part of blog)
        when(_contentPath.size()).thenReturn(1);
        assertTrue("Chain should continue in mobile mode.",
                   _toTest.execute(_blogContext));
        verify(_localModel, never()).setAttribute(eq(RenderControllerBlog.BLOG_POST_ID),
                                                  eq(_blogPostId));
        
        // Case 2 - Single view
        when(_contentPath.size()).thenReturn(2);
        when(_contentPath.get(1)).thenReturn(_blogPostId);
        when(_blogContext.getLocalModel()).thenReturn(_localModel);     
        assertTrue("Chain should terminate in www mode.",
                    _toTest.execute(_blogContext));
        verify(_localModel).setAttribute(eq(RenderControllerBlog.BLOG_POST_ID),
                                         eq(_blogPostId));
    }    
}
