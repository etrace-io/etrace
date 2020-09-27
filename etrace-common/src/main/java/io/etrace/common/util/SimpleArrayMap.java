/*
 * Copyright 2019 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

