package example.layout.element.comments;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.filter.state.ModerationState.State;
import com.polopoly.community.comment.CommentList;
import com.polopoly.community.comment.CommentListFactory;

import example.content.ContentBasePolicy;

public class CommentsElementPolicy extends ContentBasePolicy
    implements CommentsElementModelTypeDescription
{
    private static final Logger LOG = Logger.getLogger(CommentsElementPolicy.class.getName());

    private static final String MODERATION_LEVEL_COMPONENT_GROUP_NAME = "moderationLevel";

    private static final String IS_ONLINE_COMPONENT_NAME = "isOnline";
    private static final String IS_OPEN_FOR_COMMENTS_COMPONENT_NAME = "isOpenForComments";
    private static final String INITIAL_MODERATION_STATE_COMPONENT_NAME = "initialModerationState";

    private CommentList _comments;
    private final CmClient _cmClient;

    public CommentsElementPolicy(CmClient cmClient)
    {
        _cmClient = cmClient;
    }

    protected void initSelf()
    {
        super.initSelf();
        _comments = new CommentListFactory().create(_cmClient, getContentId());
    }

    public CommentList getCommentList()
    {
        return _comments;
    }

    public boolean isOpenForComments() {
        return isMarkedAsOnline() && isMarkedAsOpenForComments();
    }

    public boolean isOnline()
    {
        /* Basically just a delegation to isMarkedAsOnline, to harmonize with isOpenForComments() */
        return isMarkedAsOnline();
    }

    public boolean isMarkedAsOnline()
    {
        boolean isOnline = true;

        try {
            String isOnlineString = getComponent(MODERATION_LEVEL_COMPONENT_GROUP_NAME,
                                                 IS_ONLINE_COMPONENT_NAME);

            if (isOnlineString != null) {
                isOnline = Boolean.valueOf(isOnlineString);
            }
        } catch (CMException cme) {
            LOG.log(Level.WARNING, "Error while reading 'isOnline' setting from content! Defaulting to true.", cme);
        }

        return isOnline;
    }

    public void setMarkedAsOnline(boolean isOnline)
    {
        try {
            setComponent(MODERATION_LEVEL_COMPONENT_GROUP_NAME,
                         IS_ONLINE_COMPONENT_NAME,
                         String.valueOf(isOnline));
        } catch (CMException cme) {
            LOG.log(Level.WARNING, "Error while writing 'isOnline' setting to content!", cme);
        }
    }

    public boolean isMarkedAsOpenForComments()
    {
        boolean isOpenForComments = true;

        try {
            String isOpenForCommentsString = getComponent(MODERATION_LEVEL_COMPONENT_GROUP_NAME,
                                                          IS_OPEN_FOR_COMMENTS_COMPONENT_NAME);

            if (isOpenForCommentsString != null) {
                isOpenForComments = Boolean.valueOf(isOpenForCommentsString);
            }
        } catch (CMException cme) {
            LOG.log(Level.WARNING, "Error while reading 'isOpenForComments' setting from content! Defaulting to true.", cme);
        }

        return isOpenForComments;
    }

    public void setMarkedAsOpenForComments(boolean isOpenForComments)
    {
        try {
            setComponent(MODERATION_LEVEL_COMPONENT_GROUP_NAME,
                         IS_OPEN_FOR_COMMENTS_COMPONENT_NAME,
                         String.valueOf(isOpenForComments));
        } catch (CMException cme) {
            LOG.log(Level.WARNING, "Error while writing 'isOpenForComments' setting to content!", cme);
        }
    }

    public State getInitialModerationState()
    {
        State initialModerationState = State.PUBLIC_PENDING;

        try {
            String initialModerationStateString = getComponent(MODERATION_LEVEL_COMPONENT_GROUP_NAME,
                                                               INITIAL_MODERATION_STATE_COMPONENT_NAME);

            if (initialModerationStateString != null) {
                initialModerationState = State.valueOf(initialModerationStateString);
            }
        } catch (CMException cme) {
            LOG.log(Level.WARNING, "Error while reading 'initialModerationState' setting from content! Defaulting to PUBLIC_PENDING.", cme);
        }

        return initialModerationState;
    }

    public void setInitialModerationState(State initialModerationState)
    {
        try {
            setComponent(MODERATION_LEVEL_COMPONENT_GROUP_NAME,
                         INITIAL_MODERATION_STATE_COMPONENT_NAME,
                         (initialModerationState != null) ? initialModerationState.toString() : null);
        } catch (CMException cme) {
            LOG.log(Level.WARNING, "Error while writing 'initialModerationState' setting to content!", cme);
        }
    }
}
