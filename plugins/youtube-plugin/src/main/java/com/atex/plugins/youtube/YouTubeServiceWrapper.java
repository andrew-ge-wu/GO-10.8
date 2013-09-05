/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.youtube;

import com.google.gdata.client.youtube.YouTubeQuery;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.util.ServiceException;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.util.MessageUtil;
import com.polopoly.util.LocaleUtil;

import java.io.IOException;
import java.net.URL;

public class YouTubeServiceWrapper {
    /**
     * The name of the server hosting the YouTube GDATA feeds
     */
    public static final String YOUTUBE_GDATA_SERVER = "http://gdata.youtube.com";

    /**
     * The URL of the "Videos" feed
     */
    public static final String VIDEOS_FEED = YOUTUBE_GDATA_SERVER + "/feeds/api/videos";

    // Sort orders
    public static final int RELEVANCE = 1;
    public static final int VIEW_COUNT = 2;
    public static final int UPDATED = 3;
    public static final int RATING = 4;

    private boolean offline = false;

    /**
     * Searches the VIDEOS_FEED for search terms and print each resulting
     * VideoEntry.
     * 
     * @param service
     *            a YouTubeService object.
     * @throws ServiceException
     *             If the service is unable to handle the request.
     * @throws java.io.IOException
     *             error sending request or reading the feed.
     */
    @SuppressWarnings("deprecation")
    protected VideoFeed searchFeed(YouTubeService service, String searchTerms, int order)
            throws IOException, ServiceException, OrchidException {
        YouTubeQuery query = new YouTubeQuery(new URL(VIDEOS_FEED));

        switch (order) {
        case VIEW_COUNT:
            query.setOrderBy(YouTubeQuery.OrderBy.VIEW_COUNT);
            break;
        case UPDATED:
            query.setOrderBy(YouTubeQuery.OrderBy.UPDATED);
            break;
        case RATING:
            query.setOrderBy(YouTubeQuery.OrderBy.RATING);
            break;
        case RELEVANCE:
        default:
            query.setOrderBy(YouTubeQuery.OrderBy.RELEVANCE);
            break;
        }

        // TODO implement paging
        query.setMaxResults(10);

        // do not exclude restricted content from the search results
        // (by default, it is excluded)
        query.setSafeSearch(YouTubeQuery.SafeSearch.NONE);
        query.setFullTextQuery(searchTerms);

        VideoFeed videoFeed = service.query(query, VideoFeed.class);
        return videoFeed;

    }

    public VideoFeed search(String searchTerms, int order, OrchidContext oc) throws OrchidException {
        VideoFeed results = new VideoFeed();
        if (searchTerms == null || searchTerms.length() == 0) {
            return results;
        }

        YouTubeService service = new YouTubeService("gdataSample-YouTube-1");
        try {
            results = searchFeed(service, searchTerms, order);
            offline = false;
        } catch (IOException e) {
            offline = true;
            MessageUtil.addErrorMessage(oc, LocaleUtil.format("p.service.youtube.unabletoconnect", oc.getMessageBundle()));
        } catch (ServiceException e) {
            offline = true;
            MessageUtil.addErrorMessage(oc, LocaleUtil.format("p.service.youtube.unabletosearch", oc.getMessageBundle()));
        }

        return results;
    }

    public boolean isOffline() {
        return offline;
    }
}
