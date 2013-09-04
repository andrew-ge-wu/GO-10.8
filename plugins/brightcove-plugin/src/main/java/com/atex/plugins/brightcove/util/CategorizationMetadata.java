/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove.util;


/**
 * A POJO to hold metadata about Polopoly's categorization
 * @since 2.0.0
 */
public class CategorizationMetadata {
    
    private String id;
    private String name;

    public CategorizationMetadata(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CategorizationMetadata) {
            CategorizationMetadata other = (CategorizationMetadata) obj;
            return name.equalsIgnoreCase(other.name);
        } else {
            return false;
        }
    }
}
