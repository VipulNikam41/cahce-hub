package com.lld.datastore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryDataSource<K, V> implements DataSource<K, V> {
    private final Map<K, V> cache;

    public InMemoryDataSource() {
        this.cache = new HashMap<>();
    }

    @Override
    public V get(K key) {
        if (!this.cache.containsKey(key)) {
            return null;
        }
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
        keys.forEach(this::delete);
    }

    @Override
    public int size() {
        return this.cache.size();
    }
}
