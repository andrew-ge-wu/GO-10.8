package example.content.image;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.app.imagemanager.ImageSet;
import com.polopoly.cm.client.CMException;

public class ImageSetAsMap implements Map<Object, Object> {

    private ImageSet imageSet;

    private final static Logger LOG = Logger.getLogger(ImageSetAsMap.class.getName());

    public ImageSetAsMap(ImageSet imageSet)
    {
        this.imageSet = imageSet;
    }
    
    public Object get(Object key) {
        try {
            return imageSet.getImage((String) key);
        } catch (CMException e) {
            LOG.log(Level.WARNING,
                    String.format("Failed getting derivative image, key is '%s'.", key),
                    e);
        } catch (IOException e) {
            LOG.log(Level.WARNING,
                    String.format("Failed getting derivative image, key is '%s'.", key),
                    e);
        }
        return null;
    }

    public void clear() {
        throw new UnsupportedOperationException("Only get is supported.");
    }

    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException("Only get is supported.");
    }

    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Only get is supported.");
    }

    public Set<java.util.Map.Entry<Object, Object>> entrySet() {
        throw new UnsupportedOperationException("Only get is supported.");
    }

    public boolean isEmpty() {
        throw new UnsupportedOperationException("Only get is supported.");
    }

    public Set<Object> keySet() {
        throw new UnsupportedOperationException("Only get is supported.");
    }

    public Object put(Object arg0, Object arg1) {
        throw new UnsupportedOperationException("Only get is supported.");
    }

    public void putAll(Map<? extends Object, ? extends Object> m)
    {
        throw new UnsupportedOperationException("Only get is supported.");
    }

    public Object remove(Object key) {
        throw new UnsupportedOperationException("Only get is supported.");
    }

    public int size() {
        throw new UnsupportedOperationException("Only get is supported.");
    }

    public Collection<Object> values() {
        throw new UnsupportedOperationException("Only get is supported.");
    }

}
