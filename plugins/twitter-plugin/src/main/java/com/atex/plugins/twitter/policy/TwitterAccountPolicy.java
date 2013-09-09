/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter.policy;

import com.atex.plugins.twitter.TwitterAccount;
import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;

/*
 * A Twitter Account Policy 
 * @since 2.0.3
 */
public class TwitterAccountPolicy extends ContentPolicy implements TwitterAccount {

    public static final String CONSUMER_KEY = "consumerKey";
    public static final String CONSUMER_SECRET = "consumerSecret";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String ACCESS_TOKEN_SECRET = "accessTokenSecret";
    public static final String CAMEL_TWITTER_URI = "twitter://timeline/user?consumerKey=%s&"
                                                 + "accessToken=%s&"
                                                 + "consumerSecret=%s&"
                                                 + "accessTokenSecret=%s";
    
    public String getConsumerKey() throws CMException {
        return getValue(CONSUMER_KEY);
    }

    public String getConsumerSecret() throws CMException {
        return getValue(CONSUMER_SECRET);
    }

    public String getAccessToken() throws CMException {
        return getValue(ACCESS_TOKEN);
    }

    public String getAccessTokenSecret() throws CMException {
        return getValue(ACCESS_TOKEN_SECRET);
    }

    public String getCamelTwitterURI() throws CMException {
        return String.format(CAMEL_TWITTER_URI, 
                getConsumerKey(),
                getAccessToken(),
                getConsumerSecret(),
                getAccessTokenSecret());
    }

    String getValue(String name) throws CMException {
        return ((SingleValued) getChildPolicy(name)).getValue();
    }
}
