package example.layout.element;

import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.mockito.Mock;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.metadata.Dimension;
import com.polopoly.metadata.Metadata;
import com.polopoly.metadata.policy.MetadataAwarePolicy;
import com.polopoly.model.Model;
import com.polopoly.model.ModelBase;
import com.polopoly.model.ModelStoreInBean;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

import example.MockitoBase;

public class RenderControllerRelatedContentElementTest
    extends MockitoBase
{
    private RenderControllerRelatedContentElement target;

    @Mock private RenderRequest request;
    @Mock private TopModel m;
    @Mock private ControllerContext context;
    @Mock private CacheInfo cacheInfo;

    private ModelWrite local;

    @Mock private Model contentModel;

    @Mock Content content2;
    @Mock Content content1;

    @Mock private Policy contentPolicy1;
    @Mock private Policy contentPolicy2;
    @Mock private MetadataAwarePolicy metadataPolicy;

    @Mock private PolicyCMServer cmServer;

    private ContentId parentId = new ContentId(1,100);
    private ContentId parentId2 = new ContentId(1,100);

    private Metadata metadata;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        target = new RenderControllerRelatedContentElement();

        local = new ModelBase();
        when(m.getLocal()).thenReturn(local);

        metadata = new Metadata(Arrays.asList(new Dimension("hepp", "hepp", false)));

        when(contentPolicy1.getCMServer()).thenReturn(cmServer);
        when(contentPolicy1.getContent()).thenReturn(content1);
        when(contentPolicy2.getContent()).thenReturn(content2);
        when(metadataPolicy.getMetadata()).thenReturn(metadata);

        when(context.getContentModel()).thenReturn(contentModel);
        when(contentModel.getAttribute(ModelStoreInBean.BEAN_ATTRIBUTE_NAME)).thenReturn(contentPolicy1);

        when(content1.getSecurityParentId()).thenReturn(parentId);
        when(cmServer.getPolicy(parentId)).thenReturn(contentPolicy2);
    }

    public void testShouldGetCategorizationFromFirstCategorizationProviderInParentPath()
        throws Exception
    {
        when(content2.getSecurityParentId()).thenReturn(parentId2);
        when(cmServer.getPolicy(parentId2)).thenReturn(metadataPolicy);

        target.populateModelAfterCacheKey(request, m, cacheInfo, context);

        assertEquals(metadata, local.getAttribute("metadata"));
    }

    public void testShouldReturnEmptyCategorizationIfNoCategorizationProviderInPath()
        throws Exception
    {
        when(content2.getSecurityParentId()).thenReturn(null);

        target.populateModelAfterCacheKey(request, m, cacheInfo, context);

        assertEquals(new Metadata(), local.getAttribute("metadata"));
    }
}
