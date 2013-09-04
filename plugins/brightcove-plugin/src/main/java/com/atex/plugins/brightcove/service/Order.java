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
 * order of the result
 */
public enum Order {
    
    DESC("com.atex.plugins.brightcove.label.desc"),
    ASC("com.atex.plugins.brightcove.label.asc");
    
    private final String label;
    
    Order(final String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }

}
