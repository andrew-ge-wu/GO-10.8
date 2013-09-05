/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.deptdrpq;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;

/**
 * This Integration Test used to test whether articles displayed in List layout
 * element with Department Publishing Queue or not. If article title exists in
 * response content, means test is success.
 */
public class DeptPQHttpClientIT {

    @Test
    public void surfDeptMainPageArticles() throws Exception {
        testWebapp("/cmlink/dept-pq-main-page", "Main Site 1", "News 1", "Sports 1", "Sports 2", "Football 1", "Football 2");
    }

    @Test
    public void surfNewsPageArticles() throws Exception {
        testWebapp("/cmlink/dept-pq-main-page/dept-pq-news", "News 1");
    }

    @Test
    public void surfSportsPageArticles() throws Exception {
        testWebapp("/cmlink/dept-pq-main-page/dept-pq-sports", "Sports 1", "Sports 2", "Football 1", "Football 2");
        testWebappNotContain("/cmlink/dept-pq-main-page/dept-pq-sports", "Main Site 1", "News 1");
    }

    @Test
    public void surfFootballPageArticles() throws Exception {
        testWebapp("/cmlink/dept-pq-main-page/dept-pq-sports/dept-pq-football", "Football 1", "Football 2");
        testWebappNotContain("/cmlink/dept-pq-main-page/dept-pq-sports/dept-pq-football", "Main Site 1", "News 1", "Sports 1",
                "Sports 2");
    }

    private void testWebappNotContain(String contextPath, String... scanForThis) throws Exception {
        int port = Integer.valueOf(System.getProperty("jetty.port", "8080"));
        testWebApp(contextPath, "localhost", port, false, true, scanForThis);
    }

    private void testWebapp(String contextPath, String... scanForThis) throws Exception {
        int port = Integer.valueOf(System.getProperty("jetty.port", "8080"));
        testWebApp(contextPath, "localhost", port, false, false, scanForThis);
    }

    protected void testWebApp(final String contextPath, final String host, final int port, final boolean optional, boolean testNotContains, final String... scanForThese)
            throws Exception {
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

                fail("Wrong response code: expected 200, got " + responseCode + " (response message: '" + get.getStatusText()
                        + "', body: '" + body + "')");
            }

            Header contentLength = get.getResponseHeader("Content-Length");

            if (contentLength != null) {
                assertFalse("Content-Length was 0", Integer.parseInt(contentLength.getValue()) == 0);
            }

            Header contentType = get.getResponseHeader("Content-Type");
            assertFalse("Response did not contain Content-Type", contentType == null);

            String body = get.getResponseBodyAsString();

            if (scanForThese != null) {
                for (String scanString : scanForThese) {
                    if (testNotContains) {
                        if (body.indexOf(scanString) > 0)
                            fail(scanString + " should not in page (body: '" + body + "')");
                    } else {
                        if (body.indexOf(scanString) < 0)
                            fail("Could not find " + scanString + " in page (body: '" + body + "')");
                    }
                }
            }
        } finally {
            get.releaseConnection();
        }
    }
}
