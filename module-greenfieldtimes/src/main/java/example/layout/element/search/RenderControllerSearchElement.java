package example.layout.element.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;

import com.polopoly.application.Application;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policymvc.PolicyModelDomain;
import com.polopoly.management.ServiceNotAvailableException;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.RenderRequest;
import com.polopoly.search.solr.SearchClient;
import com.polopoly.search.solr.SearchResult;
import com.polopoly.search.solr.SearchResultPage;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;
import com.polopoly.siteengine.mvc.Renderer;
import com.polopoly.siteengine.scope.system.ServiceStatus;

import example.content.BodyTranslator;

/**
 * Model builder for search element.
 *
 * <p>
 * Indexing and search work as follows:
 *
 * <p>
 * Articles index all text fields they feel necessary in the field "text".
 * Searching is done only in the field "text".
 *
 * <p>
 * The indexer automatically adds the field "name". This field is used for hit
 *
 */
public class RenderControllerSearchElement extends RenderControllerBase
{
    private final static Logger LOG = Logger.getLogger(RenderControllerSearchElement.class.getName());

    /**
     * Populates model of search element. 
     * Unless the query parameter is a part of the cache key, this logic needs to be in before cache key.
     */
    public void populateModelBeforeCacheKey(RenderRequest request,
                                            TopModel m,
                                            ControllerContext controllerContext)
    {
        Model model = m.getLocal();

        boolean shouldDisplaySearch = shouldDisplaySearch(controllerContext);

        // Get query string
        String q = request.getParameter("q");

        // Check if is search
        if (shouldDisplaySearch) {
            // Default hit-count:
            ModelPathUtil.set(model, "totalHitCount", new Integer(0));
            if (q != null && q.length() > 0) {
              performSearch(request, m, controllerContext, model, q);
            }
        }
    }

    private boolean shouldDisplaySearch(ControllerContext controllerContext)
    {
        return "article".equals(controllerContext.getView());
    }


    private void performSearch(RenderRequest request, TopModel m,
                               ControllerContext controllerContext,
                               Model model,
                               String q)
    {
        // Get model domain
        PolicyModelDomain domain = (PolicyModelDomain) controllerContext.getModelDomain();

        // Try to get search service from controller context (9.9 wiring).
        Application app = controllerContext.getApplication();
        SearchClient searchClient = null;

        if (null != app) {
            searchClient = (SearchClient) app.getApplicationComponent("search_solrClientPublic");
        } else {
            LOG.log(Level.INFO, "No application configured, search is not available.");
            return;
        }

        // Paging. Note that page and indexes are 0-indexed.
        int itemsPerPage = 10;
        int page = 0;
        int pagesAvailable = 0;
        int firstIndex = -1;
        int lastIndex = -1;
        int prevPageIndex = -1;
        int nextPageIndex = -1;

        SolrQuery query = new SolrQuery(q);

        query.setParam("qt", "freetextSearch");
        query.addFilterQuery("inputTemplate:example.StandardArticle");

        ContentId contentId = ((ContentId) m.getContext().getSite()
                .getContent().getAttribute("contentId")).getContentId();
        query.addFilterQuery("page:" + contentId.getContentIdString());


        try {
            // Perform search
            SearchResult result = searchClient.search(query, 100);
            SearchResultPage searchResultPage = null;
            searchResultPage = result.getPage(0);

            if (!searchResultPage.isEmpty()) {
                List<ContentId> contentIds = searchResultPage.getHits();
                List<Map<String, Object>> hitList = new ArrayList<Map<String, Object>>();

                // Check nof available pages
                int resultSize = contentIds.size();

                pagesAvailable = resultSize / itemsPerPage;
                if (resultSize % itemsPerPage > 0) {
                    pagesAvailable++;
                }

                // Get page no from request
                String pageNumber = request.getParameter("page");
                if (pageNumber != null) {
                    try {
                        page = Integer.parseInt(pageNumber);
                    } catch (NumberFormatException e) {
                        page = 0;
                    }
                    // Check if page no is within possible bounds
                    if (page >= pagesAvailable) {
                        page = 0;
                    }
                }

                // Calculate first and last item index for the current
                // page
                if (resultSize > 0) {
                    firstIndex = page * itemsPerPage;
                    if ((page + 1) * itemsPerPage > resultSize) {
                        lastIndex = resultSize;
                    } else {
                        lastIndex = (page + 1) * itemsPerPage;
                    }
                }

                BodyTranslator bodyTranslator = new BodyTranslator();

                for (int i = firstIndex; i < lastIndex; i++) {
                    // Get hit content id
                    ContentId id = contentIds.get(i);
                    // Get model for hit
                    Model hitModel;
                    try {
                        hitModel = domain.getModel(id);
                    } catch (CMException e) {
                        LOG.log(Level.WARNING, "Unable to get hit model for " + id, e);
                        continue;
                    }

                    // Create and populate hit map
                    Map<String, Object> hitMap = new HashMap<String, Object>();
                    hitMap.put("model", hitModel);

                    hitMap.put("name", ModelPathUtil.get(hitModel, "name"));
                    String body = (String) ModelPathUtil.get(hitModel, "body/value");
                    if (body == null) {
                        body = (String) ModelPathUtil.get(hitModel, "lead/value");
                    }
                    if (body == null) {
                        body = "";
                    }
                    body = truncate(body, 256);
                    body = bodyTranslator.translateBody(request, id, body, false, false);
                    hitMap.put("text", body);
                    hitList.add(hitMap);
                }

                // Create stupid list to enable iteration in velocity
                List<Integer> pages = new ArrayList<Integer>(pagesAvailable);
                for (int i = 0; i < pagesAvailable; i++) {
                    pages.add(new Integer(i));
                }

                // Check if prev and next are available
                if (page > 0) {
                    prevPageIndex = page - 1;
                }
                if (page < pagesAvailable - 1) {
                    nextPageIndex = page + 1;
                }

                // Paging vars
                ModelPathUtil.set(model, "firstIndex", new Integer(firstIndex));
                ModelPathUtil.set(model, "lastIndex", new Integer(lastIndex));
                ModelPathUtil.set(model, "prevPageIndex", new Integer(prevPageIndex));
                ModelPathUtil.set(model, "nextPageIndex", new Integer(nextPageIndex));
                ModelPathUtil.set(model, "page", new Integer(page));
                ModelPathUtil.set(model, "pages", pages);
                ModelPathUtil.set(model, "pagesAvailable", new Integer(pagesAvailable));
                ModelPathUtil.set(model, "hits", hitList);
                ModelPathUtil.set(model, "hitCount", new Integer(hitList.size()));
                ModelPathUtil.set(model, "totalHitCount", new Integer(resultSize));
            }
        }
        catch (SolrServerException e) {
            LOG.log(Level.WARNING, "Search failed", e);
        }
        catch (ServiceNotAvailableException e) {
            LOG.log(Level.INFO, "Search service is currently not available.");
            LOG.log(Level.FINE, "Search failed", e);
        }
    }

    /**
     * Truncates a string if it exceeds length. Appends ... if it does.
     *
     * @param s
     * @param length
     * @return a truncated version of s or null if s is null
     */
    private String truncate(String s, int length)
    {
        if (s == null) {
            return null;
        }
        if (s.length() > length) {
            StringBuilder buf = new StringBuilder(2 * length);
            boolean insideTag = false;
            int i = 0;
            for (int j = 0; i < s.length() && j < length; i++) {
                char c = s.charAt(i);
                buf.append(c);
                if (c == '<') {
                    insideTag = true;
                } else if (c == '>') {
                    insideTag = false;
                }
                if (!insideTag) {
                    j++;
                }
            }
            if (i < s.length()) {
                buf.append("...");
            }
            return buf.toString();
        }
        return s;
    }

    @Override
    public Renderer getRenderer(RenderRequest request,
                                TopModel m,
                                Renderer defaultRenderer,
                                ControllerContext context)
    {
        // If the index server isn't available, searching won't work, so let's
        // hide the search element (but not search results, where we can show an
        // error message instead). We could display an error message instead,
        // like the poll and comments elements do.

        boolean shouldDisplaySearch = shouldDisplaySearch(context);

        if (shouldDisplaySearch || isSearchServiceAvailable(m)) {
            return super.getRenderer(request, m, defaultRenderer, context);
        } else {
            return DO_NOTHING_RENDERER;
        }
    }

    private boolean isSearchServiceAvailable(TopModel m) {
        ServiceStatus searchStatus = m.getSystem().getServiceStatus("search_solrClientPublic");
        return searchStatus.isServing();
    }
}
