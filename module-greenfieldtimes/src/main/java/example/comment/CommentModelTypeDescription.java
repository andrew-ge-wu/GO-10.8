package example.comment;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.filter.state.ModerationState.State;
import com.polopoly.moderation.ModeratablePolicy;

public interface CommentModelTypeDescription extends ModeratablePolicy
{
    public String getName() throws CMException;

    public String getText() throws CMException;

    public String getAuthor() throws CMException;

    public State getModerationState() throws CMException;
}
