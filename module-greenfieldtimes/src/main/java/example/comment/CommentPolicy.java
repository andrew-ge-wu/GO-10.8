package example.comment;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentInfo;
import com.polopoly.cm.PublishingDateTime;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.app.preview.PreviewUtil;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.filter.state.DeleteState;
import com.polopoly.cm.client.filter.state.ModerationState;
import com.polopoly.cm.client.filter.state.ModerationState.State;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.siteengine.structure.ParentPathResolver;


public class CommentPolicy
    extends ContentPolicy
    implements CommentModelTypeDescription, PublishingDateTime
{
    private static final String TEXT = "text";
    private static final String AUTHOR = "author";
    private static final String AUTHOR_IP = "authorIP";
    private static final String AUTHOR_USER_ID = "authorUserId";
    private static final String AUTHOR_WAS_LOGGED_IN = "authorWasLoggedIn";

    private final DeleteState deleteState = new DeleteState();
    private final ModerationState moderationState = new ModerationState();

    public String getText() throws CMException
    {
        return ((SingleValuePolicy) getChildPolicy(TEXT)).getValue();
    }

    public void setText(String text) throws CMException
    {
        ((SingleValuePolicy) getChildPolicy(TEXT)).setValue(text);
    }

    public String getAuthor() throws CMException
    {
        return ((SingleValuePolicy) getChildPolicy(AUTHOR)).getValue();
    }

    public void setAuthor(String author) throws CMException
    {
        ((SingleValuePolicy) getChildPolicy(AUTHOR)).setValue(author);
    }

    public String getAuthorIP() throws CMException
    {
        return ((SingleValuePolicy) getChildPolicy(AUTHOR_IP)).getValue();
    }

    public void setAuthorIP(String authorIP) throws CMException
    {
        ((SingleValuePolicy) getChildPolicy(AUTHOR_IP)).setValue(authorIP);
    }

    public String getAuthorUserId() throws CMException
    {
        return ((SingleValuePolicy) getChildPolicy(AUTHOR_USER_ID)).getValue();
    }

    public void setAuthorUserId(String authorUserId) throws CMException
    {
        ((SingleValuePolicy) getChildPolicy(AUTHOR_USER_ID)).setValue(authorUserId);
    }

    public String isAuthorLoggedIn() throws CMException
    {
        return ((SingleValuePolicy) getChildPolicy(AUTHOR_WAS_LOGGED_IN)).getValue();
    }

    public void setAuthorLoggedIn(String authorLoggedIn) throws CMException
    {
        ((SingleValuePolicy) getChildPolicy(AUTHOR_WAS_LOGGED_IN)).setValue(authorLoggedIn);
    }

    public long getPublishingDateTime()
    {
        ContentInfo info;
        try {
            info = getCMServer().getContentInfo(getContentId());
        } catch (CMException e) {
            throw new RuntimeException(e);
        }
        return info.getCreationTime();
    }

    public void delete() throws CMException
    {
        deleteState.delete(this);
    }

    public boolean isDeleted() throws CMException
    {
        return deleteState.isDeleted(this);
    }

    public void setModerationState(State state) throws CMException
    {
        moderationState.setState(this, state);
    }

    public State getModerationState() throws CMException
    {
        return moderationState.getState(this);
    }

    public String getId()
    {
        return getContentId().getContentId().getContentIdString();
    }

    public String toJSONString()
    {
        return "\"CommentPolicy-" + getId() + "\"";
    }

    public String getParentLink() throws CMException {
        Policy parentContent = getParentOfCommentsElement(getCMServer());
        String link = null;
        String contentIdString = new ContentId(getContentId()).getContentIdString();
        link = new PreviewUtil(getCMServer()).getPreviewUrl(parentContent.getContentId(), "www");
        if (link == null || link.trim().length() == 0) {
            link = "/" + contentIdString;
        }
        String showComment = "showCommentId=" + getId();
        String showComments = "commentsElementId=" + getParentId();
        if (link.indexOf("?") == -1) {
            link = link + "?";
        } else {
            link = link + "&";
        }
        return link + showComments + "&" + showComment;
    }

    private String getParentId() throws CMException
    {
        ParentPathResolver parentPathResolver = new ParentPathResolver();
        ContentId[] parentPath = parentPathResolver.getParentPath(getContentId(), getCMServer());
        return String.valueOf(parentPath[parentPath.length - 2].getContentId().getMinor());
    }

    private Policy getParentOfCommentsElement(PolicyCMServer cmServer) throws CMException
    {
        ParentPathResolver parentPathResolver = new ParentPathResolver();
        ContentId[] parentPath = parentPathResolver.getParentPath(getContentId(), cmServer);
        // parentPath looks like [pages..., parent, comments element, this],
        // we want 'parent' (usually an article or blog post).
        Policy parentPolicy = null;
        if (parentPath.length >= 3) {
            parentPolicy = cmServer.getPolicy(parentPath[parentPath.length - 3]);
        }
        else if (parentPath.length == 2) {
            parentPolicy = cmServer.getPolicy(parentPath[parentPath.length - 2]);
        }
        else {
            parentPolicy = this;
        }
        return parentPolicy;
    }

    public String getParentLinkText() throws CMException {
        Policy parentContentPolicy = getParentOfCommentsElement(getCMServer());
        return parentContentPolicy.getContent().getName();
    }
}
