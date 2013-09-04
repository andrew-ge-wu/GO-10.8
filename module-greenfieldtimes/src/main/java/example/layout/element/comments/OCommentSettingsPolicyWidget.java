package example.layout.element.comments;

import java.io.IOException;

import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.util.PolicyWidgetUtil;
import com.polopoly.cm.app.widget.OFieldPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.filter.state.ModerationState.State;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.ajax.AjaxEvent;
import com.polopoly.orchid.ajax.JSCallback;
import com.polopoly.orchid.ajax.OAjaxTrigger;
import com.polopoly.orchid.ajax.event.ClickEvent;
import com.polopoly.orchid.ajax.listener.StandardAjaxEventListener;
import com.polopoly.orchid.ajax.trigger.JsEventTriggerType;
import com.polopoly.orchid.ajax.trigger.OAjaxTriggerOnEvent;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OCheckbox;
import com.polopoly.orchid.widget.OLabel;
import com.polopoly.orchid.widget.OSelect;
import com.polopoly.util.LocaleUtil;

/**
 * Widget for the ModerationLevel field.
 */
@SuppressWarnings("serial")
public class OCommentSettingsPolicyWidget extends OFieldPolicyWidget
    implements Editor, Viewer
{
    private OLabel isOnlineLabel = null;
    private OCheckbox isOnlineCheckbox = null;

    private OLabel isOpenForCommentsLabel = null;
    private OCheckbox isOpenForCommentsCheckbox = null;

    private OLabel initialModerationStateLabel = null;
    private OSelect initialModerationStateSelect = null;

    private static final State DEFAULT_STATE = State.PUBLIC_PENDING;

    private OAjaxTrigger isOnlineAjaxTriggerOnClick;
    private OAjaxTrigger isOpenForCommentsAjaxTriggerOnClick;

    @Override
    public void initSelf(OrchidContext oc)
        throws OrchidException
    {
        super.initSelf(oc);

        isOnlineLabel = new OLabel();
        isOnlineLabel.setText(LocaleUtil.format("example.CommentSettings.isOnline", oc.getMessageBundle()));
        addAndInitChild(oc, isOnlineLabel);

        isOnlineCheckbox = new OCheckbox();
        isOnlineCheckbox.setEnabled(PolicyWidgetUtil.isEditMode(this));
        addAndInitChild(oc, isOnlineCheckbox);

        isOpenForCommentsLabel = new OLabel();
        isOpenForCommentsLabel.setText(LocaleUtil.format("example.CommentSettings.isOpenForComments", oc.getMessageBundle()));
        addAndInitChild(oc, isOpenForCommentsLabel);

        isOpenForCommentsCheckbox = new OCheckbox();
        isOpenForCommentsCheckbox.setEnabled(PolicyWidgetUtil.isEditMode(this));
        addAndInitChild(oc, isOpenForCommentsCheckbox);

        initialModerationStateLabel = new OLabel();
        initialModerationStateLabel.setText(LocaleUtil.format("example.CommentSettings.initialModerationState", oc.getMessageBundle()));
        addAndInitChild(oc, initialModerationStateLabel);

        initialModerationStateSelect = new OSelect();
        initialModerationStateSelect.setEnabled(PolicyWidgetUtil.isEditMode(this));

        for (State state : State.values()) {
            initialModerationStateSelect.addOption(initialModerationStateSelect.new Option(
                    LocaleUtil.format("cm.field.ModerationState." + state.toString(), oc.getMessageBundle()),
                    state.getName()));
        }

        initialModerationStateSelect.setSelectedValue(DEFAULT_STATE.getName());

        addAndInitChild(oc, initialModerationStateSelect);

        isOnlineLabel.setTargetId(isOnlineCheckbox.getCompoundId());
        isOpenForCommentsLabel.setTargetId(isOpenForCommentsCheckbox.getCompoundId());
        initialModerationStateLabel.setTargetId(initialModerationStateSelect.getCompoundId());

        createIsClosedComponents(oc);
        createIsClosedForCommentsComponents(oc);
    }

    private void createIsClosedComponents(final OrchidContext oc)
        throws OrchidException
    {
        StandardAjaxEventListener isOfflineOnClickListener = createAjaxOnClickListener();

        isOfflineOnClickListener.addDecodeWidget(isOnlineCheckbox);

        isOnlineAjaxTriggerOnClick = new OAjaxTriggerOnEvent(isOnlineCheckbox, JsEventTriggerType.CLICK);
        isOnlineAjaxTriggerOnClick.setFormPostSource(this);
        addAndInitChild(oc, isOnlineAjaxTriggerOnClick);

        isOfflineOnClickListener.addRenderWidget(this);

        getTree().registerAjaxEventListener(isOnlineCheckbox, isOfflineOnClickListener);
    }

    private void createIsClosedForCommentsComponents(final OrchidContext oc)
        throws OrchidException
    {
        StandardAjaxEventListener isClosedForCommentsOnClickListener = createAjaxOnClickListener();

        isClosedForCommentsOnClickListener.addDecodeWidget(isOpenForCommentsCheckbox);

        isOpenForCommentsAjaxTriggerOnClick = new OAjaxTriggerOnEvent(isOpenForCommentsCheckbox, JsEventTriggerType.CLICK);
        isOpenForCommentsAjaxTriggerOnClick.setFormPostSource(this);
        addAndInitChild(oc, isOpenForCommentsAjaxTriggerOnClick);

        isClosedForCommentsOnClickListener.addRenderWidget(this);

        getTree().registerAjaxEventListener(isOpenForCommentsCheckbox, isClosedForCommentsOnClickListener);
    }

    private StandardAjaxEventListener createAjaxOnClickListener()
    {
        return new StandardAjaxEventListener() {
            public boolean triggeredBy(OrchidContext oc,
                                       AjaxEvent e)
            {
                return e instanceof ClickEvent;
            }

            public JSCallback processEvent(OrchidContext oc,
                                           AjaxEvent e)
                throws OrchidException
            {
                return null;
            }
        };
    }

    @Override
    public void initValueFromPolicy()
        throws CMException
    {
        CommentsElementPolicy commentsElementPolicy = getCommentsElementPolicy();

        if (commentsElementPolicy != null) {
            isOnlineCheckbox.setChecked(commentsElementPolicy.isMarkedAsOnline());
            isOpenForCommentsCheckbox.setChecked(commentsElementPolicy.isMarkedAsOpenForComments());
            initialModerationStateSelect.setSelectedValue(commentsElementPolicy.getInitialModerationState().getName());
        }
    }

    @Override
    public void updateSelf(OrchidContext oc)
        throws OrchidException
    {
        super.updateSelf(oc);

        if (PolicyWidgetUtil.isEditMode(this)) {
            boolean isOpen = isOnlineCheckbox.isChecked() && isOpenForCommentsCheckbox.isChecked();

            initialModerationStateSelect.setEnabled(isOpen);
            isOpenForCommentsCheckbox.setEnabled(isOnlineCheckbox.isChecked());
        }
    }

    @Override
    public void storeSelf()
        throws CMException
    {
        CommentsElementPolicy commentsElementPolicy = getCommentsElementPolicy();

        if (commentsElementPolicy != null) {
            commentsElementPolicy.setMarkedAsOnline(isOnlineCheckbox.isChecked());
            commentsElementPolicy.setMarkedAsOpenForComments(isOpenForCommentsCheckbox.isChecked());
            commentsElementPolicy.setInitialModerationState(State.fromName(initialModerationStateSelect.getSelectedValue()));
        }
    }

    private CommentsElementPolicy getCommentsElementPolicy()
        throws CMException
    {
        if (getPolicy().getParentPolicy() instanceof CommentsElementPolicy) {
            return (CommentsElementPolicy) getPolicy().getParentPolicy();
        }

        return null;
    }

    @Override
    public boolean isAjaxTopWidget()
    {
        return true;
    }

    @Override
    public void localRender(OrchidContext oc)
        throws OrchidException, IOException
    {
        Device device = oc.getDevice();

        device.println("<br />");

        isOnlineCheckbox.render(oc);
        device.println("&nbsp;");
        isOnlineLabel.render(oc);

        isOnlineAjaxTriggerOnClick.render(oc);

        device.println("<br /><br />");

        isOpenForCommentsCheckbox.render(oc);
        device.println("&nbsp;");
        isOpenForCommentsLabel.render(oc);

        isOpenForCommentsAjaxTriggerOnClick.render(oc);

        device.println("<br /><br />");

        initialModerationStateLabel.render(oc);
        device.println("<br /><br />");
        initialModerationStateSelect.render(oc);
    }
}
