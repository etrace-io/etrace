package io.etrace.stream.biz.app.event;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class TypeNameCheck {
    private final Map<String, Set<Integer>> hashCache = new ConcurrentHashMap<>(516);

    public boolean isTooMany(String appId, String type, String name) {
        Set<Integer> hashSets = getHashSet(appId);
        if (type == null && name == null) {
            return false;
        }
        int hash = (type + name).hashCode();
        boolean contains = hashSets.contains(hash);
        if (!contains && hashSets.size() >= 2000) {
            return true;
        }
        if (!contains) {
            hashSets.add(hash);

        }
        return false;
    }

    private Set<Integer> getHashSet(String appId) {
        Set<Integer> hashSets = hashCache.get(appId);
        if (hashSets != null) {
            return hashSets;
        }
        synchronized (hashCache) {
            hashSets = hashCache.get(appId);
            if (hashSets != null) {
                return hashSets;
            }
            hashSets = new ConcurrentSkipListSet<>();
            hashCache.put(appId, hashSets);
            return hashSets;
        }
    }
}
