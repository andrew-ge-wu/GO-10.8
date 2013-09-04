package example.blog;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mock;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.servlet.StatisticsContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.model.context.ContextScope;
import com.polopoly.siteengine.model.context.PageScope;
import com.polopoly.siteengine.model.request.ContentPath;
import com.polopoly.siteengine.model.request.ContentPathFactory;
import com.polopoly.siteengine.model.request.RequestScope;

import example.MockitoBase;

public class BlogStatisticsTest extends MockitoBase {
    
    private BlogStatistics target;
    @Mock private TopModel topModel;
    @Mock private RequestScope requestScope;
    @Mock private StatisticsContext statisticsContext;
    @Mock private ContextScope contextScope;
    @Mock private PageScope pageScope;    

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        target = new BlogStatistics();
    }

    public void testShouldSetContextWithBlogAsPage() throws Exception {
        ContentId blogId = new ContentId(19, 123);
        ContentId[] expectedPath = new ContentId[] {new ContentId(1, 100), new ContentId(1, 101)};

        ContentPathFactory contentPathFactory = new ContentPathFactory();
        ContentPath blogPath = contentPathFactory.createPath(blogId);
        ContentPath pagePath = contentPathFactory.createPath(expectedPath);
        
        when(topModel.getRequest()).thenReturn(requestScope);
        when(requestScope.getStatisticsContext()).thenReturn(statisticsContext);
        when(topModel.getContext()).thenReturn(contextScope);
        when(contextScope.getPage()).thenReturn(pageScope);
        when(pageScope.getContentPath()).thenReturn(pagePath);
        when(pageScope.getPathAfterPage()).thenReturn(blogPath);
        
        target.populateStatisticsContext(topModel);
        
        verify(statisticsContext).setType("BLOG");
        verify(statisticsContext).addHit(expectedPath, blogId);
    }
}
