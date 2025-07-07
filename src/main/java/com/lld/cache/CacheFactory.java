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
 * Factory class for creating different cache configurations.
 * Implements Factory pattern for better code organization and flexibility.
 */
public class CacheFactory {
    
    /**
     * Create a cache with time-bound eviction policy.
     * @param expiryTimeMs expiry time in milliseconds
     * @return FlipCache instance
     */
    public static <K, V> Cache<K, V> createTimeBoundCache(long expiryTimeMs) {
        return new FlipCache<>(
            new TimeBoundEviction<>(expiryTimeMs),
            new InMemoryDataSource<>()
        );
    }
    
    /**
     * Create a cache with time-bound eviction policy and metrics enabled.
     * @param expiryTimeMs expiry time in milliseconds
     * @return FlipCache instance with metrics
     */
    public static <K, V> Cache<K, V> createTimeBoundCacheWithMetrics(long expiryTimeMs) {
        return new FlipCache<>(
            new TimeBoundEviction<>(expiryTimeMs),
            new InMemoryDataSource<>(),
            new HitMetric()
        );
    }
    
    /**
     * Create a cache with LRU eviction policy.
     * @param maxSize maximum number of entries
     * @return FlipCache instance
     */
    public static <K, V> Cache<K, V> createLRUCache(int maxSize) {
        return new FlipCache<>(
            new LRUEviction<>(maxSize),
            new InMemoryDataSource<>()
        );
    }
    
    /**
     * Create a cache with LRU eviction policy and metrics enabled.
     * @param maxSize maximum number of entries
     * @return FlipCache instance with metrics
     */
    public static <K, V> Cache<K, V> createLRUCacheWithMetrics(int maxSize) {
        return new FlipCache<>(
            new LRUEviction<>(maxSize),
            new InMemoryDataSource<>(),
            new HitMetric()
        );
    }
    
    /**
     * Create a cache with size-bound eviction policy.
     * @param maxSize maximum number of entries
     * @return FlipCache instance
     */
    public static <K, V> Cache<K, V> createSizeBoundCache(int maxSize) {
        return new FlipCache<>(
            new SizeBoundEviction<>(maxSize),
            new InMemoryDataSource<>()
        );
    }
    
    /**
     * Create a cache with size-bound eviction policy and metrics enabled.
     * @param maxSize maximum number of entries
     * @return FlipCache instance with metrics
     */
    public static <K, V> Cache<K, V> createSizeBoundCacheWithMetrics(int maxSize) {
        return new FlipCache<>(
            new SizeBoundEviction<>(maxSize),
            new InMemoryDataSource<>(),
            new HitMetric()
        );
    }
    
    /**
     * Create a custom cache with specified components.
     * @param evictionPolicy eviction policy implementation
     * @param dataSource data source implementation
     * @param metrics metrics implementation (can be null)
     * @return FlipCache instance
     */
    public static <K, V> Cache<K, V> createCustomCache(
            EvictionPolicy<K> evictionPolicy, 
            DataSource<K, V> dataSource,
            Metrics metrics) {
        return new FlipCache<>(evictionPolicy, dataSource, metrics);
    }
}