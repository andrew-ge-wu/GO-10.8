/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove.service;

/**
 * sort of the result
 */
public enum Sort {
    
    PUBLISH_DATE("com.atex.plugins.brightcove.label.publish.date"), 
    CREATION_DATE("com.atex.plugins.brightcove.label.creation.date"), 
    MODIFIED_DATE("com.atex.plugins.brightcove.label.modified.date"), 
    PLAYS_TOTAL("com.atex.plugins.brightcove.label.plays.total"), 
    PLAYS_TRAILING_WEEK("com.atex.plugins.brightcove.label.plays.trailing.week");
    
    private final String label;
    Sort(final String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
}
