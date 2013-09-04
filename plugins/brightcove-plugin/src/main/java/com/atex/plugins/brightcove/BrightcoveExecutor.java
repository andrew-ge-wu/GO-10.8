/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove;

import java.io.IOException;
import java.net.MalformedURLException;

import com.brightcove.mediaapi.exceptions.BrightcoveException;
import com.polopoly.application.ConnectionPropertiesConfigurationException;
import com.polopoly.application.ConnectionPropertiesParseException;
import com.polopoly.application.IllegalApplicationStateException;
import com.polopoly.cm.app.imagemanager.ImageFormatException;
import com.polopoly.cm.app.imagemanager.ImageTooBigException;
import com.polopoly.cm.client.CMException;

public class BrightcoveExecutor {

    static Importer importer = new Importer();

    /**
     * @param args
     * @throws IllegalApplicationStateException 
     * @throws IOException 
     * @throws CMException 
     * @throws ConnectionPropertiesConfigurationException 
     * @throws MalformedURLException 
     * @throws ConnectionPropertiesParseException 
     * @throws IllegalArgumentException 
     * @throws ImageFormatException 
     * @throws BrightcoveException 
     * @throws ImageTooBigException 
     */
    public static void main(String[] args) throws IllegalArgumentException, ConnectionPropertiesParseException, MalformedURLException, ConnectionPropertiesConfigurationException, CMException, IOException, IllegalApplicationStateException, BrightcoveException, ImageFormatException, ImageTooBigException  {
        BrightcoveExecutor be = new BrightcoveExecutor();
        be.run(args);
     }

    static void setImporter(Importer importer)  {
        BrightcoveExecutor.importer = importer;
    }

    void run(String[] args) throws IllegalArgumentException, ConnectionPropertiesParseException, MalformedURLException, ConnectionPropertiesConfigurationException, CMException, IOException, IllegalApplicationStateException, BrightcoveException, ImageFormatException, ImageTooBigException{
        String url = System.getProperty("url", Importer.URL);
        String securityParent = System.getProperty("securityParent", Importer.SECURITY_PARENT);
        String user = System.getProperty("user", Importer.USER);
        long minutes = Long.parseLong(System.getProperty("minutes", String.valueOf(Importer.MINUTES)));
        int limit = Integer.parseInt(System.getProperty("limit", String.valueOf(Importer.LIMIT)));
        String siteId = System.getProperty("siteId", Importer.SITEID);
        importer.setUrl(url);
        importer.setSecurityParent(securityParent);
        importer.setUser(user);
        importer.setMinutes(minutes);
        importer.setLimit(limit);
        importer.setSiteId(siteId);
        importer.init();
        importer.run();
        importer.shutdown();
    }
}
