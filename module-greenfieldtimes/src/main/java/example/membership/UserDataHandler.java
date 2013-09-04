package example.membership;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.user.server.UserId;

/**
 * Handling of user data, see implementing class for details.
 */
public interface UserDataHandler
{
    public RegistrationFormHandler getFormHandler(String siteIdString)
        throws CMException;
    
    public ContentPolicy createUserData(UserId userId)
        throws CMException;
    
    public ContentPolicy getUserData(UserId userId)
        throws CMException;

    public void commitUserData(ContentPolicy userData)
        throws CMException;
    
}
