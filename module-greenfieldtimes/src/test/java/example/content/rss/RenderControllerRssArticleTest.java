package example.content.rss;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.mockito.Mock;

import com.polopoly.cm.app.search.categorization.CategorizationProvider;
import com.polopoly.cm.app.search.categorization.CategorizationTransformer;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.metadata.Dimension;
import com.polopoly.metadata.Entity;
import com.polopoly.metadata.Metadata;

import example.MockitoBase;

public class RenderControllerRssArticleTest extends MockitoBase {

    private RenderControllerRssArticle target;
    @Mock CategorizationProvider categorizationProvider;
    @Mock PolicyCMServer cmServer;
    @Mock CategorizationTransformer categorizationTransformer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        target = new RenderControllerRssArticle();
    }

    public void testShouldReturnTreeCategoriesAsCategoriesAndSkipEverythingElse() throws Exception {
        Metadata metadata = new Metadata(Arrays.asList(new Dimension("itpc", "IPTC", true, new Entity("cat1.id", "cat1", new Entity("cat12.id", "cat12")),
                                                                                           new Entity("cat2.id", "cat2")),
                                                       new Dimension("tags", "Tags", false, new Entity("tag1"))));

        Collection<String> feedCategories = target.getFeedEntities(metadata);
        assertEquals(2, feedCategories.size());
        Iterator<String> iter = feedCategories.iterator();
        assertEquals("<![CDATA[cat1/cat12]]>", iter.next());
        assertEquals("<![CDATA[cat2]]>", iter.next());

    }

}
