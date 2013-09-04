package example.util;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.mockito.Mock;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.servlet.RequestPreparator;
import com.polopoly.cm.servlet.URLBuilder;
import com.polopoly.render.RenderRequest;

import example.MockitoBase;

public class UrlBuilderTest extends MockitoBase {

    private UrlBuilder target;
    @Mock private URLBuilder urlBuilder;
    @Mock private RenderHttpRequest request;
    private ContentId stopId = new ContentId(19,100);    

    private ContentId[] path = new ContentId[]{
        new ContentId(2,100), stopId};
   
    
    private ContentId[] pathWithContentIdAfterStopId = new ContentId[]{
        new ContentId(2,100), stopId, new ContentId(1,100)};
    
    private String blogUrl;
    private List<ContentId> pathList;
    private List<ContentId> pathWithContentIdAfterStopIdList;
    private ContentId nonExistingStopId = new ContentId(1,300);
    
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        target = new UrlBuilder();
        blogUrl = "/site/blog";
    
        pathList = Arrays.asList(path);
        pathWithContentIdAfterStopIdList = 
            Arrays.asList(pathWithContentIdAfterStopId);
        
        when(request.getAttribute(RequestPreparator.class.getName()+".ub")).thenReturn(urlBuilder);
    }
    
    
    public void testShouldReturnCompleteUrlIfStopIdLast() throws Exception
    {
                
        when(urlBuilder.createUrl(path, request)).thenReturn(blogUrl);
        String url = target.buildUrl(pathList, stopId, request);
        assertEquals(blogUrl, url);
        
    }
    
    
    public void testShouldReturnUrlToUpToStopId() throws Exception
    {
       
        when(urlBuilder.createUrl(path, request)).thenReturn(blogUrl);
        String url = target.buildUrl(pathWithContentIdAfterStopIdList, stopId, request);
        assertEquals(blogUrl, url);
        
    }
    
    public void testShouldThrowIllegalArgumentExceptionIfPathDoesNotContainStopId() throws Exception
    {
        when(urlBuilder.createUrl(path, request)).thenReturn(blogUrl);
        try {
            target.buildUrl(pathList, nonExistingStopId, request);
            fail();
        } catch (IllegalArgumentException e) {

        }
       
    }
    
    private interface RenderHttpRequest extends RenderRequest, HttpServletRequest {
    
            
        
    }
}
