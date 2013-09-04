package example.content.rss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.imagemanager.Image;
import com.polopoly.cm.app.imagemanager.ImageManagerPolicy;
import com.polopoly.cm.app.imagemanager.ImageSetPolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.metadata.Dimension;
import com.polopoly.metadata.Entity;
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
import com.polopoly.siteengine.standard.feed.FeedProvider;
import com.polopoly.util.StringUtil;

/**
 * Model builder for the RSS Article used construct RSS Feeds based on
 * {@link Rssable} policies.
 *
 * It also includes functionality like computing last build date
 */
public class RenderControllerRssArticle
    extends RenderControllerRssLink
{
    private static final String CDATA_BEGIN = "<![CDATA[";
    private static final String CDATA_END = "]]>";

    protected final static String CLASS = RenderControllerRssArticle.class.getName();
    protected final static Logger logger = Logger.getLogger(CLASS);
    private static final Object LOCK = new Object();

    private final RssDateFormatter rssDateFormatter = new RssDateFormatter();

    @Override
    @SuppressWarnings("unchecked")
    public void populateModelAfterCacheKey(RenderRequest request, TopModel m,
            CacheInfo cacheInfo, ControllerContext context) {

        super.populateModelAfterCacheKey(request, m, cacheInfo, context);

        Date lastBuildDate = null;

        Model localModel = m.getLocal();
        Model thisModel = localModel.getModel("content");

        // Retrieve the selected categories for the feed
        MetadataAware categorizationProvider =
                (MetadataAware) ModelPathUtil.getBean(thisModel, "categories");
        Set<String> entities = getFeedEntities(categorizationProvider.getMetadata());

        // Get the items from the publishing Queues and/or articles
        List<?> contents = (List<?>) ModelPathUtil.get(thisModel, "publishingQueues/list");
        List<RssItem> rssItems = new ArrayList<RssItem>();

        int maxLength =
                (ModelPathUtil.get(thisModel, "length/value") != null) ? Integer.valueOf(
                        ModelPathUtil.get(thisModel, "length/value").toString()).intValue() : 0;

        if (contents != null) {
            for (int i = 0; i < contents.size(); i++) {
                Policy p = (Policy) ModelPathUtil.getBean((Model) contents.get(i), "content");
                if (p instanceof Rssable) {
                    prepareFeedItem((Rssable) p, rssItems, lastBuildDate);
                } else if (p instanceof FeedProvider) {
                    List<?> feedables;
                    try {
                        feedables = ((FeedProvider) p).getFeedables();
                    } catch (CMException e) {
                        logger.log(Level.WARNING, "Unable to get feed provider", e);
                        feedables = null;
                    }

                    if (feedables != null) {
                        Iterator<?> it = feedables.iterator();
                        if (logger.isLoggable(Level.FINER)) {
                            logger.finer("Got " + feedables.size()
                                    + " hits from queue: "
                                    + ModelPathUtil.get((Model) contents.get(i), "content/name"));
                        }

                        while (it.hasNext()) {
                            Rssable rssable = (Rssable) it.next();
                            if (rssable != null) {
                                lastBuildDate = prepareFeedItem(rssable, rssItems, lastBuildDate);
                            }
                        }
                    }
                }
            }
        }

        // Sort the items based on pub date and make sure there are no duplicate
        // entries
        Set<RssItem> unique = new HashSet<RssItem>();
        unique.addAll(rssItems);
        rssItems.clear();
        rssItems.addAll(unique);
        synchronized (LOCK) { // Required due to a bug in at least java 1.5
            Collections.sort(rssItems);
        }

        if (maxLength > 0 && rssItems.size() > maxLength) {
            rssItems = rssItems.subList(0, maxLength);
        }

        // Append CDATA to name
        ModelPathUtil.set(localModel, "title", appendCDATA((String) ModelPathUtil.get(thisModel,
                "name")));

        // Append CDATA to description
        ModelPathUtil.set(localModel, "description", appendCDATA((String) ModelPathUtil.get(
                thisModel, "description/value")));

        // Add computed last build date to model, fallback to todays date
        if (lastBuildDate == null) {
            lastBuildDate = new Date();
        }

        // Add last build date
        ModelPathUtil.set(localModel, "lastBuildDate", rssDateFormatter.format(lastBuildDate));

        // Add the categories
        ModelPathUtil.set(localModel, "categories", entities);

        // Add the image of the feed
        ImageManagerPolicy imageManager =
                (ImageManagerPolicy) ModelPathUtil.getBean(thisModel, "image");
        try {
            ImageSetPolicy imagePolicy = imageManager.getSelectedImage();
            if (imagePolicy != null) {

                Image image = null;
                image = imagePolicy.getImage();

                if (image != null) {
                    // URL
                    ModelPathUtil.set(localModel, "imageId", imagePolicy.getContentId());
                    ModelPathUtil.set(localModel, "imagePath", image.getPath());
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unable to get feed image", e);
        }
        // Add the items to the model
        ModelPathUtil.set(localModel, "rssItems", rssItems);
    }

    Set<String> getFeedEntities(Metadata metadata)
    {
        Set<String> entities = new LinkedHashSet<String>();
        for (Dimension dimension : metadata.getDimensions()) {
            if (!dimension.isEnumerable()) {
                continue;
            }
            Iterable<List<Entity>> paths = MetadataUtil.traverseEntityPaths(dimension, Filtering.ONLY_LEAVES);
            for (List<Entity> path : paths) {
                StringBuilder category = new StringBuilder();
                String separator = "";
                for (Entity entity : path) {
                    category.append(separator);
                    category.append(entity.getName());
                    separator = "/";
                }
                entities.add(appendCDATA(category.toString()));
            }
        }
        return entities;
    }

    protected Date prepareFeedItem(Rssable rssable, List<RssItem> rssItems, Date lastBuildDate)
    {
        Date itemPublishedDate = rssable.getItemPublishedDate();
        ContentId contentId = rssable.getItemContentId();
        // Update last build date of the feed
        if (lastBuildDate == null
                || (itemPublishedDate != null && lastBuildDate.before(itemPublishedDate))) {
            lastBuildDate = itemPublishedDate;
        }

        // Make sure there are no bad characters in the title or
        String appendCDATA = appendCDATA(rssable.getItemTitle());

        // description which can break the feed
        String appendCDATA2 = appendCDATA(rssable.getItemDescription());

        String contentIdString = contentId.getContentId().getContentIdString();
        RssItem rssItem =
                new RssItem(appendCDATA, appendCDATA2, itemPublishedDate, contentIdString, rssable
                        .getItemParentIds());
        rssItems.add(rssItem);
        return lastBuildDate;
    }

    /**
     * Convenience method to add a CDATA section to the provided String
     * argument.
     *
     * @param str
     * @return <!CDATA[[str]]>
     */
    private String appendCDATA(String str)
    {
        StringBuffer ret = new StringBuffer();
        if (!StringUtil.isEmpty(str)) {
            ret.append(CDATA_BEGIN);
            ret.append(str);
            ret.append(CDATA_END);
        }
        return ret.toString();
    }
}
