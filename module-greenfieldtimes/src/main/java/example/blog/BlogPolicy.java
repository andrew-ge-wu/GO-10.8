package example.blog;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.polopoly.cm.AnnotatedContent;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.UrlPathSegment;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ComponentSet;
import com.polopoly.cm.collections.ReadOnlyComponentSet;
import com.polopoly.cm.path.PathSegment;
import com.polopoly.model.ModelTypeDescription;
import com.polopoly.siteengine.structure.ParentPathResolver;
import com.polopoly.siteengine.util.FriendlyUrlConverter;
import com.polopoly.user.server.UserId;

import example.content.ContentBasePolicy;

@AnnotatedContent
public class BlogPolicy extends ContentBasePolicy implements BaseModelTypeDescription, PathSegment,
        ModelTypeDescription
{
    private static final String DESCRIPTION = "description";
    private static final String PATH_SEGMENT = "pathsegment";
    private static final String OWNERS = "owners";

    @UrlPathSegment
    public String getPathSegmentString()
        throws CMException
    {
        String pathSegment = ((SingleValuePolicy) getChildPolicy(PATH_SEGMENT)).getValue();
        pathSegment = pathSegment != null && pathSegment.length() > 0 ? pathSegment : getName();
        pathSegment = pathSegment != null ? FriendlyUrlConverter.convertPermissive(pathSegment).toLowerCase() : null;
        return pathSegment;
    }

    public void setPathSegmentString(String value)
        throws CMException
    {
        ((SingleValuePolicy) getChildPolicy(PATH_SEGMENT)).setValue(value);
    }

    public String getDescription()
        throws CMException
    {
        return ((SingleValuePolicy) getChildPolicy(DESCRIPTION)).getValue();
    }

    public void setDescription(String value)
        throws CMException
    {
        ((SingleValuePolicy) getChildPolicy(DESCRIPTION)).setValue(value);
    }

    public void setOwnerIds(Set<UserId> userIds)
        throws CMException
    {
        ComponentSet set = new ComponentSet(this, OWNERS);
        set.clear();

        for (UserId userId : userIds) {
            set.add(userId.getPrincipalIdString());
        }
    }

    public Set<UserId> getOwnerIds()
        throws CMException
    {
        ReadOnlyComponentSet set = new ReadOnlyComponentSet(this, OWNERS);

        Set<UserId> userIds = new HashSet<UserId>();
        Iterator<?> userIdIter = set.iterator();
        while (userIdIter.hasNext()) {
            String userIdString = (String) userIdIter.next();
            userIds.add(new UserId(userIdString));
        }

        return userIds;
    }

    public boolean isAllowedToEdit(UserId userId)
        throws CMException
    {
        ReadOnlyComponentSet set = new ReadOnlyComponentSet(this, OWNERS);
        return set.contains(userId.getPrincipalIdString());
    }

    public void setInsertParent(ContentId parentSite) throws CMException {
        ParentPathResolver parentPathResolver = new ParentPathResolver();
        synchronized (parentPathResolver) {
            setContentReference("polopoly.Parent", "insertParentId", parentSite);
            clearParentIdsCache();
        }
    }

}
