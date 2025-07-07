package com.lld.eviction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe LRU (Least Recently Used) eviction policy implementation.
 * Evicts the least recently used keys when the cache size exceeds the maximum capacity.
 */
public class LRUEviction<K> implements EvictionPolicy<K> {
    private final ConcurrentHashMap<K, Long> keyAccessTimeMap;
    private final AtomicLong accessCounter;
    private final int maxSize;

    public LRUEviction(int maxSize) {
        this.keyAccessTimeMap = new ConcurrentHashMap<>();
        this.accessCounter = new AtomicLong(0);
        this.maxSize = maxSize;
    }

    @Override
    public List<K> evict() {
        if (keyAccessTimeMap.size() <= maxSize) {
            return Collections.emptyList();
        }

        int itemsToEvict = keyAccessTimeMap.size() - maxSize;
        List<K> keysToEvict = new ArrayList<>();
        
        // Find the least recently used keys
        keyAccessTimeMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(itemsToEvict)
                .forEach(entry -> {
                    keysToEvict.add(entry.getKey());
                    keyAccessTimeMap.remove(entry.getKey());
                });

        return keysToEvict;
    }

    @Override
    public void manageKey(K key) {
        // Update access time for both new and existing keys
        keyAccessTimeMap.put(key, accessCounter.incrementAndGet());
    }
}