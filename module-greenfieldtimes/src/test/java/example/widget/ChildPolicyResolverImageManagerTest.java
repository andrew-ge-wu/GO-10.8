package example.widget;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.cm.policy.Policy;

public class ChildPolicyResolverImageManagerTest extends
        VideoPlayerBaseTestCase {
    private static final String IMAGE_POLICY_NAME = "image";
    private ChildPolicyResolverImageManager target;
    
    protected void setUp() throws Exception
    {
        super.setUp();
    
        target = new ChildPolicyResolverImageManager();
    }
    
    
    public void testResolvePolicyShouldReturnImageManagerPolicy() throws Exception
    {
        Policy childPolicy = mock(Policy.class);
        when(policy.getChildPolicy(IMAGE_POLICY_NAME)).thenReturn(childPolicy);
        Policy resolvedPolicy = target.resolvePolicy(policy);
        assertSame(childPolicy, resolvedPolicy);

    }
    
    public void testResolvePolicyShouldThrowRuntimeExceptionWhenCmExceptionIsThrown() throws Exception
    {
        when(policy.getChildPolicy(IMAGE_POLICY_NAME)).thenThrow(new CMException("cm exception"));
        try {
            target.resolvePolicy(policy);
            fail();
        } catch (CMRuntimeException e) {
          
        }
        verify(policy).getChildPolicy(IMAGE_POLICY_NAME);
    }
}
