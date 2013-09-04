package example.widget;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mock;

import com.polopoly.cm.app.policy.SelectableSubFieldPolicy;
import com.polopoly.cm.policy.Policy;

public class LinkResolverVideoTest
    extends VideoPlayerBaseTestCase
{
    private LinkResolverVideo target;
    @Mock
    private LinkResolver videoLinkResolverFromUrl;
    @Mock
    private LinkResolver videoLinkResolverFromFile;
    @Mock
    private SelectableSubFieldPolicy selectableSubFieldPolicy;
    private String videoLink = "resolvedLink";;

    protected void setUp() throws Exception
    {
        super.setUp();

        target = new LinkResolverVideo(videoLinkResolverFromUrl,
            videoLinkResolverFromFile);

    }

    public void testResolveLinkShouldSelectVideoResolverWhenUrlFieldIsSelectedInSelectableSubfield()
        throws Exception
    {

        String urlPolicyName = "url";

        when(policy.getChildPolicy("file")).thenReturn(selectableSubFieldPolicy);

        when(selectableSubFieldPolicy.getSelectedSubFieldName()).thenReturn(urlPolicyName);

        Policy urlPolicy = mock(Policy.class);
        when(selectableSubFieldPolicy.getChildPolicy(urlPolicyName)).thenReturn(urlPolicy);

        when(videoLinkResolverFromUrl.resolveLink(oc, urlPolicy)).thenReturn(videoLink);

        String resolvedLink = target.resolveLink(oc, policy);

        assertEquals(videoLink, resolvedLink);

    }

    public void testResolveLinktShouldSelectVideoResolverFromFileWhenFlashGroupIsSelectedInSelectableSubfield()
        throws Exception
    {

        String flashfilePolicyName = "flashfile";

        when(policy.getChildPolicy("file")).thenReturn(selectableSubFieldPolicy);

        when(selectableSubFieldPolicy.getSelectedSubFieldName()).thenReturn(flashfilePolicyName);

        Policy flashGroupPolicy = mock(Policy.class);
        when(selectableSubFieldPolicy.getChildPolicy(flashfilePolicyName)).thenReturn(flashGroupPolicy);

        when(videoLinkResolverFromFile.resolveLink(oc, flashGroupPolicy)).thenReturn(videoLink);

        String resolvedLink = target.resolveLink(oc, policy);

        assertEquals(videoLink, resolvedLink);

    }
    
    public void testResolveLinkShouldThrowExceptionWhenUnknownFieldIsSelected() 
        throws Exception
    {
        when(policy.getChildPolicy("file")).thenReturn(selectableSubFieldPolicy);

        when(selectableSubFieldPolicy.getSelectedSubFieldName()).thenReturn("nameThatDoesNotExist");

        try {
           target.resolveLink(oc, policy);
            fail();
        } catch(RuntimeException e){
            
        }
        
        verify(selectableSubFieldPolicy).getSelectedSubFieldName();
    }
}
