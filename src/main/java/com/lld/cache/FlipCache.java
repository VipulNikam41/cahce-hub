package com.lld.cache;

import com.lld.config.Logger;
import com.lld.constants.Response;
import com.lld.datastore.DataSource;
import com.lld.eviction.EvictionPolicy;
import com.lld.metrics.Metrics;

import java.util.List;

public class FlipCache<K, V> implements Cache<K, V> {
    private final EvictionPolicy<K> evictionPolicy;
    private final DataSource<K, V> dataSource;
    private final Metrics metrics;

    public FlipCache(EvictionPolicy<K> evictionPolicy, DataSource<K, V> dataSource) {
        this(evictionPolicy, dataSource, null);
    }

    public FlipCache(EvictionPolicy<K> evictionPolicy, DataSource<K, V> dataSource, Metrics metrics) {
        this.evictionPolicy = evictionPolicy;
        this.dataSource = dataSource;
        this.metrics = metrics;
    }

    @Override
    public V getValue(K key) {
        this.executeEvictionPolicy(key);
        V val = this.dataSource.get(key);
        if (val == null) {
            Logger.info(Response.NOT_FOUND.getValue());
        }
        return val;
    }

    @Override
    public void putValue(K key, V value) {
        this.executeEvictionPolicy(key);
        this.dataSource.put(key, value);
        Logger.info(Response.OK.getValue());
    }

    @Override
    public int getSize() {
        return this.dataSource.size();
    }

    private void executeEvictionPolicy(K key) {
        this.evictionPolicy.manageKey(key);
        dataSource.delete(evictionPolicy.evict());
    }
}
