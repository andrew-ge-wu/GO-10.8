package example.content.editorialblog;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.widget.OFieldPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;

/**
 * Widget for browsing blog postings by date in the gui.
 */
public class OBlogPostingBrowserPolicyWidget extends OFieldPolicyWidget
    implements Viewer, Editor
{
    private static final long serialVersionUID = 1L;
    private OBlogYearWidget latestBlogYear;

    private TimeZone blogTimeZone = null;

    /**
     * Standard Orchid method that initializes caching of the posting links.
     *
     * @param oc
     * @throws com.polopoly.orchid.OrchidException
     */
    @Override
    public void initSelf(OrchidContext oc)
        throws OrchidException
    {
        super.initSelf(oc);

        try {
            BlogPolicy blogPolicy = getBlogPolicy();

            if (blogPolicy != null) {
                if (blogPolicy.getBlogTimeZoneComponent() == null) {
                    blogTimeZone = oc.getTimeZone();
                } else {
                    blogTimeZone = blogPolicy.getBlogTimeZone();
                }
            }

            Blog blog = getBlog();

            List<BlogYear> years = blog.getBlogYears();
            Iterator<BlogYear> yearsIter = years.iterator();

            while (yearsIter.hasNext()) {
                BlogYear blogYear = yearsIter.next();

                OBlogYearWidget blogYearWidget = new OBlogYearWidget(blogYear, getContentSession().getPolicyCMServer());
                blogYearWidget.setBlogTimeZone(blogTimeZone);

                addAndInitChild(oc, blogYearWidget);

                if (latestBlogYear == null) {
                    latestBlogYear = blogYearWidget;
                }
            }

            if (latestBlogYear != null) {
                latestBlogYear.expandLatestMonth();
            }
        } catch (CMException e) {
            throw new OrchidException(e);
        }
    }

    @Override
    public void storeSelf()
        throws CMException
    {
        super.storeSelf();

        BlogPolicy blogPolicy = getBlogPolicy();

        if (blogPolicy != null && blogPolicy.getBlogTimeZoneComponent() == null) {
            blogPolicy.setBlogTimeZoneComponent(blogTimeZone.getID());
        }
    }

    /**
     * Renders the postings of a Blog.
     * All the postings for the Blog will be rendered as a list of titles.
     *
     * @param oc standard OrchidContext
     * @throws com.polopoly.orchid.OrchidException
     * @throws java.io.IOException
     */
    @Override
    public void localRender(OrchidContext oc)
        throws OrchidException, IOException
    {
        Device device = oc.getDevice();

        Blog blog = getBlog();

        if (blog == null) {
            device.println("Your policy is not a blog.");
            return;
        }

        device.println("<div class='blogPostingBrowser'>");

        super.localRender(oc);

        device.println("</div>");
    }


    /**
     * Locates the Blog in the Policy tree.
     *
     * @return the Blog or null of no Blog was found.
     */
    private Blog getBlog()
    {
        Policy p = getPolicy();

        while (p != null) {
            if (p instanceof Blog) {
                return (Blog) p;
            }

            try {
                p = p.getParentPolicy();
            } catch (CMException e) {
                e.printStackTrace();
                return null;
            }
        }

        return null;
    }

    private BlogPolicy getBlogPolicy()
    {
        Policy policy = getPolicy();

        while (policy != null) {
            if (policy instanceof BlogPolicy) {
                return (BlogPolicy) policy;
            }

            try {
                policy = policy.getParentPolicy();
            } catch (CMException cme) {
                logger.log(Level.WARNING, "Error while accessing blog policy!", cme);

                return null;
            }
        }

        return null;
    }
}
