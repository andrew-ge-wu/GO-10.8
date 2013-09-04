package example.content.editorialblog;

import java.io.IOException;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.orchid.widget.OContentIdLink;
import com.polopoly.cm.app.widget.OFieldPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;

/**
 * Widget that shows a link to the blog from the blog posting.
 */
public class ParentBlogLinkPolicyWidget extends OFieldPolicyWidget
    implements Viewer, Editor
{

    private static final long serialVersionUID = 1L;

    private OContentIdLink parentBlogLink;

    /**
     * Standard Orchid method that initializes the link to the
     * parent Blog.
     * 
     * @param oc standard OrchidContext
     * @throws com.polopoly.orchid.OrchidException
     */
    @Override
    public void initSelf(OrchidContext oc) throws OrchidException
    {
        super.initSelf(oc);

        try {
            BlogPolicy blog = getParentBlog();
            if (blog != null) {
                ContentId blogId = blog.getContentId().getContentId();

                parentBlogLink = new OContentIdLink();
                parentBlogLink.setContentId(blogId);
                parentBlogLink.setLabel(blog.getName());
                parentBlogLink.setTitle(blogId.getContentIdString());
                parentBlogLink.init(oc);
            }
        } catch (CMException e) {
            e.printStackTrace();
        }
    }

    /**
     * Renders the parent Blog link.
     *
     * @param oc OrchidContext
     * @throws com.polopoly.orchid.OrchidException
     * @throws java.io.IOException
     */
    @Override
    public void localRender(OrchidContext oc) throws OrchidException, IOException
    {
        super.localRender(oc);

        if (parentBlogLink == null) {
            Device device = oc.getDevice();
            device.println("Warning: This blog posting is not associated with a blog.");
        } else {
            parentBlogLink.render(oc);
        }
    }

    private BlogPolicy getParentBlog()
    {
        ContentId securityParentId = getPolicy().getContent().getSecurityParentId();
        try {
            Policy securityParent =
                    getContentSession().getPolicyCMServer().getPolicy(securityParentId);
            if (securityParent instanceof Blog) {
                return (BlogPolicy) securityParent;
            }
        } catch (CMException e1) {
            e1.printStackTrace();
        }

        return null;
    }
}
