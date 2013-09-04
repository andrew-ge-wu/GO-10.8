package example.widget;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.widget.OModerationStatePolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentOperationFailedException;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.util.LocaleUtil;

import example.comment.CommentPolicy;

@SuppressWarnings("serial")
public class OCommentListItemWidget extends OSearchItemBaseWidget
{
    private OModerationStatePolicyWidget moderationState;

    private static final Logger logger = Logger.getLogger(OCommentListItemWidget.class.getName());

    public void initSelf(OrchidContext oc)
    {
        super.initSelf(oc);

        moderationState = new OModerationStatePolicyWidget();
        moderationState.setEnableEditInViewMode(true);

        try {
            moderationState.setParentPolicyWidget(this);
            addAndInitChild(oc, moderationState);
        } catch (OrchidException e) {
            logger.logp(Level.WARNING, CLASS, "initSelf",
                    "Could  not initialize", e);
        }
    }

    @Override
    protected void renderToolbox(Device device, OrchidContext oc)
        throws OrchidException,
               IOException
    {
        // Do not want either check box or copy button rendered.
    }

    @Override
    public void updateSelf(OrchidContext oc)
        throws OrchidException
    {
        if (getPolicy() == null) {
            return;
        }
        
        ContentId commentContentId = getPolicy().getContentId().getContentId();
        
        try {
            setPolicy(getPolicy().getCMServer().getPolicy(commentContentId));
        } catch (ContentOperationFailedException e) {
            setPolicy(null);
        } catch (CMException e) {
            throw new OrchidException(e);
        }
    }

    @Override
    protected void renderEntryBody(Device device, OrchidContext oc)
        throws OrchidException, IOException
    {
        try {
            CommentPolicy contentPolicy = (CommentPolicy) getPolicy();
            
            if (contentPolicy == null) {
                device.println("<span style='font-weight: bold;'>"
                               + LocaleUtil.format("cm.example.comment.NotFound", oc.getMessageBundle())
                               + " </span>");

            } else {
                if (contentPolicy.isDeleted()) {
                    device.println("<span style='font-weight: bold;'>"
                                   + LocaleUtil.format("cm.example.comment.DeletedDescription", oc.getMessageBundle())
                                   + " </span>");
                }

                device.println(contentPolicy.getText());
                moderationState.render(oc);
            }
        } catch (CMException e) {
            throw new OrchidException(e);
        }
    }
}
