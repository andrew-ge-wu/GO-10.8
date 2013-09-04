package example.collection.searchbased;

import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;

import com.atex.plugins.baseline.collection.searchbased.AbstractPublishingQueuePolicyMetaDataBased;
import com.polopoly.application.Application;
import com.polopoly.cache.SynchronizedUpdateCache;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.Policy;
import com.polopoly.search.solr.querydecorators.UnderPage;
import com.polopoly.search.solr.querydecorators.WithInputTemplate;
import com.polopoly.search.solr.schema.IndexFields;

/**
 * Publishing queue for latest blog post, based on a solr search.
 *
 */
public class LatestBlogPostingsPublishingQueue
    extends AbstractPublishingQueuePolicyMetaDataBased
{

    public LatestBlogPostingsPublishingQueue(Application application,
                                             CmClient cmClient,
                                             SynchronizedUpdateCache searchCache)
    {
        super(application, cmClient, searchCache);
    }

    @Override
    protected SolrQuery getSolrQuery()
        throws CMException
    {
        Set<Policy> associatedSites = associatedSitesFetcher.fetch(this);

        SolrQuery query = new SolrQuery("*:*");
        query.addSortField(IndexFields.MODIFIED_DATE.fieldName(), ORDER.desc);
        new UnderPage(getContentIds(associatedSites)).decorate(query);
        new WithInputTemplate(new String[]{"example.BlogPost"}).decorate(query);

        return query;
    }
}
