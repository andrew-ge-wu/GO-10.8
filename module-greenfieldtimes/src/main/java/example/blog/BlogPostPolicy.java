package example.blog;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.PublishingDateTime;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.filter.state.DeleteState;
import com.polopoly.cm.policy.Policy;
import com.polopoly.model.ModelTypeDescription;
import com.polopoly.user.server.UserId;

import example.content.ContentBasePolicy;


public class BlogPostPolicy extends ContentBasePolicy implements BaseModelTypeDescription,
        ModelTypeDescription, PublishingDateTime
{
    private static final String TEXT = "text";
    DeleteState deleteState = new DeleteState();

    public String getText() throws CMException
    {
        return ((SingleValuePolicy) getChildPolicy(TEXT)).getValue();
    }

    public void setText(String text) throws CMException
    {
        ((SingleValuePolicy) getChildPolicy(TEXT)).setValue(text);
    }

    public boolean isAllowedToEdit(UserId userId) throws CMException
    {
        ContentId blogId = getSecurityParentId();
        
        Policy policy = getCMServer().getPolicy(blogId);
        
        if ( policy instanceof BlogPolicy ){
            BlogPolicy blogPolicy = (BlogPolicy)policy;
            
            if (blogPolicy.isAllowedToEdit(userId)) {
                return true;
            }
        }
        
        return false;
    }

    public void delete() throws CMException
    {
        deleteState.delete(this);
    }

    public long getPublishingDateTime() {
        try {
            return getCreated().getTime();
        } catch (CMException e) {
            throw new RuntimeException(e);
        }
    }
    
    /* These two fulfill the expectations of the comment element output templates,
     * but cannot be specifically controlled for blogs posts (yet) */
    public boolean isOpenForComments() {
        return true;
    }

    public boolean isOnline() {
        return true;
    }

}
