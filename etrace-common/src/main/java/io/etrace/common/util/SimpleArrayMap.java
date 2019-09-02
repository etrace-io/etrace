package io.etrace.common.util;

import java.io.Serializable;
import java.util.*;

public class SimpleArrayMap<K, V> extends AbstractMap<K, V>
    implements Map<K, V>, Cloneable, Serializable {

    private Entry<K, V>[] entries;

    private int size;

    public SimpleArrayMap(int capacity) {
        entries = new SimpleEntry[capacity];
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<Entry<K, V>>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                return new Iterator<Entry<K, V>>() {

                    int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < size;
                    }

                    @Override
                    public Entry<K, V> next() {
                        return entries[index++];
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("can not remove entry from this map");
                    }
                };
            }

            @Override
            public int size() {
                return size;
            }
        };
    }

    @Override
    public V put(K key, V value) {
        if (size >= entries.length) {
            return null;
        }
        entries[size++] = new SimpleEntry<>(key, value);
        return value;
    }
}

