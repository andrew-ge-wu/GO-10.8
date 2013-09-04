package example.content.editorialblog;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.orchid.widget.OContentIdLink;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OWidgetBase;

import example.util.ThreadLocalDateFormat;

@SuppressWarnings("serial")
public class OBlogMonthWidget
    extends OWidgetBase
{
    private final Map<ContentId, OContentIdLinkHolder> entryLinks =
        Collections.synchronizedMap(new HashMap<ContentId, OContentIdLinkHolder>());

    private final BlogMonth blogMonth;
    private final PolicyCMServer cmServer;

    public OExpanderWidget expanderWidget;

    private ThreadLocalDateFormat monthSdf;
    private ThreadLocalDateFormat dateSdf;

    private TimeZone blogTimeZone;

    public OBlogMonthWidget(final BlogMonth blogMonth,
                            final PolicyCMServer policyCMServer)
    {
        this.blogMonth = blogMonth;
        this.cmServer = policyCMServer;
    }

    @Override
    public void initSelf(OrchidContext oc)
        throws OrchidException
    {
        super.initSelf(oc);

        monthSdf = new ThreadLocalDateFormat("MMMM", getTimeZoneToUse(oc), oc.getLocale());
        dateSdf = new ThreadLocalDateFormat("EEE dd, HH:mm", getTimeZoneToUse(oc), oc.getLocale());

        expanderWidget = new OExpanderWidget(this);
        addAndInitChild(oc, expanderWidget);
    }

    public void setExpanded(boolean expanded)
    {
        expanderWidget.setExpanded(expanded);
    }

    private TimeZone getTimeZoneToUse(final OrchidContext oc)
    {
        return (blogTimeZone != null) ? blogTimeZone : oc.getTimeZone();
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

    @Override
    public void localRender(OrchidContext oc)
        throws IOException,
               OrchidException
    {
        try {
            Device device = oc.getDevice();
            device.print("<div>");

            List<ContentId> postings = blogMonth.getPostingIds();
            renderMonthLabel(oc, device, postings);

            if (expanderWidget.isExpanded()) {
                renderBlogMonth(oc, device, postings);
            }

            device.print("</div>");
        } catch (CMException e) {
            throw new OrchidException(e);
        }
    }

    private void renderMonthLabel(final OrchidContext oc,
                                  final Device device,
                                  final List<ContentId> postings)
        throws IOException,
               OrchidException
    {
        int monthNofPostings = postings.size();

        Calendar calendar = oc.getCalendarInstance();
        calendar.set(Calendar.YEAR, blogMonth.getYear());
        calendar.set(Calendar.MONTH, blogMonth.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        device.println("<span class='month timeLabel'>");
        expanderWidget.render(oc);

        device.println(
            monthSdf.format(calendar.getTime()) + " <span class='count'>("
            + monthNofPostings + ")</span></span>");
    }

    private void renderBlogMonth(final OrchidContext oc,
                                 final Device device,
                                 final List<ContentId> postings)
        throws IOException,
               OrchidException
    {
        Iterator<ContentId> postingsIter = postings.iterator();

        while (postingsIter.hasNext()) {
            ContentId postingId = postingsIter.next();

            device.println("<div class='postingLink'>");

            try {
                OContentIdLinkHolder contentIdLinkHolder = entryLinks.get(postingId);

                if (contentIdLinkHolder == null) {
                    BlogPostingPolicy posting = (BlogPostingPolicy) cmServer.getPolicy(postingId);

                    OContentIdLink contentIdLink = new OContentIdLink();
                    contentIdLink.setContentId(postingId);

                    String prefix = dateSdf.format(posting.getPublishDate());
                    contentIdLink.setLabel(posting.getName());
                    contentIdLink.setTitle(postingId.getContentIdString());
                    contentIdLink.init(oc);

                    contentIdLinkHolder =
                            new OContentIdLinkHolder(contentIdLink, prefix);

                    entryLinks.put(postingId, contentIdLinkHolder);
                }

                device.print(contentIdLinkHolder.getPrefix() + " ");
                contentIdLinkHolder.getContentIdLink().render(oc);

            } catch (CMException e) {
                e.printStackTrace();
            }

            device.println("</div>");
        }
    }

    private class OContentIdLinkHolder
    {
        private String prefix;
        private OContentIdLink contentIdLink;

        public OContentIdLinkHolder(OContentIdLink contentIdLink, String prefix)
        {
            this.contentIdLink = contentIdLink;
            this.prefix = prefix;
        }

        public String getPrefix()
        {
            return prefix;
        }

        public OContentIdLink getContentIdLink()
        {
            return contentIdLink;
        }
    }

    @Override
    public boolean isAjaxTopWidget()
    {
        return true;
    }
}
