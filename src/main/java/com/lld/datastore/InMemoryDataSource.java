package com.lld.datastore;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory data source implementation using ConcurrentHashMap.
 * This implementation handles concurrent access safely without explicit synchronization.
 */
public class InMemoryDataSource<K, V> implements DataSource<K, V> {
    private final ConcurrentHashMap<K, V> cache;

    public InMemoryDataSource() {
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public V get(K key) {
        return this.cache.get(key);
    }

    @Override
    public void put(K key, V value) {
        this.cache.put(key, value);
    }

    @Override
    public void delete(K key) {
        this.cache.remove(key);
    }

    @Override
    public void delete(List<K> keys) {
        keys.forEach(this.cache::remove);
    }

    @Override
    public int size() {
        return this.cache.size();
    }
}
