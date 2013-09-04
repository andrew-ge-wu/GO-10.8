package example.content.editorialblog;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OWidgetBase;

@SuppressWarnings("serial")
public class OBlogYearWidget
    extends OWidgetBase
{
    private final BlogYear blogYear;
    private final PolicyCMServer policyCMServer;
    
    private OBlogMonthWidget latestBlogMonth;
    
    private TimeZone blogTimeZone;

    public OBlogYearWidget(final BlogYear blogYear,
                           final PolicyCMServer policyCMServer)
    {
        this.blogYear = blogYear;
        this.policyCMServer = policyCMServer;
    }

    @Override
    public void initSelf(OrchidContext oc)
        throws OrchidException
    {
        super.initSelf(oc);
        
        List<BlogMonth> months = blogYear.getMonths();
        Iterator<BlogMonth> monthsIter = months.iterator();

        while (monthsIter.hasNext()) {
            BlogMonth blogMonth = monthsIter.next();
            
            OBlogMonthWidget blogMonthWidget = new OBlogMonthWidget(blogMonth, policyCMServer);
            blogMonthWidget.setBlogTimeZone(blogTimeZone);
            
            if (latestBlogMonth == null) {
                latestBlogMonth = blogMonthWidget;
            }
            
            addAndInitChild(oc, blogMonthWidget);
        }
    }
    
    /**
     * Sets the timezone this blog is using.
     * 
     * @param timeZone the blog timezone
     */
    public void setBlogTimeZone(final TimeZone timeZone)
    {
        this.blogTimeZone = timeZone;
    }

    /**
     * Expands the latest month so the user can see new posts.
     */
    public void expandLatestMonth()
    {
        if (latestBlogMonth != null) {
            latestBlogMonth.setExpanded(true);
        }
    }

    @Override
    public void localRender(OrchidContext oc)
        throws IOException,
               OrchidException
    {
        int yearNofPostings = blogYear.getPostingCount();
        
        oc.getDevice().println("<div class='year timeLabel'>" + blogYear.getYear()
                + " <span class='count'>(" + yearNofPostings + ")</span></div>");
        
        super.localRender(oc);
    }

    @Override
    public boolean isAjaxTopWidget()
    {
        return true;
    }
}
