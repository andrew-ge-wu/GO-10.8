package example.content.video;

import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.plugins.baseline.url.UrlResolver;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.imagemanager.ImageManagerPolicy;
import com.polopoly.cm.app.policy.FilePolicy;
import com.polopoly.cm.app.policy.SelectableSubFieldPolicy;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;

import example.content.ImageProviderPolicy;

/**
 * Policy representing a video resource.
 */
public class VideoResourcePolicy extends ImageProviderPolicy
{
    private static final Logger LOG = Logger.getLogger(VideoResourcePolicy.class.getName());

    private static final String MOVIE_ICON = "image/48x48/video/x-flv.png";
    private static final ExternalContentId ICONS_CONTENT = new ExternalContentId("p.Icons");

    public String getThumbnailPath(UrlResolver urlResolver) {
        String thumbNailPath = super.getThumbnailPath(urlResolver);

        return thumbNailPath != null ? thumbNailPath : getMovieIconPath(urlResolver);
    }

    private String getMovieIconPath(UrlResolver urlResolver) {
        try {
            ExternalContentId iconsId = ICONS_CONTENT;
            getCMServer().getContent(iconsId).getFileInfo(MOVIE_ICON);
            return urlResolver.getFileUrl(iconsId, MOVIE_ICON);
        } catch (FileNotFoundException noIconFound) {
        } catch (Exception e) {
            LOG.log(Level.WARNING,
                    "Unable to get icon file info for icon: " + MOVIE_ICON,
                    e);
        }
        return  null;
    }
    
    public String getPreviewPath(UrlResolver urlResolver) {
        try {
            return getVideoPath(urlResolver);
        } catch (CMException cme) {
            LOG.log(Level.WARNING, "Unable to get video preview url", cme);
        }
        return null;
    }

    public String getVideoPath(UrlResolver urlResolver) throws CMException {
        SelectableSubFieldPolicy videoSourceSelect =
            (SelectableSubFieldPolicy) getChildPolicy("file");
        return ("url".equals(videoSourceSelect.getSelectedSubFieldName()))
            ? ((SingleValuePolicy) videoSourceSelect.getChildPolicy("url")).getValue()
            : urlResolver.getFileUrl(getContentId(), ((FilePolicy) videoSourceSelect.getChildPolicy("flashfile")).getFullFilePath());
    }
    
    @Override
    protected ImageManagerPolicy getImageProvider() throws CMException {
        return (ImageManagerPolicy) getChildPolicy("image");
    }
}
