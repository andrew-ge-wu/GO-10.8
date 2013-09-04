/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove;

import java.util.List;

import com.polopoly.model.ModelTypeDescription;

/**
 *
 */
public interface VideoListModelTypeDescription extends ModelTypeDescription {
    
    List<BrightcoveVideoPolicy> getVideos();

}
