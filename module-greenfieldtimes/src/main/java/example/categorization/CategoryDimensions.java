package example.categorization;

public enum CategoryDimensions {
    TAG("Tag"),
    PERSON("Person"),
    COMPANY("Company"),
    ORGANIZATION("Organization"),
    LOCATION("Location");
    
    private static final String BASE = "department.categorydimension.tag.";
    
    private final String _name;
    
    private CategoryDimensions(String name) {
        _name = name;
    }
    
    public String externalId() {
        return BASE + _name;
    }
}