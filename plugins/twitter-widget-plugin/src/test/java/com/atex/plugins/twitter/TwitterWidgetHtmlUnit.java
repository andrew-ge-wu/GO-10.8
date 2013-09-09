/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter;

import org.apache.commons.httpclient.HostConfiguration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.polopoly.cm.client.CMException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TwitterWidgetHtmlUnit {
    private static Logger LOG = Logger.getLogger(TwitterWidgetHtmlUnit.class.getName());
    private final String NO_EXT_CONN = "Test skipped - External hosts Connection fail";

    private final String WIDGET_ONE_URL = "/cmlink/twitter-empty-page/twitter.hashtags.element.1";

    private HostConfiguration hostConfiguration;
    private WebClient webClient;

    private boolean skipTest;

    @Before
    public void setUp() throws CMException, Exception {
        hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost("localhost", Integer.valueOf(System.getProperty("jetty.port", "8080")));
        webClient = new WebClient();
        skipTest = Boolean.parseBoolean(System.getProperty("skip.external.connetion.tests", "false"));
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
    public void testTwitterElementPresent() throws Exception {
        if (skipTest) {
            LOG.log(Level.WARNING, NO_EXT_CONN);
            return;
        }

        String url = hostConfiguration.getHostURL() + WIDGET_ONE_URL;
        HtmlPage page = webClient.getPage(url);
        String pageAsXml = page.asXml();

        assertTrue(pageAsXml.contains("div class=\"element twitter\""));
    }

    @Test
    public void testTwitterWidgetRender() throws Exception {
        if (skipTest) {
            LOG.log(Level.WARNING, NO_EXT_CONN);
            return;
        }

        String url = hostConfiguration.getHostURL() + WIDGET_ONE_URL;
        HtmlPage page = webClient.getPage(url);
        String pageAsXml = page.asXml();

        assertTrue(pageAsXml.contains("<script src=\"http://widgets.twimg.com/j/2/widget.js\">"));
        assertTrue(pageAsXml.contains("new TWTR.Widget({"));
        assertTrue(pageAsXml.contains("version: 2,"));
        assertTrue(pageAsXml.contains("type: 'search',"));
        assertTrue(pageAsXml.contains("interval: 5000,"));
        assertTrue(pageAsXml.contains("search: '#atex',"));
        assertTrue(pageAsXml.contains("title: 'Atex',"));
        assertTrue(pageAsXml.contains("subject: 'Atex News',"));
        assertTrue(pageAsXml.contains("rpp: '10',"));
        assertTrue(!pageAsXml.contains("width: 'auto',"));
        assertTrue(pageAsXml.contains("height: 'auto',"));
        assertTrue(pageAsXml.contains("theme: {"));
        assertTrue(pageAsXml.contains("shell: {"));
        assertTrue(pageAsXml.contains("background: '#8ec1da',"));
        assertTrue(pageAsXml.contains("color: '#ffffff'"));
        assertTrue(pageAsXml.contains("tweets: {"));
        assertTrue(pageAsXml.contains("background: '#ffffff',"));
        assertTrue(pageAsXml.contains("color: '#444444',"));
        assertTrue(pageAsXml.contains("links: '#1985b5'"));
        assertTrue(pageAsXml.contains("features: {"));
        assertTrue(pageAsXml.contains("scrollbar: true,"));
        assertTrue(pageAsXml.contains("loop: true,"));
        assertTrue(pageAsXml.contains("live: true,"));
        assertTrue(pageAsXml.contains("avatars: true,"));
        assertTrue(pageAsXml.contains("toptweets: true,"));
        assertTrue(pageAsXml.contains("behavior: 'default'"));
        assertTrue(pageAsXml.contains("}).render().start();"));
        assertTrue(pageAsXml.contains("<div class=\"twtr-widget\" id=\"twtr-widget-1\">"));
    }
}
