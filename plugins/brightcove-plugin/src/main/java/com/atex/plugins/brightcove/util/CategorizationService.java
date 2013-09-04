/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

import com.brightcove.commons.catalog.objects.CustomField;
import com.brightcove.commons.catalog.objects.Video;
import com.polopoly.cm.app.search.categorization.Categorization;
import com.polopoly.cm.app.search.categorization.CategorizationBuilder;
import com.polopoly.cm.app.search.categorization.CategorizationPolicyUtil;
import com.polopoly.cm.app.search.categorization.CategorizationProvider;
import com.polopoly.cm.app.search.categorization.Category;
import com.polopoly.cm.app.search.categorization.CategoryBuilder;
import com.polopoly.cm.app.search.categorization.CategoryType;
import com.polopoly.cm.app.search.categorization.Dimension;
import com.polopoly.cm.app.search.categorization.DimensionBuilder;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;

/**
 * @since 1.1.0
 */
public class CategorizationService {

    private static final String FILE_NAME = "categorization-mapping.properties";
    private static final Map<String, String> CATEGORIES = new HashMap<String, String>(407);
    public static final List<String> CUSTOM_FIELDS = Arrays.asList("atexperson", "atexcompany", "atexorganization", "atexlocation");
    public static final List<CategorizationMetadata> METADATA_FIELDS = 
            Collections.unmodifiableList(
                        Arrays.asList(
                                new CategorizationMetadata("department.categorydimension.tag.Person", "Person"),
                                new CategorizationMetadata("department.categorydimension.tag.Company", "Company"),
                                new CategorizationMetadata("department.categorydimension.tag.Organisation", "Organization"),
                                new CategorizationMetadata("department.categorydimension.tag.Location", "Location")
                        )
                    );

    private static CategorizationService INSTANCE;

    private CategorizationService() {
        InputStream is = getClass().getClassLoader().getResourceAsStream(FILE_NAME);
        Properties properties = new Properties();
        try {
            properties.load(is);
            for(Entry<Object, Object> entry: properties.entrySet()) {
                CATEGORIES.put(entry.getKey().toString(), entry.getValue().toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file '" + FILE_NAME + "' from classpath, please make sure it is exist.", e);
        }
    }

    public Categorization toCategorization(List<String> tags, List<CustomField> customFields, Map<String, String> mappings) {
        if(tags == null) {
            tags = Collections.emptyList();
        }
        if(customFields == null) {
            customFields = Collections.emptyList();
        }
        mappings = safe(mappings);
        tags = clearTags(tags, customFields);
        
        if(tags.isEmpty() && customFields.isEmpty()) {
        	return Categorization.EMPTY_CATEGORIZATION;
        }

        List<Category> treeCategory = new ArrayList<Category>();
        List<Category> tagCategory = new ArrayList<Category>();
        for (int i = 0; i < tags.size(); i++) {
            String tag = tags.get(i);

            String externalId = CATEGORIES.get(tag.toLowerCase());
            if(externalId != null) {
                treeCategory.add(new CategoryBuilder().withName(tag).id(externalId).build());
            } else {
                tagCategory.add(new CategoryBuilder().withName(tag).id(tag).build());
            }
        }

        Map<String, List<Category>> listOfCategories = new HashMap<String, List<Category>>();
        // get list of brightcove custom fields via Entry's key
        for(Entry<String, String> entry: mappings.entrySet()) {
            listOfCategories.put(entry.getKey(), new ArrayList<Category>());
        }
        // add brightcove custom field values into category, if found
        for(CustomField customField: customFields) {
            List<Category> categories = listOfCategories.get(customField.getName());
            // found custom field name that match
            if(categories != null) {
                String[] values = customField.getValue().split(",");
                for(String value: values) {
                    // add metadata
                    categories.add(
                            new CategoryBuilder()
                                    .withName(value)
                                    .id(value)
                                    .build());
                }
            }
        }
        Dimension treeDimension = new DimensionBuilder().withType(CategoryType.TREE)
                .withName("Subject")
                .id("department.categorydimension.subject")
                .withCategories(treeCategory)
                .build();
        Dimension tagDimension = new DimensionBuilder().withType(CategoryType.TAG)
                .withName("Tag")
                .id("department.categorydimension.tag.Tag")
                .withCategories(tagCategory)
                .build();
        List<Dimension> dimensions = new ArrayList<Dimension>(4);
        for(Entry<String, List<Category>> entry: listOfCategories.entrySet()) {
            String polopolyMetadata = mappings.get(entry.getKey());
            int index = METADATA_FIELDS.indexOf(new CategorizationMetadata(null, polopolyMetadata));
            if(index != -1) {
                CategorizationMetadata categorizationMetadata = METADATA_FIELDS.get(index);
                Dimension dimension = new DimensionBuilder().withType(CategoryType.TAG)
                        .withName(categorizationMetadata.getName())
                        .id(categorizationMetadata.getId())
                        .withCategories(entry.getValue())
                        .build();
                dimensions.add(dimension);
            }
        }

        dimensions.add(treeDimension);
        dimensions.add(tagDimension);
        CategorizationBuilder builder = new CategorizationBuilder();
        return builder.withDimensions(dimensions.toArray(new Dimension[1])).build();
    }

    public List<String> getCategorizationTags(Policy contentPolicy) throws CMException {
        List<String> tagList = new ArrayList<String>();
        CategorizationProvider catProvider = CategorizationPolicyUtil.getCategorizationProviderFromPolicy(contentPolicy);
        Categorization cat = catProvider.getCategorization();
        Map<String, Dimension> dimMap = cat.getDimensionMap();
        for(Dimension dim : dimMap.values()){
            for (Category category :dim.getCategories()) {
                tagList.add(category.getName());
            }
        }
        return tagList;
    }
    
    
    /**
     * set the policy's categorization to Brightcove Video object.
     * using <code>mapping</code> to do custom mapping to Brightcove custom fields
     * @param policy where the categorization take from
     * @param video where the categorization set to
     * @param mapping used to map Polopoly categorization
     *  to Brightcove custom Fields
     * @throws CMException when failed to get categorization from policy
     */
    public void setCategorizationToBrightcoveVideo(Policy policy, Video video, Map<String, String> mapping) throws CMException {
        mapping = safe(mapping);
        Categorization categorization = CategorizationPolicyUtil.getCategorizationProviderFromPolicy(policy).getCategorization();
        Map<String, Dimension> map = categorization.getDimensionMap();
        List<CustomField> customFields = new ArrayList<CustomField>();
        for(Entry<String, String> entry: mapping.entrySet()) {
            for(Entry<String, Dimension> mapEntry: map.entrySet()) {
                Dimension dimension = mapEntry.getValue();
                if(dimension.getName().equalsIgnoreCase(entry.getValue())) {
                    customFields.add(categoryToBrightcoveCustomField(entry.getKey(), dimension.getCategories()));
                }
            }
        }
        video.setCustomFields(customFields);
    }
    
    /**
     * create Brightcove custom field object via <code>name</code>
     * and set of <code>categories</code>
     * @param name of the Brightcove custom field
     * @param categories Polopoly categories set
     * @return Brightcove custom field
     */
    protected CustomField categoryToBrightcoveCustomField(String name, Set<Category> categories) {
        StringBuilder builder = new StringBuilder();
        for(Category category: categories) {
            builder.append(category.getName()).append(',');
        }
        return new CustomField(name, builder.toString());
    }
    
    private <E, T> Map<E, T> safe(Map<E, T> map) {
        if(map == null) {
            return Collections.emptyMap();
        } else {
            return map;
        }
    }
    
    /**
     * When push video to Brightcove, the Polopoly Tags are set 
     * to Brightcove video's tags, and custom fields.
     * When pull from Brightcove, we need remove some of the tags that 
     * will duplicate into Polopoly's Tag field.
     * @param tags from brightcove
     * @param customFields of the brightcove video
     * @return clean list of tags
     */
    public List<String> clearTags(List<String> tags, List<CustomField> customFields) {
        final List<String> customTags = new ArrayList<String>();
        for(CustomField customField: customFields) {
            String[] values = customField.getValue().split(",");
            for(String value: values) {
                customTags.add(value.toLowerCase().trim());
            }
        }
        List<String> result = new ArrayList<String>();
        for(String tag: tags) {
            if(customTags.indexOf(tag.toLowerCase().trim()) == -1) {
                result.add(tag);
            }
        }
        return result;
    }

    public static final CategorizationService getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new CategorizationService();
        }
        return INSTANCE;
    }
}
