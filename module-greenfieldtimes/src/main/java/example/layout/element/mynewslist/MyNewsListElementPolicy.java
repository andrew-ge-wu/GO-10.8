package example.layout.element.mynewslist;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.ModelTypeDescription;
import com.polopoly.siteengine.membership.ServiceDefinitionPolicy;

import example.layout.element.ElementPolicy;

/**
 * Policy for the MyNewsList element.
 */
public class MyNewsListElementPolicy extends ElementPolicy
    implements ModelTypeDescription
{
    private static final ExternalContentId MNL_SERVICE_DEFINITION_EID =
        new ExternalContentId(ServiceDefinitionPolicy.SERVICE_DEFINITION_DOMAIN_NAME + "mnl");

    @Override
    public void preCommitSelf()
        throws CMException
    {
        if (null == getServiceDefinitionId()) {
            setContentReference("serviceDefinition", "reference", MNL_SERVICE_DEFINITION_EID);
        }
    }

    /**
     * Get the service definition id for my news list.
     *
     * @return the service definition id of the MNL service
     * @throws CMException if something goes wrong
     */
    public String getServiceDefinitionId()
        throws CMException
    {
        String serviceName = null;

        PolicyCMServer cmServer = getCMServer();
        ContentId serviceDefinitionContentId = getContentReference("serviceDefinition", "reference");

        if (null != serviceDefinitionContentId) {
            ServiceDefinitionPolicy definitionPolicy = (ServiceDefinitionPolicy)
            cmServer.getPolicy(serviceDefinitionContentId);

            serviceName = definitionPolicy.getServiceName();
        }

        return serviceName;
    }
}
