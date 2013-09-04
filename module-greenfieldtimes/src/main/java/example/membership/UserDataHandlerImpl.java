package example.membership;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.DefaultMajorNames;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.policy.ContentReferencePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.siteengine.membership.UserDataManagerImpl;
import com.polopoly.user.server.UserId;

/**
 * Class handing user data for a site user. When registering a new user on site a new
 * user data content needed by the UserDataManagerImpl needs to be created. That content
 * is managed by this class.
 */
public class UserDataHandlerImpl implements UserDataHandler
{
    private PolicyCMServer cmServer;

    public UserDataHandlerImpl(PolicyCMServer cmServer)
    {
        this.cmServer = cmServer;
    }
   
    /**
     * Get the registration form handler for a site.
     */
    public RegistrationFormHandler getFormHandler(String siteIdString)
        throws CMException
    {
        ContentId id;
        try {
            id = ContentIdFactory.createContentId(siteIdString);
        }
        catch (IllegalArgumentException e) {
            throw new CMException("Could not parse site id string.", e);
        }
        
        ContentPolicy siteContent = (ContentPolicy) cmServer.getPolicy(id);
        
        Policy membershipConfiguration =
            siteContent.getChildPolicy("membershipConfiguration");
        
        ContentReferencePolicy registrationElementSelector =
            (ContentReferencePolicy) membershipConfiguration.getChildPolicy("registrationElement");

        ContentId registrationElementId = registrationElementSelector.getReference();

        return (RegistrationFormHandler) cmServer.getPolicy(registrationElementId);
    }
    
    /**
     * Create the user data content for a site user.
     */
    public ContentPolicy createUserData(UserId userId)
        throws CMException
    {
        int contentDataMajor =
            cmServer.getMajorByName(DefaultMajorNames.CONTENT);
        
        ContentPolicy policy =
            (ContentPolicy) cmServer.createContent(contentDataMajor,
                                                   new ExternalContentId("p.siteengine.SiteUserData"));

        policy.setExternalId(UserDataManagerImpl.USER_DATA_EXTERNAL_ID_PREFIX + userId.getPrincipalIdString());
        
        return policy;
    }
    
    /**
     * Get the user data content for a site user.
     */
    public ContentPolicy getUserData(UserId userId)
        throws CMException
    {
        Policy userData = cmServer.getPolicy(
            new ExternalContentId(
                UserDataManagerImpl.USER_DATA_EXTERNAL_ID_PREFIX + userId.getPrincipalIdString()));
        
        return (ContentPolicy) userData;
    }
    
    /**
     * Commit the site user data content.
     */
    public void commitUserData(ContentPolicy userData)
        throws CMException
    {
        userData.commit();
    }
}
