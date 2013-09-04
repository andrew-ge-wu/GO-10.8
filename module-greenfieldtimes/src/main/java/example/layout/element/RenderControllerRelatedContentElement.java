package example.layout.element;

import com.google.common.collect.Lists;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.metadata.Dimension;
import com.polopoly.metadata.Metadata;
import com.polopoly.metadata.MetadataAware;
import com.polopoly.metadata.util.MetadataUtil;
import com.polopoly.metadata.util.MetadataUtil.Filtering;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

import example.content.RenderControllerExtended;

public class RenderControllerRelatedContentElement extends RenderControllerExtended {

    @Override
    public void populateModelAfterCacheKey(RenderRequest request, TopModel m,
            CacheInfo cacheInfo, ControllerContext context) {
        super.populateModelAfterCacheKey(request, m, cacheInfo, context);

        Metadata metadata = new Metadata();
        Model contentModel = context.getContentModel();
        try {
            Policy policy = (Policy) ModelPathUtil.getBean(contentModel);
            PolicyCMServer cmServer = policy.getCMServer();

            while (policy != null && !(policy instanceof MetadataAware)) {
                ContentId securityParentId = policy.getContent().getSecurityParentId();
                if (securityParentId != null) {
                    policy = cmServer.getPolicy(securityParentId);
                }
                else {
                    policy = null;
                    break;
                }
            }

            if (policy instanceof MetadataAware) {
                Metadata originalMetadata = ((MetadataAware) policy).getMetadata();
                // Landing pages don't support entities with paths, so we replace the entity trees with the leaf entities
                for (Dimension originalDimension : originalMetadata.getDimensions()) {
                    Dimension dimension = originalDimension.copyWithEntities(Lists.newArrayList(MetadataUtil.traverseEntities(originalDimension, Filtering.ONLY_LEAVES)));
                    metadata.addDimension(dimension);
                }
            }
        }
        catch (CMException e) {
            // We can't get categorization, so we just set an empty categorization
        }
        m.getLocal().setAttribute("metadata", metadata);
    }

}
