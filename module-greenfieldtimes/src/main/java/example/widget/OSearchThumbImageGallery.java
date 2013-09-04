package example.widget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.atex.plugins.baseline.url.OrchidUrlResolver;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.app.search.widget.OSearchThumbBase;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OFrameEventLink;

import example.content.image.ImagePolicy;

public class OSearchThumbImageGallery extends OSearchThumbBase {

    private static final long serialVersionUID = -3514489073673059820L;
    private OFrameEventLink nameLink;
    private ContentList publishingQueue;
    private PolicyCMServer _cmServer;

    @Override
    public void initSelf(OrchidContext oc) throws OrchidException {
        try {
            super.initSelf(oc);

            String name;
            nameLink = new OFrameEventLink();
            addAndInitChild(oc, nameLink);

            ContentPolicy policy = (ContentPolicy) getPolicy();
            name = policy.getName();
            if (name == null || "".equals(name))
                name = policy.getContentId().getContentIdString();
            nameLink.setLabel(name);
            nameLink.setEventData(linkEventData);

            publishingQueue = policy.getContentList("publishingQueue");
            _cmServer = getContentSession().getPolicyCMServer();
        } catch (CMException e) {
            throw new OrchidException(e);
        }
    }

    @Override
    protected int getWidth() {
        return 100;
    }

    @Override
    protected void renderThumb(OrchidContext oc) throws IOException, OrchidException {
        Device device = oc.getDevice();

        device.print("<div class=\"customSearchImageGalleryTitle\" >");
        nameLink.render(oc);
        device.print("</div>");

        ContentList publishingQueueContentList = getPublishingQueueContentList();
        if (publishingQueueContentList != null) {
            List<String> imgThumbUrls = getImageThumbUrls(oc, publishingQueueContentList);
            for (int i = 0; i < Math.min(imgThumbUrls.size(), 4); i++) {
                String position = (i % 2 == 0) ? "left" : "right";
                device.print("<div class=\"imageGalleryThumb\" style=\"float: "+position+";\"><img src=\"" + imgThumbUrls.get(i) + "\"></div>");
            }
        }
    }

    /**
     * build list of thumbnail urls from a publishing queue content list
     *
     * @param oc
     * @param publishingQueueContentList
     * @return list of urls to thumbs
     * @throws OrchidException
     */
    private List<String> getImageThumbUrls(OrchidContext oc, ContentList publishingQueueContentList) throws OrchidException {
        List<String> imgThumbUrls = new ArrayList<String>();

        try {
            ListIterator<ContentReference> it = publishingQueueContentList.getListIterator();

            while (it.hasNext()) {
                ContentReference ref = it.next();
                Policy policy = _cmServer.getPolicy(ref.getReferredContentId());
                if (policy instanceof ImagePolicy) {
                    String imageThumbnailSrc = ((ImagePolicy) policy).getThumbnailPath(new OrchidUrlResolver(oc));
                    imgThumbUrls.add(imageThumbnailSrc);
                }
            }

        } catch (CMException e) {
            throw new OrchidException(e);
        }
        return imgThumbUrls;
    }

    /**
     * fetch the content list that contains images from the publishing queue
     * @return the content list in the publishing queue
     * @throws OrchidException
     */
    private ContentList getPublishingQueueContentList() throws OrchidException {
        ContentList publishingQueueContentList;
        if (publishingQueue.size() == 0) {
            return null;
        }
        try {
            ContentReference ref = publishingQueue.getEntry(0);
            ContentId publishingQueueEntryId = ref.getReferenceMetaDataId();
            if (publishingQueueEntryId == null) {
                publishingQueueEntryId = ref.getReferredContentId();
            }

            ContentPolicy publishingQueuePolicy = (ContentPolicy) _cmServer.getPolicy(publishingQueueEntryId);
            publishingQueueContentList = publishingQueuePolicy.getContentList();
        } catch (CMException e) {
            return null;
        }
        return publishingQueueContentList;
    }

    @Override
    protected String getCSSClass() {
        return "customSearchImageGallery";
    }

}
