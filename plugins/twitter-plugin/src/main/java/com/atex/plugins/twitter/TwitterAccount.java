/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;

/*
 * @since 2.0.3
 */
public interface TwitterAccount extends Policy {

    String getConsumerKey() throws CMException;
    String getConsumerSecret() throws CMException;
    String getAccessToken() throws CMException;
    String getAccessTokenSecret() throws CMException;

    /*
     * Construct Camel Twitter URI dynamically
     * @return camel twitter uri
     * @throws CMException
     */
    String getCamelTwitterURI() throws CMException;
}
