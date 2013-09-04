package example.util;

import java.util.concurrent.ConcurrentHashMap;

public class ContextImplBase
    implements Context
{
    protected final ConcurrentHashMap<String, Object> map =
        new ConcurrentHashMap<String, Object>();
    
    public Object get(String key)
    {
        return map.get(key);
    }

    public void put(String key, Object val)
    {
        map.put(key, val);
    }
    
}
