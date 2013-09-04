package example.widget;

import com.polopoly.cm.app.policy.SelectableSubFieldPolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.orchid.context.OrchidContext;

/**
 * Resolve a link to a video.
 */
public class LinkResolverVideo
    implements LinkResolver
{
    private static final String FILESELECT_POLICY_NAME = "flashfile";
    private static final String URL_POLICY_NAME = "url";
    private final LinkResolver videoLinkResolverFromUrl;
    private final LinkResolver videoLinkResolverFromFile;

    public LinkResolverVideo(LinkResolver videoLinkResolverFromUrl,
        LinkResolver videoLinkResolverFromFile)
    {
        this.videoLinkResolverFromUrl = videoLinkResolverFromUrl;
        this.videoLinkResolverFromFile = videoLinkResolverFromFile;
    }

    public String resolveLink(OrchidContext oc, Policy policy)
        throws UnresolvableLinkException
    {
        String resolvedLink = "";

        try {
            SelectableSubFieldPolicy selectableSubFieldPolicy = (SelectableSubFieldPolicy) policy
                .getChildPolicy("file");

            String selectedField = selectableSubFieldPolicy
                .getSelectedSubFieldName();

            if (URL_POLICY_NAME.equals(selectedField)) {
                Policy urlPolicy = selectableSubFieldPolicy
                    .getChildPolicy(URL_POLICY_NAME);

                resolvedLink = videoLinkResolverFromUrl.resolveLink(oc, urlPolicy);
            }
            else if (FILESELECT_POLICY_NAME.equals(selectedField)) {

                Policy filePolicy = selectableSubFieldPolicy
                    .getChildPolicy(FILESELECT_POLICY_NAME);

                resolvedLink = videoLinkResolverFromFile
                    .resolveLink(oc, filePolicy);

            }
            else {
                throw new RuntimeException(
                    "Unable to resolve link from field: '" + selectedField+"'");
            }

        }
        catch (CMException e) {
            throw new CMRuntimeException(e);
        }

        return resolvedLink;
    }
}
