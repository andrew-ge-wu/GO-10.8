package example.layout;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.servlet.dispatcher.NotFoundException;
import com.polopoly.metadata.Dimension;
import com.polopoly.metadata.Entity;
import com.polopoly.metadata.Metadata;
import com.polopoly.metadata.MetadataAware;
import com.polopoly.metadata.util.MetadataUtil;
import com.polopoly.metadata.util.MetadataUtil.Filtering;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

import example.content.RenderControllerExtended;
import example.content.article.StandardArticlePolicy;
import example.util.EntityName;

public class RenderControllerDefaultPageLayout
    extends RenderControllerExtended
{
    private static final Logger LOG = Logger.getLogger(RenderControllerDefaultPageLayout.class.getName());

    public void populateModelAfterCacheKey(RenderRequest request,
                                           TopModel m,
                                           CacheInfo cacheInfo,
                                           ControllerContext context) {
        super.populateModelAfterCacheKey(request, m, cacheInfo, context);

        ContentId lastContentId = getLastContentId(m, context);
        Policy lastContent = getLastContentInPath(context, lastContentId);
        if (lastContent != null) {
            verifyLastContentHasOutputTemplate(context, lastContent);
            addLastContentCategoriesToLocalModel(m, lastContent);
            addLastContentAuthorToLocalModel(m, lastContent);
            addLastContentNameToLocalModel(m, lastContent);
        }
    }

    private void verifyLastContentHasOutputTemplate(ControllerContext context,
                                                    Policy lastContent) {
        try {
            if (lastContent.getOutputTemplate(context.getMode()) == null) {
                throw new NotFoundException(
                    "No output template found for " + lastContent.getContentId().getContentId());
            }
        } catch (CMException e) {
            throw new CMRuntimeException("Couldn't get output template for "
                                         + lastContent.getContentId().getContentId());
        }
    }

    void addLastContentCategoriesToLocalModel(TopModel m,
                                              Policy lastContent) {
        StringBuffer keywordsBuffer = new StringBuffer();
        StringBuffer locationBuffer = new StringBuffer();
        try {
            Metadata categorization = null;
            if (lastContent instanceof MetadataAware) {
                categorization = ((MetadataAware) lastContent).getMetadata();
            }
            else {
                MetadataAware categorizationPolicy = (MetadataAware)
                lastContent.getChildPolicy("categorization");
                if (categorizationPolicy != null) {
                    categorization = categorizationPolicy.getMetadata();
                }
            }
             
            if (categorization != null) {
                addCategoryNamesForDimensionToBuffer(keywordsBuffer, categorization,
                        "department.categorydimension.subject",
                        "department.categorydimension.tag.Person",
                        "department.categorydimension.tag.Company",
                        "department.categorydimension.tag.Organisation",
                        "department.categorydimension.tag.Tag",
                        "department.categorydimension.tag.Location");
                
                addCategoryNamesForDimensionToBuffer(locationBuffer, categorization,
                        "department.categorydimension.tag.Location");

            }
        } catch (Exception e) {
            // Ok, so we don't have a last content, that should have been handled by the dispatcher already
            LOG.log(Level.FINE, "Can't figure out categorization for " + lastContent, e);
        }

        if (keywordsBuffer.length() > 0) {
            m.getLocal().setAttribute("keywords", keywordsBuffer.toString());
        }

        if (locationBuffer.length() > 0) {
            m.getLocal().setAttribute("locations", locationBuffer.toString());
        }
    }

    private void addLastContentAuthorToLocalModel(TopModel m,
                                                  Policy lastContent) {
        try {
            if (lastContent instanceof StandardArticlePolicy) {
                String author =
                        ((StandardArticlePolicy) lastContent).getAuthor();
                if (author != null) {
                    m.getLocal().setAttribute("author", author);
                }
            }
        } catch (CMException e) {
            LOG.log(Level.FINE,
                    "Can't figure out author for " + lastContent, e);
        }
    }

    private void addLastContentNameToLocalModel(TopModel m,
                                                Policy lastContent) {
        try {
            String title = lastContent.getContent().getName();
            if (title != null) {
                m.getLocal().setAttribute("title", title);
            }
        } catch (CMException e) {
            LOG.log(Level.FINE,
                    "Can't figure out title (content name) for "
                    + lastContent, e);
        }
    }

    private ContentId getLastContentId(TopModel m, ControllerContext context) {
        ContentId lastContentId = m.getRequest().getContentPath().getLast();
        if (lastContentId == null) {
            lastContentId = context.getContentId();
        }

        return lastContentId;
    }

    private Policy getLastContentInPath(ControllerContext context,
                                        ContentId contentId)
    {
        PolicyCMServer cmServer =
                _policyCMServerProvider.getPolicyCMServer(context);

        Policy policy = null;
        try {
            policy = cmServer.getPolicy(contentId);
        } catch (CMException cme) {
            LOG.log(Level.FINE,
                    "Can't find policy for " + contentId, cme);
        }
        return policy;
    }

    private void addCategoryNamesForDimensionToBuffer(StringBuffer buffer, Metadata metadata,
                                                      String... categoryNamesForDimension)
    {
        Set<String> categories = new LinkedHashSet<String>();
        for(String categoryName : categoryNamesForDimension) {
            Dimension dim = metadata.getDimensionById(categoryName);
            if (dim != null) {
                for (List<Entity> path : MetadataUtil.traverseEntityPaths(dim, Filtering.ONLY_LEAVES)) {
                    categories.add(EntityName.getEntityName(path));
                }
            }
        }
        int i = 0;
        for (String category : categories) {
            buffer.append(category);
            if (i < (categories.size() - 1 )) {
                buffer.append(", ");
            }
            i++;
        }
    }

}
