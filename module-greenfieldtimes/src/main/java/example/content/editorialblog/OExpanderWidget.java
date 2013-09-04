package example.content.editorialblog;

import java.io.IOException;

import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.ajax.AjaxEvent;
import com.polopoly.orchid.ajax.JSCallback;
import com.polopoly.orchid.ajax.OAjaxTrigger;
import com.polopoly.orchid.ajax.event.ClickEvent;
import com.polopoly.orchid.ajax.listener.StandardAjaxEventListener;
import com.polopoly.orchid.ajax.trigger.JsEventTriggerType;
import com.polopoly.orchid.ajax.trigger.OAjaxTriggerOnEvent;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OImage;
import com.polopoly.orchid.widget.OWidget;
import com.polopoly.orchid.widget.OWidgetBase;

@SuppressWarnings("serial")
public class OExpanderWidget extends OWidgetBase {
    public OImage expandImage;
    public OImage foldImage;
    public ExpandListener expandListener;
    public FoldListener foldListener;
    private boolean isExpanded;
    private final OWidget parent;
    private OAjaxTrigger expandTrigger;
    private OAjaxTrigger foldTrigger;

    public OExpanderWidget(OWidget parent) {
        this.parent = parent;
    }

    public void initSelf(OrchidContext oc) throws OrchidException {
        expandImage = new OImage();
        expandImage.setAltText("Expand month");
        expandImage.setSrc(oc.getPathHandler().getImageDirPath() +
                           "/icons/expand_arrow_right.png");
        expandImage.setStyle("position: relative; vertical-align: middle");
        addAndInitChild(oc, expandImage);

        foldImage = new OImage();
        addAndInitChild(oc, foldImage);
        foldImage.setAltText("V");
        foldImage.setSrc(oc.getPathHandler().getImageDirPath() +
                         "/icons/fold_arrow_down.png");
        foldImage.setStyle("position: relative; vertical-align: middle");

        expandListener = new ExpandListener(parent);

        foldListener = new FoldListener(parent);

        expandTrigger = initAjaxTriggerAndListener(oc, expandListener, expandImage);
        foldTrigger = initAjaxTriggerAndListener(oc, foldListener, foldImage);
    }


    OAjaxTrigger initAjaxTriggerAndListener(OrchidContext oc, StandardAjaxEventListener listener, OWidget target)
        throws OrchidException
    {
        OAjaxTrigger oAjaxTriggerOnClick = new OAjaxTriggerOnEvent(target, JsEventTriggerType.CLICK);
        oAjaxTriggerOnClick.setFormPostSource(this);
        addAndInitChild(oc, oAjaxTriggerOnClick);

        getTree().registerAjaxEventListener(target, listener);

        return oAjaxTriggerOnClick;
    }

    @Override
    public void localRender(OrchidContext oc) throws IOException, OrchidException
    {
        if (isExpanded()) {
            foldImage.render(oc);
            foldTrigger.render(oc);
        }
        else {
            expandImage.render(oc);
            expandTrigger.render(oc);
        }
    }

    public void setExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    private final class ExpandListener extends
    StandardAjaxEventListener {
        public ExpandListener(OWidget parent) {
            addDecodeWidget(parent);
            addRenderWidget(parent);
        }

        public boolean triggeredBy(OrchidContext oc, AjaxEvent e) {
            return e instanceof ClickEvent;
        }

        public JSCallback processEvent(OrchidContext oc, AjaxEvent e)
        throws OrchidException {
            setExpanded(true);
            return null;
        }
    }

    private final class FoldListener extends
    StandardAjaxEventListener {
        public FoldListener(OWidget parent) {
            addDecodeWidget(parent);
            addRenderWidget(parent);
        }

        public boolean triggeredBy(OrchidContext oc, AjaxEvent e) {
            return e instanceof ClickEvent;
        }

        public JSCallback processEvent(OrchidContext oc, AjaxEvent e)
        throws OrchidException {
            setExpanded(false);
            return null;
        }
    }}
