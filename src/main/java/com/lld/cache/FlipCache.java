package com.lld.cache;

import com.lld.config.Logger;
import com.lld.constants.Response;
import com.lld.datastore.DataSource;
import com.lld.eviction.EvictionPolicy;
import com.lld.metrics.Metrics;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe cache implementation with configurable eviction policies and metrics.
 * Uses ReadWriteLock for coordinating read/write operations and eviction.
 */
public class FlipCache<K, V> implements Cache<K, V> {
    private final EvictionPolicy<K> evictionPolicy;
    private final DataSource<K, V> dataSource;
    private final Metrics metrics;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

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
        lock.readLock().lock();
        try {
            // For access-based eviction policies (like LRU), we need to manage the key
            // But only if the key exists in the data source
            V val = this.dataSource.get(key);
            if (val != null) {
                this.evictionPolicy.manageKey(key);
            }
            
            // Run eviction after managing the key
            List<K> keysToEvict = evictionPolicy.evict();
            if (!keysToEvict.isEmpty()) {
                dataSource.delete(keysToEvict);
            }
            
            if (metrics != null) {
                if (val != null) {
                    metrics.recordHit();
                } else {
                    metrics.recordMiss();
                }
            }
            
            if (val == null) {
                Logger.info(Response.NOT_FOUND.getValue());
            }
            return val;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void putValue(K key, V value) {
        lock.writeLock().lock();
        try {
            // Always manage the key for put operations
            this.evictionPolicy.manageKey(key);
            this.dataSource.put(key, value);
            
            // Run eviction after adding the key
            List<K> keysToEvict = evictionPolicy.evict();
            if (!keysToEvict.isEmpty()) {
                dataSource.delete(keysToEvict);
            }
            
            Logger.info(Response.OK.getValue());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int getSize() {
        lock.readLock().lock();
        try {
            return this.dataSource.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get cache statistics if metrics are enabled.
     */
    public void printStats() {
        if (metrics != null) {
            metrics.printStats();
        } else {
            Logger.info("Metrics not enabled for this cache instance");
        }
    }

    private void executeEvictionPolicy(K key) {
        this.evictionPolicy.manageKey(key);
        List<K> keysToEvict = evictionPolicy.evict();
        if (!keysToEvict.isEmpty()) {
            dataSource.delete(keysToEvict);
        }
    }
}
