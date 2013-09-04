package example.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.tools.view.context.ViewContext;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentOperationFailedException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.servlet.RequestPreparator;
import com.polopoly.siteengine.structure.Site;
import com.polopoly.siteengine.structure.SiteRoot;

/**
 * Contains utilities for working with paths.
 */
public class PathUtil
{
    private static final Logger logger = Logger.getLogger(PathUtil.class.getName());

    private PolicyCMServer cmServer;

    /**
     * Initializes the tool by getting cm server from request
     * from initParam which should be of type ViewContext.
     *
     * @param initParam the current ViewContext
     */
    public void init(Object initParam)
    {
        if (initParam instanceof ViewContext) {
            HttpServletRequest request = ((ViewContext) initParam).getRequest();
            PolicyCMServer server = RequestPreparator.getCMServer(request);

            this.cmServer = server;
        } else {
            throw new IllegalArgumentException("initParam not of type ViewContext. Need request to get cm server.");
        }
    }

    /**
     * Gets a full path to site root for a content,
     * using insert- or security parent.
     */
    public ContentId[] getPathToRoot(ContentId contentId)
    {
        List<ContentId> parentList = new ArrayList<ContentId>();

        try {
            ContentPolicy parent = (ContentPolicy) cmServer.getPolicy(contentId);

            // Follow parents until content of type SiteRoot is found
            HashSet<ContentId> visitedParents = new HashSet<ContentId>();

            while (parent != null && !(parent instanceof SiteRoot)) {
                ContentId parentId = parent.getContentId().getContentId();

                if (visitedParents.contains(parentId)) {
                    logger.warning("Parent chain loop found. Content: " + parentId + ". Parents: "
                            + Arrays.toString(parentList.toArray()));
                    return new ContentId[] { contentId };
                }

                parentList.add(0, parent.getContentId());

                // Check if insert parent exists
                ContentId id = parent.getContentReference("polopoly.Parent", "insertParentId");

                // Otherwise, use security parent
                if (id == null) {
                    id = parent.getSecurityParentId();
                }

                // Check if we are passing security root department (minor == 1)
                if (id.getMinor() < 1) {
                    return new ContentId[] { contentId };
                }

                try {
                    parent = (ContentPolicy) cmServer.getPolicy(id);
                } catch (ContentOperationFailedException e) {
                    if (id.getVersion() != VersionedContentId.LATEST_VERSION) {
                        parent = (ContentPolicy) cmServer.getPolicy(id.getLatestVersionId());
                    }

                    else throw e;
                }
            }
        } catch (CMException cme) {
            logger.warning(cme.getLocalizedMessage());
            return new ContentId[] { contentId };
        }

        return parentList.toArray(new ContentId[0]);
    }

    /**
     * A path is considered valid if it contains
     * an element of the type Site.
     *
     * @param path the path to validate
     * @return true if path is valid, else false
     */
    public boolean isValidPath(ContentId[] path)
    {
        if (path == null) {
            return false;
        }
        return containsType(path, Site.class);
    }

    /**
     * Checks if the path contains
     * an element which has a policy of certain type.
     *
     * @param path the path to check
     * @param klass class types of the path segments
     *
     * @return true if path contains element of certain type, else false
     */
    public boolean containsType(ContentId[] path,
                                Class<Site> klass)
    {
        try {
            for (int i = 0; i < path.length; i++) {
                if (klass.isInstance(cmServer.getPolicy(path[i]))) {
                    return true;
                }
            }
        } catch (CMException ignore) {
        }

        return false;
    }

    /**
     * Checks if a content id path contains a content id.
     *
     * @param contentId the content id to check for
     * @param path the path to check
     *
     * @return true if path contains contentId
     */
    public boolean contains(ContentId contentId,
                            ContentId[] path)
    {
        if (contentId == null || path == null) {
            return false;
        }

        for (int i = 0; i < path.length; i++) {
            if (path[i].equalsIgnoreVersion(contentId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Concatenates id to path
     *
     * @param path the path to concatinate to
     * @param id the id to concatinate
     *
     * @return the concatinated path
     */
    public ContentId[] concat(ContentId[] path,
                              ContentId id)
    {
        if (path == null || id == null) {
            return new ContentId[0];
        }

        ContentId[] newPath = new ContentId[path.length + 1];

        for (int i = 0; i < path.length; i++) {
            newPath[i] = path[i];
        }

        newPath[newPath.length - 1] = id;

        return newPath;
    }

    /**
     * Concatenates id to path
     *
     * @param path the path as a <code>List</code>
     * @param id the <code>ContentId</code> to concatenate to path
     *
     * @return the new path
     */
    public ContentId[] concat(List<ContentId> path,
                              ContentId id)
    {
        if (path == null || id == null) {
            return new ContentId[0];
        }

        List<ContentId> newPathList = new ArrayList<ContentId>(path.size() + 1);

        newPathList.addAll(path);
        newPathList.add(id);

        return newPathList.toArray(new ContentId[0]);
    }
}
