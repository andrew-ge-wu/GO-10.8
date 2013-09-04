package example.util;

public interface Context
{
    Object get(String key);
    
    void put(String key, Object val);
}
