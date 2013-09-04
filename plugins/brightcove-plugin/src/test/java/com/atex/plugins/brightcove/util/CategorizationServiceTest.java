/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.*;

import com.brightcove.commons.catalog.objects.CustomField;
import com.brightcove.commons.catalog.objects.Video;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import com.polopoly.cm.app.search.categorization.Categorization;
import com.polopoly.cm.app.search.categorization.CategorizationBuilder;
import com.polopoly.cm.app.search.categorization.CategorizationProvider;
import com.polopoly.cm.app.search.categorization.Category;
import com.polopoly.cm.app.search.categorization.CategoryBuilder;
import com.polopoly.cm.app.search.categorization.CategoryType;
import com.polopoly.cm.app.search.categorization.Dimension;
import com.polopoly.cm.app.search.categorization.DimensionBuilder;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;

/**
 *
 */
public class CategorizationServiceTest {
    
    List<String> type = Arrays.asList(
            "department.categorydimension.subject",
            "department.categorydimension.tag.Person",
            "department.categorydimension.tag.Company",
            "department.categorydimension.tag.Organisation",
            "department.categorydimension.tag.Tag",
            "department.categorydimension.tag.Location");
    //                                subject   subject    tag        tag
    List<String> tags = Arrays.asList("health", "strike", "sample", "Obama");
    Map<String, String> defaultMappings = new HashMap<String, String>();
    
    @Before
    public void before() {
        defaultMappings.put("atexperson", "Person");
        defaultMappings.put("atexcompany", "Company");
        defaultMappings.put("atexorganization", "Organization");
        defaultMappings.put("atexlocation", "Location");
    }

    @Test
    public void ifTagNotFoundInSubjectThenWillMapToTag() {
        Categorization c = CategorizationService.getInstance().toCategorization(tags, null, Collections.<String, String> emptyMap());
        Map<String, Dimension> map = c.getDimensionMap();
        // two dimensions
        assertEquals("Does not have two types", 2, map.size());
        for(Entry<String, Dimension> entry: map.entrySet()) {
            // type must within "type" list
            assertTrue("Does not contain the type", type.contains(entry.getKey()));
        }
    }
    
    @Test
    public void shouldMapCustomFieldsToMetadata() {
    	List<CustomField> customFields = new ArrayList<CustomField>();
    	customFields.add(new CustomField("atexperson", "Barak Obama"));
    	customFields.add(new CustomField("atexcompany", "Atex"));
    	customFields.add(new CustomField("atexorganization", "WWF"));
    	customFields.add(new CustomField("atexlocation", "Malaysia"));
    	customFields.add(new CustomField("atexotherfield", "Other Field"));
    	Categorization c = CategorizationService.getInstance().toCategorization(null, customFields, defaultMappings);
    	Map<String, Dimension> map = c.getDimensionMap();
    	// person
    	Dimension personDimension = map.get("department.categorydimension.tag.Person");
    	assertEquals(1, personDimension.getCategories().size());
    	Category personCategory = personDimension.getCategories().iterator().next();
    	assertEquals("Barak Obama", personCategory.getId());
    	assertEquals("Barak Obama", personCategory.getName());
    	// company
    	Dimension companyDimension = map.get("department.categorydimension.tag.Company");
    	assertEquals(1, companyDimension.getCategories().size());
    	Category companyCategory = companyDimension.getCategories().iterator().next();
    	assertEquals("Atex", companyCategory.getId());
    	assertEquals("Atex", companyCategory.getName());
    	// organization
    	Dimension organizationDimension = map.get("department.categorydimension.tag.Organisation");
    	assertEquals(1, organizationDimension.getCategories().size());
    	Category organizationCategory = organizationDimension.getCategories().iterator().next();
    	assertEquals("WWF", organizationCategory.getId());
    	assertEquals("WWF", organizationCategory.getName());
    	// location
    	Dimension locationDimension = map.get("department.categorydimension.tag.Location");
    	assertEquals(1, locationDimension.getCategories().size());
    	Category locationCategory = locationDimension.getCategories().iterator().next();
    	assertEquals("Malaysia", locationCategory.getId());
    	assertEquals("Malaysia", locationCategory.getName());
    }

    @Test
    public void emptyOrNullTagsShouldReturnEmptyCategorization() {
        assertEquals(Categorization.EMPTY_CATEGORIZATION, CategorizationService.getInstance().toCategorization(null, null, null));
        assertEquals(Categorization.EMPTY_CATEGORIZATION, CategorizationService.getInstance().toCategorization(Collections.<String>emptyList(), Collections.<CustomField>emptyList(), null));
    }
    
    @Test
    public void mapPolopolyCategorizationToBrightcoveVideo() throws CMException {
        MockCategorizationPolicy policy = mock(MockCategorizationPolicy.class);
        Category category = new CategoryBuilder().withName("Barak Obama").id("Barak Obama").build();
        Dimension dimension = new DimensionBuilder().withType(CategoryType.TAG).withName("Person").id("department.categorydimension.tag.Person").withCategories(category).build();
        Categorization categorization = new CategorizationBuilder().withDimensions(dimension).build();
        when(policy.getCategorization()).thenReturn(categorization);
        Video video = new Video();
        Map<String, String> mapping = new HashMap<String, String>();
        // map Polopoly "Person" to brightcove custom field "atexperson"
        mapping.put("atexperson", "Person");
        CategorizationService.getInstance().setCategorizationToBrightcoveVideo(policy, video, mapping);
        List<CustomField> customFields = video.getCustomFields();
        assertEquals(1, customFields.size());
        CustomField customField = customFields.get(0);
        // verify Brightcove custom field name and value
        assertEquals("atexperson", customField.getName());
        assertEquals("Barak Obama,", customField.getValue());
    }
    
    @Test
    public void shouldAbleClearTags() {
        // tags from normal Brightcove tag field
        List<String> tags = Arrays.asList("Barak obama", "Atex", "Nike", "Coffee");
        // tags from brightcove custom fields
        List<CustomField> customFields = Arrays.asList(new CustomField("atexperson", "Barak Obama"), new CustomField("atexcompany", "Atex, Nike"));
        List<String> result = CategorizationService.getInstance().clearTags(tags, customFields);
        assertEquals(1, result.size());
        assertEquals("Coffee", result.get(0));
    }
    
    private interface MockCategorizationPolicy extends CategorizationProvider, Policy {}
}
