package com.lld.eviction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe time-bound eviction policy implementation.
 * Evicts keys after a specified time period regardless of access frequency.
 */
public class TimeBoundEviction<K> implements EvictionPolicy<K> {
    private final ConcurrentHashMap<K, Long> keyTimeStampMap;
    private final Long expiryTime;

    public TimeBoundEviction(Long expiryTimeInMilliSeconds) {
        this.keyTimeStampMap = new ConcurrentHashMap<>();
        this.expiryTime = expiryTimeInMilliSeconds;
    }

    @Override
    public List<K> evict() {
        long currentExpiryTime = System.currentTimeMillis() - expiryTime;
        List<K> keysToEvict = new ArrayList<>();
        
        keyTimeStampMap.entrySet().removeIf(entry -> {
            if (entry.getValue() < currentExpiryTime) {
                keysToEvict.add(entry.getKey());
                return true;
            }
            return false;
        });
        
        return keysToEvict;
    }

    @Override
    public void manageKey(K key) {
        keyTimeStampMap.putIfAbsent(key, System.currentTimeMillis());
    }
}
