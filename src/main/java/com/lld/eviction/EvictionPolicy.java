package com.lld.eviction;

import java.util.List;

public interface EvictionPolicy<K> {
    List<K> evict();
    void manageKey(K key);
}
