package com.lld.cache;

import com.lld.datastore.DataSource;
import com.lld.datastore.InMemoryDataSource;
import com.lld.eviction.EvictionPolicy;
import com.lld.eviction.LRUEviction;
import com.lld.eviction.SizeBoundEviction;
import com.lld.eviction.TimeBoundEviction;
import com.lld.metrics.HitMetric;
import com.lld.metrics.Metrics;

/**
 * Builder class for creating FlipCache instances with flexible configuration.
 * Implements Builder pattern for better readability and optional parameters.
 */
public class CacheBuilder<K, V> {
    private EvictionPolicy<K> evictionPolicy;
    private DataSource<K, V> dataSource;
    private Metrics metrics;
    
    public CacheBuilder() {
        // Default to in-memory data source
        this.dataSource = new InMemoryDataSource<>();
    }
    
    /**
     * Set time-bound eviction policy.
     * @param expiryTimeMs expiry time in milliseconds
     * @return this builder
     */
    public CacheBuilder<K, V> withTimeBoundEviction(long expiryTimeMs) {
        this.evictionPolicy = new TimeBoundEviction<>(expiryTimeMs);
        return this;
    }
    
    /**
     * Set LRU eviction policy.
     * @param maxSize maximum number of entries
     * @return this builder
     */
    public CacheBuilder<K, V> withLRUEviction(int maxSize) {
        this.evictionPolicy = new LRUEviction<>(maxSize);
        return this;
    }
    
    /**
     * Set size-bound eviction policy.
     * @param maxSize maximum number of entries
     * @return this builder
     */
    public CacheBuilder<K, V> withSizeBoundEviction(int maxSize) {
        this.evictionPolicy = new SizeBoundEviction<>(maxSize);
        return this;
    }
    
    /**
     * Set custom eviction policy.
     * @param evictionPolicy eviction policy implementation
     * @return this builder
     */
    public CacheBuilder<K, V> withEvictionPolicy(EvictionPolicy<K> evictionPolicy) {
        this.evictionPolicy = evictionPolicy;
        return this;
    }
    
    /**
     * Set custom data source.
     * @param dataSource data source implementation
     * @return this builder
     */
    public CacheBuilder<K, V> withDataSource(DataSource<K, V> dataSource) {
        this.dataSource = dataSource;
        return this;
    }
    
    /**
     * Enable hit metrics tracking.
     * @return this builder
     */
    public CacheBuilder<K, V> withMetrics() {
        this.metrics = new HitMetric();
        return this;
    }
    
    /**
     * Set custom metrics implementation.
     * @param metrics metrics implementation
     * @return this builder
     */
    public CacheBuilder<K, V> withMetrics(Metrics metrics) {
        this.metrics = metrics;
        return this;
    }
    
    /**
     * Build the cache instance.
     * @return FlipCache instance
     * @throws IllegalStateException if eviction policy is not set
     */
    public Cache<K, V> build() {
        if (evictionPolicy == null) {
            throw new IllegalStateException("Eviction policy must be set");
        }
        
        return new FlipCache<>(evictionPolicy, dataSource, metrics);
    }
}