package example.layout.element.comments;

import com.polopoly.cm.client.filter.state.ModerationState.State;

/**
 * ModelTypeDescription interface for the comments element.
 */
public interface CommentsElementModelTypeDescription
{
    public boolean isOpenForComments();

    public State getInitialModerationState();
    
    public boolean isOnline();
}
