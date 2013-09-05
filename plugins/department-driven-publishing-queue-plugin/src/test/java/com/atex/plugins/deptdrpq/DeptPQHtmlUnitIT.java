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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.httpclient.HostConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class DeptPQHtmlUnitIT {

    private HostConfiguration hostConfiguration;
    private WebClient webClient;

    @Before
    public void setUp() {
        hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost("localhost", Integer.valueOf(System.getProperty("jetty.port", "8080")));
        webClient = new WebClient();
        webClient.setJavaScriptEnabled(false);
    }

    @After
    public void tearDown() {
        hostConfiguration = null;

        if (webClient != null) {
            webClient.closeAllWindows();
        }
        webClient = null;
    }

    @Test
    public void testMainPageArticles() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        String url = hostConfiguration.getHostURL() + "/cmlink/dept-pq-main-page";

        HtmlPage page = webClient.getPage(url);
        String pageAsXml = page.asXml();

        assertTrue(pageAsXml.contains("Main Site 1"));
        assertTrue(pageAsXml.contains("News 1"));
        assertTrue(pageAsXml.contains("Sports 1"));
        assertTrue(pageAsXml.contains("Sports 2"));
        assertTrue(pageAsXml.contains("Football 1"));
        assertTrue(pageAsXml.contains("Football 2"));
    }

    @Test
    public void testNewsPageArticles() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        String url = hostConfiguration.getHostURL() + "/cmlink/dept-pq-main-page/dept-pq-news";

        HtmlPage page = webClient.getPage(url);
        String pageAsXml = page.asXml();

        assertTrue(pageAsXml.contains("News 1"));
    }

    @Test
    public void testSportsPageArticles() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        String url = hostConfiguration.getHostURL() + "/cmlink/dept-pq-main-page/dept-pq-sports";

        HtmlPage page = webClient.getPage(url);
        String pageAsXml = page.asXml();

        assertTrue(pageAsXml.contains("Sports 1"));
        assertTrue(pageAsXml.contains("Sports 2"));
        assertTrue(pageAsXml.contains("Football 1"));
        assertTrue(pageAsXml.contains("Football 2"));

        assertFalse(pageAsXml.contains("Main Site 1"));
        assertFalse(pageAsXml.contains("News 1"));
    }

    @Test
    public void testFootballPageArticles() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        String url = hostConfiguration.getHostURL() + "/cmlink/dept-pq-main-page/dept-pq-sports/dept-pq-football";

        HtmlPage page = webClient.getPage(url);
        String pageAsXml = page.asXml();

        assertTrue(pageAsXml.contains("Football 1"));
        assertTrue(pageAsXml.contains("Football 2"));

        assertFalse(pageAsXml.contains("Main Site 1"));
        assertFalse(pageAsXml.contains("News 1"));
        assertFalse(pageAsXml.contains("Sports 1"));
        assertFalse(pageAsXml.contains("Sports 2"));
    }
}
