package com.lld.cache;

import com.lld.constants.Response;

public interface Cache<K, V> {
    V getValue(K key);
    void putValue(K key, V value);
    int getSize();
}
