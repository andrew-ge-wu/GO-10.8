/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HostConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class TwitterTwittsOuputIT {

    private static final Logger logger = Logger.getLogger(TwitterTwittsOuputIT.class.getName());

    private final String WIDGET_URL_RENDER = "/cmlink/twitter-empty-page/twitter.hashtags.element.1";
    private final String WIDGET_URL_NOCONTENT = "/cmlink/twitter-empty-page/twitter.hashtags.element.nocontent";

    private final int LOAD_TWITT_TIMEOUT = 20;
    private final int NUM_OF_RECORD_EXPECTED = 2;

    private WebClient webClient;
    private HostConfiguration localHostConfiguration;

    List<String> localTwitts = new ArrayList<String>();
    List<String> localTwittsXml = new ArrayList<String>();

    boolean connectionAlive;

    @Before
    public void setUp() {
        webClient = new WebClient();
        localHostConfiguration = new HostConfiguration();
        localHostConfiguration.setHost("localhost", Integer.valueOf(System.getProperty("jetty.port", "8080")));
        String conn = System.getProperty("skip.external.connetion.tests");
        connectionAlive = (!Boolean.valueOf(conn).booleanValue());

    }

    @After
    public void tearDown() {
        localHostConfiguration = null;
        if (webClient != null) {
            webClient.closeAllWindows();
        }
        webClient = null;
    }

    @Test
    public void testElement1() throws FailingHttpStatusCodeException, IOException, InterruptedException {
        if (connectionAlive) {
            assertFalse(isElementNotProperOnLocalSite(WIDGET_URL_RENDER));
        } else {
            logger.log(Level.WARNING, "Test skipped");
        }
    }

    @Test
    public void testElementNoContent() throws FailingHttpStatusCodeException, IOException, InterruptedException {
        if (connectionAlive) {
            assertTrue(isElementNotProperOnLocalSite(WIDGET_URL_NOCONTENT));
        } else {
            logger.log(Level.WARNING, "Test skipped");
        }
    }

    @Ignore
    private boolean isElementNotProperOnLocalSite(String path)
            throws FailingHttpStatusCodeException, IOException, InterruptedException {
        boolean isNoOutput = false;
        localTwitts.clear();
        localTwittsXml.clear();
        readTwittFromLocalSite(toHostURL("" + path));
        // Verify there is twitts
        isNoOutput = localTwitts.size() <= 0;
        // Verify each record is not empty
        for (String localTwitt : localTwitts) {
            isNoOutput = localTwitt.isEmpty();
        }

        // Verify the class of each record is in place
        for (String localTwittXml : localTwittsXml) {
            isNoOutput = (!localTwittXml.contains("twtr-user"));
            isNoOutput = (!localTwittXml.contains("twtr-timestamp"));
            isNoOutput = (!localTwittXml.contains("twtr-reply"));
            isNoOutput = (!localTwittXml.contains("twtr-rt"));
            isNoOutput = (!localTwittXml.contains("twtr-fav"));
        }

        if (isNoOutput) {
            logger.log(Level.WARNING, "Either there is no record or it's not rendered correctly");
        }
        return isNoOutput;
    }

    @Ignore
    private void readTwittFromLocalSite(String url) throws FailingHttpStatusCodeException, IOException, InterruptedException {
        HtmlPage page;
        WebRequest request = new WebRequest(new URL(url));
        page = webClient.getPage(request);
        // Get the twitter widget
        HtmlElement widget = page.getElementById("twtr-widget-1");

        logger.log(Level.INFO, "Loading Twitts for " + url);
        for (int timeout = LOAD_TWITT_TIMEOUT * 10; timeout > 0; timeout--) {
            if (widget.getElementsByAttribute("div", "class", "twtr-tweet").size() >= NUM_OF_RECORD_EXPECTED) {
                logger.log(Level.INFO, "Records found in " + (LOAD_TWITT_TIMEOUT - timeout / 10) + "secs");
                // Get the twitter text
                List<HtmlElement> localTwittsEle = widget.getElementsByAttribute("div", "class", "twtr-tweet");
                for (HtmlElement localTwittEle : localTwittsEle) {
                    localTwittsXml.add(localTwittEle.asXml());
                    List<HtmlElement> localTwittTextList = localTwittEle
                            .getElementsByAttribute("div", "class", "twtr-tweet-text");
                    HtmlElement localTwittTextEle = (HtmlElement) localTwittTextList.get(0);
                    localTwitts.add(localTwittTextEle.asText());
                }
                break;
            }
            synchronized (page) {
                page.wait(100);
            }
        }
    }

    @Ignore
    private String toHostURL(String path) {
        return localHostConfiguration.getHostURL() + path;
    }

}
