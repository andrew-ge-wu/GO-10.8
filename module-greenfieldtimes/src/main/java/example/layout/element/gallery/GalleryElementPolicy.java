package example.layout.element.gallery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListProvider;
import com.polopoly.cm.collections.ContentListUtil;
import com.polopoly.cm.policy.Policy;
import com.polopoly.siteengine.layout.ContentRepresentative;
import com.polopoly.siteengine.standard.image.ImageResource;

import example.layout.element.ElementPolicy;

public class GalleryElementPolicy
    extends ElementPolicy
    implements ContentRepresentative
{
    public static final ExternalContentId GALLERY_INPUT_TEMPLATE_ID =
        new ExternalContentId("example.ImageGalleryElement");

    public ContentListProvider getPublishingQueue()
        throws CMException
    {
	ContentList publishingQueue = getContentList("publishingQueue");
	ContentReference entry = null;
	if(publishingQueue.size() > 0) {
            entry = publishingQueue.getEntry(0);
	}

        if (entry == null) {
            return null;
        }

        return (ContentListProvider) getCMServer().getPolicy(entry.getReferredContentId());
    }

    public void setPublishingQueue(ContentId contentListProvider)
        throws CMException
    {
        ContentList contentList = getContentList("publishingQueue");
        ContentListUtil.clear(contentList);
        ContentId referredContentId = contentListProvider.getContentId();
        contentList.add(0, new ContentReference(referredContentId, null));
    }
    
    public ImageResource getImageResource(int index)
        throws CMException
    {
    	ContentListProvider pq = null;
        try {
        	pq = getPublishingQueue();
        } catch(IndexOutOfBoundsException ioobe) {
        	return null;
        }
        if (pq == null) {
            return null;
        }
        
        if (pq.getContentList().size() <= index) {
            return null;
        }
        
        ContentReference entry = pq.getContentList().getEntry(index);
        if (entry == null) {
            return null;
        }
        
        ContentId contentId = entry.getReferredContentId();
        if (contentId == null) {
            return null;
        }
        
        return (ImageResource) getCMServer().getPolicy(contentId);
    }
    
    public List<ContentId> getRepresentedContent() {
        try {
            ContentListProvider pq = getPublishingQueue();
            if (pq != null) {
                List<ContentId> containedIds = new ArrayList<ContentId>();
                if (pq instanceof Policy) {
                    containedIds.add(((Policy)pq).getContentId().getContentId());
                }
                
                return containedIds;
            }
        } catch(CMException e) {
            logger.log(Level.WARNING, "Could not get publishing queue from image gallery " + this.getContentId().getContentIdString(), e);
            
        }
        return Collections.emptyList();
    }
}
