package example.widget;

import java.io.IOException;

import com.atex.plugins.baseline.url.OrchidUrlResolver;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.search.widget.OSearchThumbBase;
import com.polopoly.cm.client.CMException;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;

import example.content.article.StandardArticlePolicy;
import example.content.image.ImagePolicy;

public class OSearchThumbArticle extends OSearchThumbBase {

    private static final long serialVersionUID = 3470106554572389508L;

    private StandardArticlePolicy article;
    private String name;
    private String imageThumbnailSrc;

    protected int getWidth() {
        return 250;
    }

    @Override
    public void initSelf(OrchidContext oc) throws OrchidException {
        super.initSelf(oc);
        try {
            article = (StandardArticlePolicy) getPolicy();
            name = article.getName();
            if (name == null || "".equals(name)) {
                name = article.getContentId().getContentId().getContentIdString();
            }

            ContentId imgCid = article.getDefaultReferredImage();
            if (imgCid != null) {
                ImagePolicy img =  (ImagePolicy) getContentSession().getPolicyCMServer().getPolicy(imgCid);
                imageThumbnailSrc = img.getThumbnailPath(new OrchidUrlResolver(oc));
            }

        } catch (CMException e) {
            throw new OrchidException(e);
        }
    }

    @Override
    protected void renderThumb(OrchidContext oc) throws IOException, OrchidException
    {
        Device device = oc.getDevice();
        device.print("<h2>" + name + "</h2>");
        if (imageThumbnailSrc != null) {
            device.println("<img src='" + imageThumbnailSrc + "' class='articleImage' />");
        }
        
        device.println("<span class='lead'>" + abbreviate(article.getItemDescription(), 200) + "</span>");
    }
    
    private String abbreviate(String str, int maxWidth)
    {
        if (str.length() <= maxWidth) {
            return str;
        } else {
            return str.substring(0, maxWidth - 3) + "...";
        }
    }

    @Override
    protected String getCSSClass() {
        return "customSearchArticle";
    }
}
