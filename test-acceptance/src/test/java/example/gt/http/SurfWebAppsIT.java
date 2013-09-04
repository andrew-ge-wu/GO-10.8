package example.gt.http;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;

public class SurfWebAppsIT
{
    @Test
    public void surfDispatcher()
        throws Exception
    {
        testWebapp("/");
    }

    @Test
    public void surfGui()
        throws Exception
    {
        testWebapp("/polopoly");
    }

    @Test
    public void surfSolr()
        throws Exception
    {
        testWebapp("/solr");
    }

    @Test
    public void surfSolrIndexer()
        throws Exception
    {
        testWebapp("/solr-indexer");
    }

    @Test
    public void surfStatisticsServer()
        throws Exception
    {
        testWebapp("/statistics-server");
    }

    @Test
    public void surfIntegrationServer()
        throws Exception
    {
        testWebapp("/integration-server");
    }

    @Test
    public void surfManagement()
        throws Exception
    {
        testWebapp("/management");
    }

    @Test
    public void surfModeration()
        throws Exception
    {
        testWebapp("/moderation");
    }

    private void testWebapp(String contextPath)
        throws Exception
    {
        testWebApp(contextPath, "localhost", 8080, false, null);
    }

    protected void testWebApp(final String contextPath,
                              final String host,
                              final int port,
                              final boolean optional,
                              final String scanForThis)
        throws Exception
    {
        HttpClient httpClient = new HttpClient();
        HttpMethod get = new GetMethod(contextPath);
        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost(host, port);

        try {
            int responseCode = httpClient.executeMethod(hostConfiguration, get);

            if (optional && responseCode == 404) {
                return;
            }

            if (responseCode != 200) {
                String body = get.getResponseBodyAsString();

                fail("Wrong response code: expected 200, got " + responseCode
                    + " (response message: '" + get.getStatusText()
                    + "', body: '" + body + "')");
            }

            Header contentLength = get.getResponseHeader("Content-Length");

            if (contentLength != null) {
                assertFalse("Content-Length was 0", Integer.parseInt(contentLength.getValue()) == 0);
            }

            Header contentType = get.getResponseHeader("Content-Type");
            assertFalse("Response did not contain Content-Type", contentType == null);

            String body = get.getResponseBodyAsString();

            if (scanForThis != null && body.indexOf(scanForThis) < 0) {
                fail("Could not find " + scanForThis + " in page (body: '" + body + "')");
            }
        } finally {
            get.releaseConnection();
        }
    }
}
