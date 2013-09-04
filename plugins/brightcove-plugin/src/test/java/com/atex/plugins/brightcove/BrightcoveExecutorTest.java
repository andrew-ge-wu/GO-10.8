/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.brightcove.mediaapi.exceptions.BrightcoveException;
import com.polopoly.application.ConnectionPropertiesConfigurationException;
import com.polopoly.application.ConnectionPropertiesParseException;
import com.polopoly.application.IllegalApplicationStateException;
import com.polopoly.cm.app.imagemanager.ImageFormatException;
import com.polopoly.cm.app.imagemanager.ImageTooBigException;
import com.polopoly.cm.client.CMException;

public class BrightcoveExecutorTest {

    BrightcoveExecutor target;

    @Mock
    Importer importer;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = spy(new BrightcoveExecutor());
        BrightcoveExecutor.setImporter(importer);
    }

    @Test
    public void testRun() throws IllegalArgumentException, ConnectionPropertiesParseException, 
                                 MalformedURLException, ConnectionPropertiesConfigurationException, 
                                 CMException, IOException, IllegalApplicationStateException, 
                                 BrightcoveException, ImageFormatException, ImageTooBigException {
        doNothing().when(target).run(null);
        BrightcoveExecutor.main(null);
    }

}
