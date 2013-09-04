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
 * type of the search
 */
public enum Type {
    
    NAME("com.atex.plugins.brightcove.label.name.and.description"),
    TAG("com.atex.plugins.brightcove.label.tags"),
    REFERENCE_ID("com.atex.plugins.brightcove.label.reference.id");
    
    private final String label;
    
    
    Type(final String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
}
