package example.util;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * ThreadLocal class wrapping the actual ThreadLocal
 * instance in order to help the VM garbage collection
 * reclaiming these objects.
 *
 * The values themselves are stored using {@link WeakReference}
 * and hard references are kept in a separate map.
 */
public class DisposableThreadLocal<T>
{
    private ThreadLocal<WeakReference<T>> threadLocal = new ThreadLocal<WeakReference<T>>();
    private Map<Long, T> hardReferences = new HashMap<Long, T>();

    public void set(T value)
    {
        internalSet(value);
    }

    public T get()
    {
        WeakReference<T> weakRef = (WeakReference<T>) threadLocal.get();

        if (weakRef != null) {
            return weakRef.get();
        }

        return setInitialValue();
    }

    protected T initialValue()
    {
        return null;
    }

    public void remove()
    {
        threadLocal.remove();
    }

    public void destroy()
    {
        hardReferences = null;
        threadLocal = null;
    }

    private T setInitialValue()
    {
        T value = initialValue();

        if (value != null) {
            internalSet(value);
        }

        return value;
    }

    private void internalSet(T value)
    {
        threadLocal.set(new WeakReference<T>(value));

        synchronized(hardReferences) {
            hardReferences.put(Thread.currentThread().getId(), value);
        }
    }
}
