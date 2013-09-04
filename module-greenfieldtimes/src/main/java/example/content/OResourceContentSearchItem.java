package example.content;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.plugins.baseline.url.OrchidUrlResolver;
import com.atex.plugins.baseline.url.UrlResolver;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ReferenceMetaDataPolicy;
import com.polopoly.metadata.Dimension;
import com.polopoly.metadata.Entity;
import com.polopoly.metadata.Metadata;
import com.polopoly.metadata.util.MetadataUtil;
import com.polopoly.metadata.util.MetadataUtil.Filtering;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OImage;
import com.polopoly.util.LocaleUtil;

import example.categorization.CategoryDimensions;
import example.util.EntityName;
import example.widget.OSearchItemBaseWidget;

@SuppressWarnings("serial")
public class OResourceContentSearchItem extends OSearchItemBaseWidget
{
    private static final Logger LOG =
        Logger.getLogger(OResourceContentSearchItem.class.getName());

    protected OImage _thumbnail;
    protected String _previewPath;

    protected String _byline;
    protected String _bylineLabel;

    private String _tags;
    private String _tagsLabel;

    public void initSelf(OrchidContext oc) {
        super.initSelf(oc);

        UrlResolver urlResolver = new OrchidUrlResolver(oc);

        try {
            String thumbnailPath = getResourcePolicy().getThumbnailPath(urlResolver);

            if (thumbnailPath != null) {
                _thumbnail = new OImage();
                _thumbnail.setStylesheetClass("iconMedium");
                _thumbnail.setSrc(thumbnailPath);

                addAndInitChild(oc, _thumbnail);
            }

            _bylineLabel = LocaleUtil.format("cm.general.Byline",
                                             oc.getMessageBundle());

            _tagsLabel = LocaleUtil.format("cm.label.Tags",
                                           oc.getMessageBundle());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Unable to initialize widget.", e);
        }
    }

    /**
     * Returns the resourceContent. Either directly or through refmetadata.
     */
    private ResourceContent getResourcePolicy() throws CMException {
        // Check if we have a referencemetadatapolicy in the entry

        if (getPolicy() instanceof ReferenceMetaDataPolicy) {
            ContentId referredId = ((ReferenceMetaDataPolicy) getPolicy()).getReferredContentId();
            return (ResourceContent) getContentSession().getPolicyCMServer().getPolicy(referredId);
        }

        // Otherwise, assume we're pointing directly to resource policy
        return (ResourceContent) getPolicy();
    }

    public void preRender(OrchidContext oc) throws OrchidException {
        super.preRender(oc);

        try {
            UrlResolver urlResolver = new OrchidUrlResolver(oc);
            ResourceContent resourceContent = getResourcePolicy();

            Metadata categorization = resourceContent.getMetadata();
            Dimension tags = categorization.getDimensionById(CategoryDimensions.TAG.externalId());
            if (tags != null) {
                Iterable<List<Entity>> categories = MetadataUtil.traverseEntityPaths(tags, Filtering.ONLY_LEAVES);
                if (categories.iterator().hasNext()) {
                    StringBuilder sb = new StringBuilder();
                    String delimiter = "";
                    for (List<Entity> category : categories) {
                        sb.append(delimiter);
                        sb.append(EntityName.getEntityName(category));
                        delimiter = ", ";
                    }
                    _tags = sb.toString();
                }
            }

            // Get src for original image
            _previewPath = resourceContent.getPreviewPath(urlResolver);

            _byline = resourceContent.getByline();
        } catch (CMException cme) {
            LOG.log(Level.WARNING, "Unable to pre-render widget.", cme);
        }
    }

    @Override
    protected void renderEntryBody(Device device, OrchidContext oc)
        throws OrchidException, IOException
    {
        device.println("<div class=\"listEntryBody clearfix\">");

        if (_previewPath != null) {
            device.println("<a href='" + _previewPath + "' target='_preview'>");
            renderPreviewLinkContent(oc, device);
            device.println("</a>");
        } else {
            renderPreviewLinkContent(oc, device);
        }

        if (null != _byline) {
            device.println("<div class=\"byline\">");
            device.println("<span class=\"bylineLabel\">");
            device.println(_bylineLabel);
            device.println("</span>");
            device.println("<span class=\"bylineText\">");
            device.println(_byline);
            device.println("</span>");
            device.println("</div>");
        }

        if (_tags != null) {
            device.println("<div class=\"tags\">");
            device.println("<span class=\"tagsLabel\">");
            device.println(_tagsLabel);
            device.println("</span>");
            device.println("<span class=\"tagsText\">");
            device.println(_tags);
            device.println("</span>");
            device.println("</div>");
        }

        device.println("</div>");
    }

    private void renderPreviewLinkContent(OrchidContext oc, Device device)
        throws OrchidException, IOException
    {
        if (_thumbnail == null) {
            device.println(getName());
        }
        else {
            _thumbnail.render(oc);
        }
    }
}
