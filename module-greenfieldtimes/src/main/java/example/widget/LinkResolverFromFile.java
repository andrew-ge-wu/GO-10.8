package example.widget;

import com.polopoly.cm.app.policy.FilePolicy;
import com.polopoly.cm.app.util.URLBuilder;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.OrchidContext;

/**
 * Resolve a link from a file manager.
 */
public class LinkResolverFromFile
    implements LinkResolver
{
    public String resolveLink(OrchidContext oc, Policy policy)
        throws UnresolvableLinkException
    {
        String link;
        try {
            FilePolicy filePolicy = (FilePolicy) policy;

            String fileName = filePolicy.getFileName();

            String fullFilePath = filePolicy.getFullFilePath();

            if (fileName != null && fullFilePath != null) {

                link = URLBuilder.getFileUrl(filePolicy.getContentId(),
                                             fullFilePath, oc);
            } else {
                throw new UnresolvableLinkException("Unable to resolve file");
            }

        }
        catch (CMException e) {
            throw new CMRuntimeException(e);
        }
        catch (OrchidException e) {
            throw new RuntimeException(e);
        }

        return link;
    }
}
