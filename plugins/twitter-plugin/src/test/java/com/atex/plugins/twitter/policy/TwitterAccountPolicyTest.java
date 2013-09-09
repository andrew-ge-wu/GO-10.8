/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter.policy;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

import com.polopoly.cm.client.CMException;

public class TwitterAccountPolicyTest {

    TwitterAccountPolicy target;

    @Before
    public void before() throws CMException {
        MockitoAnnotations.initMocks(this);
        target = spy(new TwitterAccountPolicy());
        doReturn("ck").when(target).getValue(TwitterAccountPolicy.CONSUMER_KEY);
        doReturn("ConsumerSecret").when(target).getValue(TwitterAccountPolicy.CONSUMER_SECRET);
        doReturn("AccessToken").when(target).getValue(TwitterAccountPolicy.ACCESS_TOKEN);
        doReturn("AccessTokenSecret").when(target).getValue(TwitterAccountPolicy.ACCESS_TOKEN_SECRET);
    }

    @Test
    public void testGetteres() throws CMException {
        assertEquals("ck", target.getConsumerKey());
        assertEquals("ConsumerSecret", target.getConsumerSecret());
        assertEquals("AccessToken", target.getAccessToken());
        assertEquals("AccessTokenSecret", target.getAccessTokenSecret());
    }

    @Test
    public void testGetCamelTwitterURI() throws CMException {
        assertEquals("twitter://timeline/user?"
                     + "consumerKey=ck&"
                     + "accessToken=AccessToken&"
                     + "consumerSecret=ConsumerSecret&"
                     + "accessTokenSecret=AccessTokenSecret", 
                     target.getCamelTwitterURI());
    }
}
