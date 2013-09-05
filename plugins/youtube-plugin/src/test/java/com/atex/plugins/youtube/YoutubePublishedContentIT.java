/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.youtube;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;

/**
 * This Integration Test used to test whether youtube element successful insert
 * into page or not. If youtube element title exists in response content, means
 * test is success.
 */
public class YoutubePublishedContentIT {

    // SET 1
    @Test
    public void surfYoutubeEmptyPageTestTitle() throws Exception {
        testWebapp("/cmlink/youtube-empty-page", "Samsung Galaxy Nexus");
    }

    @Test
    public void surfYoutubeEmptyPageTestWidthAndHeight() throws Exception {
        testWebapp("/cmlink/youtube-empty-page", "<object width=\"273.0\" height=\"250.0\">", "width=\"273.0\" height=\"250.0\">");
    }

    @Test
    public void surfYoutubeEmptyPageTestSource() throws Exception {
        testWebapp("/cmlink/youtube-empty-page", "<embed src=\"http://www.youtube.com/v/t8PQYgw62vY?fs=1\"",
                "allowfullscreen=\"true\"");
    }

    @Test
    public void surfYoutubeEmptyPageTestMovieParam() throws Exception {
        testWebapp("/cmlink/youtube-empty-page",
                "<param name=\"movie\" value=\"http://www.youtube.com/v/t8PQYgw62vY?fs=1\"></param>");
    }

    // SET 2
    @Test
    public void surfYoutubeEmptyWidthTestTitle() throws Exception {
        testWebapp("/cmlink/youtube-empty-page/youtube-empty-width", "Atex DNA H264 16 9");
    }

    @Test
    public void surfYoutubeEmptyWidthTestWidthAndHeight() throws Exception {
        testWebapp("/cmlink/youtube-empty-page/youtube-empty-width", "<object width=\"797.0\" height=\"400.0\">",
                "width=\"797.0\" height=\"400.0\">");
    }

    @Test
    public void surfYoutubeEmptyWidthTestSource() throws Exception {
        testWebapp("/cmlink/youtube-empty-page/youtube-empty-width", "<embed src=\"http://www.youtube.com/v/g0_egjMb5Yo?fs=0\"",
                "allowfullscreen=\"false\"");
    }

    @Test
    public void surfYoutubeEmptyWidthTestMovieParam() throws Exception {
        testWebapp("/cmlink/youtube-empty-page/youtube-empty-width",
                "<param name=\"movie\" value=\"http://www.youtube.com/v/g0_egjMb5Yo?fs=0\"></param>");
    }

    // SET 3
    @Test
    public void surfYoutubeEmptyHeightTestTitle() throws Exception {
        testWebapp("/cmlink/youtube-empty-page/youtube-empty-height", "ASUS Padphone Promo");
    }

    @Test
    public void surfYoutubeEmptyHeightTestWidthAndHeight() throws Exception {
        testWebapp("/cmlink/youtube-empty-page/youtube-empty-height", "<object width=\"273.0\" height=\"210.0\">",
                "width=\"273.0\" height=\"210.0\">");
    }

    @Test
    public void surfYoutubeEmptyHeightTestSource() throws Exception {
        testWebapp("/cmlink/youtube-empty-page/youtube-empty-height", "<embed src=\"http://www.youtube.com/v/DE4NxPGyFjI?fs=1\"",
                "allowfullscreen=\"true\"");
    }

    @Test
    public void surfYoutubeEmptyHeightTestMovieParam() throws Exception {
        testWebapp("/cmlink/youtube-empty-page/youtube-empty-height",
                "<param name=\"movie\" value=\"http://www.youtube.com/v/DE4NxPGyFjI?fs=1\"></param>");
    }

    // SET 4
    @Test
    public void surfYoutubeEmptyWidthHeightTestTitle() throws Exception {
        testWebapp("/cmlink/youtube-empty-page/youtube-empty-width-and-height",
                "Marvel Avengers Assemble (2012) Watch the Official trailer | HD");
    }

    @Test
    public void surfYoutubeEmptyWidthHeightTestWidthAndHeight() throws Exception {
        testWebapp("/cmlink/youtube-empty-page/youtube-empty-width-and-height", "<object width=\"497.0\" height=\"378.0\">",
                "width=\"497.0\" height=\"378.0\">");
    }

    @Test
    public void surfYoutubeEmptyWidthHeightTestSource() throws Exception {
        testWebapp("/cmlink/youtube-empty-page/youtube-empty-width-and-height",
                "<embed src=\"http://www.youtube.com/v/NPoHPNeU9fc?fs=1\"", "allowfullscreen=\"true\"");
    }

    @Test
    public void surfYoutubeEmptyWidthHeightTestMovieParam() throws Exception {
        testWebapp("/cmlink/youtube-empty-page/youtube-empty-width-and-height",
                "<param name=\"movie\" value=\"http://www.youtube.com/v/NPoHPNeU9fc?fs=1\"></param>");
    }

    private void testWebapp(String contextPath, String... scanForThis) throws Exception {
        int port = Integer.valueOf(System.getProperty("jetty.port", "8080"));
        testWebApp(contextPath, "localhost", port, false, scanForThis);
    }

    protected void testWebApp(final String contextPath, final String host, final int port, final boolean optional, final String... scanForThese)
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
                    if (body.indexOf(scanString) < 0)
                        fail("Could not find " + scanString + " in page (body: '" + body + "')");
                }
            }
        } finally {
            get.releaseConnection();
        }
    }
}
