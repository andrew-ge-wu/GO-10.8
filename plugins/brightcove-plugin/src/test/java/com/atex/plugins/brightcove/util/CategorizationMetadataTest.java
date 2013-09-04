/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove.util;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class CategorizationMetadataTest {
    
    @Test
    public void equalsByIgnoreCase() {
        CategorizationMetadata categorizationMetadata = new CategorizationMetadata(null, "Person");
        CategorizationMetadata another = new CategorizationMetadata(null, "person");
        Assert.assertTrue(categorizationMetadata.equals(another));
    }

    @Test
    public void equalsByOtherObject() {
        CategorizationMetadata categorizationMetadata = new CategorizationMetadata(null, "Person");
        Assert.assertFalse(categorizationMetadata.equals(new Integer(1)));
    }

    @Test
    public void hashCodeIsSameAsStringHashCode() {
        CategorizationMetadata categorizationMetadata = new CategorizationMetadata(null, "Person");
        Assert.assertEquals("Person".hashCode(), categorizationMetadata.hashCode());
    }
}
