package example.widget;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mock;

import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMRuntimeException;

public class LinkResolverFromUrlTest
    extends VideoPlayerBaseTestCase
{
    private LinkResolverFromTextInput target;
    @Mock
    private SingleValuePolicy singleValuePolicy;

    protected void setUp() throws Exception
    {
        super.setUp();

        target = new LinkResolverFromTextInput();

    }

    public void testResolveLinkShouldReturnUrlFromTextInput() throws Exception
    {
        when(singleValuePolicy.getValue()).thenReturn(videoLink);
        String resolvedLink = target.resolveLink(oc, singleValuePolicy);

        assertEquals(videoLink, resolvedLink);
        verify(singleValuePolicy).getValue();

    }

    public void testResolveLinkShoudThrowRuntimeExceptionWhenCmExceptionIsThrownFromGetValue()
        throws Exception
 {
        when(singleValuePolicy.getValue()).thenThrow(new CMException("cm exception"));
        try {
            target.resolveLink(oc, singleValuePolicy);
            fail();
        }
        catch (CMRuntimeException e) {
            // Expected
        }
        verify(singleValuePolicy).getValue();
    }
    
    public void testResolveLinkShouldThrowUnresolvableLinkExceptionWhenEmptyUrl() throws Exception
    {
        when(singleValuePolicy.getValue()).thenReturn("");
        try {
            target.resolveLink(oc, singleValuePolicy);
            fail();
        }
        catch (UnresolvableLinkException e) {
            // Expected
        }
        verify(singleValuePolicy).getValue();
        
    }
    
    public void testResolveLinkShouldThrowUnresolvableLinkExceptionWhenNullUrl() throws Exception
    {
        when(singleValuePolicy.getValue()).thenReturn(null);
        try {
            target.resolveLink(oc, singleValuePolicy);
            fail();
        }
        catch (UnresolvableLinkException e) {
            // Expected
        }
        verify(singleValuePolicy).getValue();
        
    }
}
