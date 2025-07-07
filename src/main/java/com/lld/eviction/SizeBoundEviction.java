package com.lld.eviction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe size-bound eviction policy implementation.
 * Evicts keys when the cache size exceeds the maximum capacity.
 * Uses FIFO (First In, First Out) strategy for eviction.
 */
public class SizeBoundEviction<K> implements EvictionPolicy<K> {
    private final ConcurrentHashMap<K, Long> keyInsertionTimeMap;
    private final int maxSize;
    private final AtomicLong insertionCounter = new AtomicLong(0);

    public SizeBoundEviction(int maxSize) {
        this.keyInsertionTimeMap = new ConcurrentHashMap<>();
        this.maxSize = maxSize;
    }

    @Override
    public List<K> evict() {
        if (keyInsertionTimeMap.size() <= maxSize) {
            return Collections.emptyList();
        }

        int itemsToEvict = keyInsertionTimeMap.size() - maxSize;
        List<K> keysToEvict = new ArrayList<>();
        
        // Find the oldest keys (FIFO strategy)
        keyInsertionTimeMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(itemsToEvict)
                .forEach(entry -> {
                    keysToEvict.add(entry.getKey());
                    keyInsertionTimeMap.remove(entry.getKey());
                });

        return keysToEvict;
    }

    @Override
    public void manageKey(K key) {
        keyInsertionTimeMap.putIfAbsent(key, insertionCounter.incrementAndGet());
    }
}