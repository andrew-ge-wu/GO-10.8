package example.widget;

import java.io.IOException;

import com.atex.plugins.baseline.url.OrchidUrlResolver;
import com.polopoly.cm.app.imagemanager.Image;
import com.polopoly.cm.app.imagemanager.ImageProvider;
import com.polopoly.cm.app.imagemanager.ImageSet;
import com.polopoly.cm.app.imagemanager.ImageSetPolicy;
import com.polopoly.cm.app.search.widget.OSearchThumbBase;
import com.polopoly.cm.client.CMException;
import com.polopoly.html.CharConv;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;

import example.content.image.ImagePolicy;

public class OSearchThumbImage extends OSearchThumbBase {

    private static final long serialVersionUID = 842743607687086728L;

    private ImageSet image;
    private String imageThumbnailSrc;
    private String name;
    private String shortName;
    private int width;
    private int height;
    private String imageSuffix;

    private int thumbnailWidth;
    private int thumbnailHeight;

    @Override
    public void initSelf(OrchidContext oc) throws OrchidException {
        super.initSelf(oc);
        try {
            name = getPolicy().getContent().getName();
            if (getPolicy() instanceof ImagePolicy) {
                image = ((ImagePolicy) getPolicy()).getImageSet();
            } else {
                for (Object childName : getPolicy().getChildPolicyNames()) {
                    Object child = getPolicy().getChildPolicy((String) childName);
                    if (child instanceof ImageProvider) {
                        image = ((ImageProvider) child).getSelectedImage();
                        break;
                    }
                }
            }
            if (name == null || "".equals(name)) {
                name = getPolicy().getContentId().getContentId().getContentIdString();
            }

            shortName = clipString(name, 16);

            if (image != null) {
                Image thumbnail = image.getThumbnail(ImageSetPolicy.THUMBNAIL_SIZE_MEDIUM);
                width = image.getImage().getWidth();
                height = image.getImage().getHeight();
                String[] path = image.getImage().getPath().split("\\.");
                imageSuffix = path.length>1?path[path.length-1].toUpperCase():"?";


                if (thumbnail != null) {
                    thumbnailWidth = thumbnail.getWidth();
                    thumbnailHeight = thumbnail.getHeight();
                    imageThumbnailSrc = thumbnail.isAbsolute() ?
                            thumbnail.getPath() :
                            new OrchidUrlResolver(oc).getFileUrl(getPolicy().getContentId(), thumbnail.getPath());
                }
            }

        } catch (CMException e) {
            throw new OrchidException(e);
        } catch (IOException e) {
            throw new OrchidException(e);
        }
    }

    private String clipString(String string, int maxWidth)
    {
        Integer cutoffPoint = null;
        int count = 0;
        float width = 0;
        for (char c : string.toCharArray())
        {
            count++;
            width += 1;
            if ( c == 'm' || c == 'M' || c == 'W' || c == 'w') {
                width += 0.8;
            }
            if (cutoffPoint == null && width > maxWidth) {
                cutoffPoint = count;
            }
        }

        if (cutoffPoint != null && cutoffPoint < string.length() - 3) {
            return string.substring(0, cutoffPoint) + "...";
        } else {
            return string;
        }
    }

    @Override
    protected int getWidth() {
        return 115;
    }

    @Override
    protected void renderThumb(OrchidContext oc) throws IOException, OrchidException {
        Device device = oc.getDevice();
        device.print("<div class='customSearchImageDiv'>");
        String dimension = "&nbsp;";
        if (width >= 0 && height >= 0) {
            dimension = width + "x" + height;
        }
        device.print("<span style=\"float: left\">" + dimension + "</span>");
        device.print("<span style=\"float: right\">" + CharConv.CC.toHTML(imageSuffix) + "</span>");
        if (imageThumbnailSrc != null) {
            String dimensionAttributes = "";
            if (thumbnailWidth >= 0 && thumbnailHeight >= 0) {
                dimensionAttributes = String.format(" width='%dpx' height='%dpx'", thumbnailWidth, thumbnailHeight);
            }
            device.println("<img src='" + imageThumbnailSrc + "'" + dimensionAttributes + "/>");
        }
        device.print("<div title='" + CharConv.CC.toHTML(name) + "'>" + shortName + "</div>");

        device.print("</div>");
    }


    @Override
    protected String getCSSClass() {
        return "customSearchImage";
    }
}
