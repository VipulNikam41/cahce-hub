package com.lld.datastore;

import java.util.List;

public interface DataSource<K, V> {
    V get(K key);
    void put(K key, V value);
    void delete(K key);
    void delete(List<K> key);
    int size();
}
