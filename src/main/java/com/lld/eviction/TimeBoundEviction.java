package com.lld.eviction;

import java.util.*;

/*
irrespective of how many times and when the key is called,
this eviction policy is for to delete key after
<expiryTime> milliseconds have passed after it was added to cache
*/
public class TimeBoundEviction<K> implements EvictionPolicy<K> {
    private final Map<K, Long> keyTimeStampMap;
    private final Long expiryTime;

    public TimeBoundEviction(Long expiryTimeInMilliSeconds) {
        this.keyTimeStampMap = new HashMap<>();
        this.expiryTime = expiryTimeInMilliSeconds;
    }

    @Override
    public List<K> evict() {
        long currentExpiryTime = System.currentTimeMillis() - expiryTime;
        List<K> keys = keyTimeStampMap.entrySet().stream()
                .filter(entry -> entry.getValue() < currentExpiryTime)
                .map(Map.Entry::getKey)
                .toList();

        keys.forEach(this.keyTimeStampMap::remove);
        return keys;
    }

    @Override
    public void manageKey(K key) {
        if (keyTimeStampMap.containsKey(key)) {
            return;
        }
        keyTimeStampMap.put(key, System.currentTimeMillis());
    }
}
