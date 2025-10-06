package zmq.util;

import org.checkerframework.framework.qual.EnsuresQualifier;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.checker.mustcall.qual.Owning;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

// custom implementation of a collection mapping multiple values, tailored for use in the lib.
// this class is definitely not thread-safe, and allows only one mapping per key-value
// aka if the same value is correlated to a new key, the old mapping is removed.
public final class MultiMap<K extends Comparable<? super K>, V>
{
    // sorts entries according to the natural order of the values
    private final class EntryComparator implements Comparator<Entry<V, K>>
    {
        @Pure
        @Override
        public int compare(Entry<V, K> first, Entry<V, K> second)
        {
            return first.getValue().compareTo(second.getValue());
        }
    }

    private final Comparator<? super Entry<V, K>> comparator = new EntryComparator();

    // where all the data will be put
    private final Map<K, List<V>> data;
    // inverse mapping to speed-up the process
    private final Map<V, K> inverse;

    @Impure
    public MultiMap()
    {
        data = new HashMap<>();
        inverse = new HashMap<>();
    }

    @EnsuresQualifier(expression="this", qualifier=org.checkerframework.checker.collectionownership.qual.OwningCollectionBottom.class)
    @Impure
    public void clear()
    {
        data.clear();
        inverse.clear();
    }

    @Impure
    public Collection<Entry<V, K>> entries()
    {
        List<Entry<V, K>> list = new ArrayList<>(inverse.entrySet());
        list.sort(comparator);
        return list;
    }

    @SideEffectFree
    @Deprecated
    public Collection<V> values()
    {
        return inverse.keySet();
    }

    @EnsuresQualifier(expression="this", qualifier=org.checkerframework.checker.collectionownership.qual.OwningCollectionBottom.class)
    @Pure
    public boolean contains(@Owning V value)
    {
        return inverse.containsKey(value);
    }

    @Pure
    public K key(@Owning V value)
    {
        return inverse.get(value);
    }

    @Pure
    public V find(@Owning V copy)
    {
        K key = inverse.get(copy);
        if (key != null) {
            List<V> list = data.get(key);
            return list.get(list.indexOf(copy));
        }
        return null;
    }

    @Pure
    public boolean hasValues(@Owning K key)
    {
        List<V> list = data.get(key);
        if (list == null) {
            return false;
        }
        return !list.isEmpty();
    }

    @EnsuresQualifier(expression="this", qualifier=org.checkerframework.checker.collectionownership.qual.OwningCollectionBottom.class)
    @Pure
    public boolean isEmpty()
    {
        return inverse.isEmpty();
    }

    @Impure
    private List<V> getValues(@Owning K key)
    {
        return data.computeIfAbsent(key, k -> new ArrayList<>());
    }

    @Impure
    public boolean insert(@Owning K key, @Owning V value)
    {
        K old = inverse.get(value);
        if (old != null) {
            boolean rc = removeData(old, value);
            assert rc;
        }
        boolean inserted = getValues(key).add(value);
        if (inserted) {
            inverse.put(value, key);
        }
        else {
            inverse.remove(value);
        }
        return inserted;
    }

    @Impure
    public Collection<V> remove(@Owning K key)
    {
        List<V> removed = data.remove(key);
        if (removed != null) {
            for (V val : removed) {
                inverse.remove(val);
            }
        }
        return removed;
    }

    @Impure
    public boolean remove(@Owning V value)
    {
        K key = inverse.remove(value);
        if (key != null) {
            return removeData(key, value);
        }
        return false;
    }

    @Impure
    public boolean remove(@Owning K key, @Owning V value)
    {
        boolean removed = removeData(key, value);
        if (removed) {
            inverse.remove(value);
        }
        return removed;
    }

    @Impure
    private boolean removeData(@Owning K key, @Owning V value)
    {
        boolean removed = false;
        List<V> list = data.get(key);
        if (list != null) {
            removed = list.remove(value);
            if (list.isEmpty()) {
                data.remove(key);
            }
        }
        return removed;
    }

    @SideEffectFree
    @Override
    public String toString()
    {
        return data.toString();
    }
}
