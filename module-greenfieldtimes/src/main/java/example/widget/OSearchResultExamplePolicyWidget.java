package example.widget;

import org.apache.solr.client.solrj.SolrQuery;

import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.search.widget.OSearchResult;
import com.polopoly.cm.app.widget.OComplexPolicyWidget;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.ajax.AjaxEvent;
import com.polopoly.orchid.ajax.JSCallback;
import com.polopoly.orchid.ajax.OAjaxTrigger;
import com.polopoly.orchid.ajax.event.ClickEvent;
import com.polopoly.orchid.ajax.listener.StandardAjaxEventListener;
import com.polopoly.orchid.ajax.trigger.JsEventTriggerType;
import com.polopoly.orchid.ajax.trigger.OAjaxTriggerOnEvent;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OButton;

/**
 * Example usage of {@link OSearchResult}
 *
 * PolicyWidget displays a button that performs a dummy solr search when
 * clicked.
 *
 */
@SuppressWarnings("serial")
public class OSearchResultExamplePolicyWidget extends OComplexPolicyWidget implements Viewer, Editor {

    private OSearchResult searchResultContainer;
    private OButton searchButton;
    private OAjaxTrigger onClickTrigger;

    public void initSelf(OrchidContext oc) throws OrchidException {

        searchResultContainer = new OSearchResult(getContentSession(), oc, "search_solrClientInternal");
        searchResultContainer.setListentryContext("myCustomContext");
        searchResultContainer.setCssClass("customSearchResultDiv");
        addAndInitChild(oc, searchResultContainer);

        searchButton = new OButton();
        searchButton.setLabel("Search!");
        addAndInitChild(oc, searchButton);

        StandardAjaxEventListener onClickListener = new StandardAjaxEventListener() {
            public boolean triggeredBy(OrchidContext orchidContext, AjaxEvent e) {
                return e instanceof ClickEvent;
            }

            public JSCallback processEvent(OrchidContext orchidContext, AjaxEvent event) throws OrchidException {
                searchResultContainer.doSearch(orchidContext, getQuery());
                return null;
            }
        };
        onClickListener.addDecodeWidget(this);
        onClickListener.addRenderWidget(searchResultContainer.getAjaxRenderWidget());

        onClickTrigger = new OAjaxTriggerOnEvent(searchButton, JsEventTriggerType.CLICK);
        onClickTrigger.setFormPostSource(this);
        addAndInitChild(oc, onClickTrigger);

        getTree().registerAjaxEventListener(searchButton, onClickListener);
    }

    private SolrQuery getQuery() {
        return new SolrQuery("text:a*");
    }
}
