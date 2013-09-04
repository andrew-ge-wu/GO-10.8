package example.membership;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

public class MembershipServletTest extends MembershipTestBase
{
    private static final String WRONG_ACTION_SERVLET_URI = SERVLET_URI + "/no_action";
    
    public void testWrongAction()
        throws Exception
    {
        jetty.start();
    
        HttpClient httpClient = new HttpClient();
        GetMethod method = new GetMethod(jetty.getURL(WRONG_ACTION_SERVLET_URI));
        method.addRequestHeader("Referer", REFERRER);
    
        int responseCode = httpClient.executeMethod(method);
    
        assertEquals(404, responseCode);
    }
}
