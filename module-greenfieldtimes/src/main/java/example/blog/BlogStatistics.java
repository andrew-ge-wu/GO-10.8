package example.blog;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.servlet.StatisticsContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.model.context.PageScope;

public class BlogStatistics
{
    private static final Logger LOG = Logger.getLogger(BlogStatistics.class.getName());
    private static final String BLOG_LOGGING_TYPE = "BLOG";

    public void populateStatisticsContext(TopModel m) {
        StatisticsContext statisticsContext = m.getRequest().getStatisticsContext();
        PageScope page = m.getContext().getPage();
        
        if (page == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE,
                        "BlogStatistics - Page is null in context: "
                        + m.getContext() + ", for request: " + m.getRequest()
                        + ".");
            }
            return;
        }
        
        ContentId[] pagePath = getContentPathToBlog(page);
        ContentId blogId = page.getPathAfterPage().get(0);
        
        statisticsContext.addHit(pagePath, blogId);
        statisticsContext.setType(BLOG_LOGGING_TYPE);
    }

    private ContentId[] getContentPathToBlog(PageScope page)
    {
        return page.getContentPath().getAsArray();
    }
}
