package com.lld.cache;

import com.lld.datastore.InMemoryDataSource;
import com.lld.eviction.LRUEviction;
import com.lld.eviction.SizeBoundEviction;
import com.lld.eviction.TimeBoundEviction;
import com.lld.metrics.HitMetric;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for CacheFactory and CacheBuilder implementations.
 */
class CacheFactoryAndBuilderTest {
    
    @Test
    @DisplayName("Test CacheFactory time-bound cache creation")
    void testFactoryTimeBoundCache() {
        Cache<String, String> cache = CacheFactory.createTimeBoundCache(5000L);
        assertNotNull(cache);
        
        cache.putValue("key1", "value1");
        assertEquals("value1", cache.getValue("key1"));
        assertEquals(1, cache.getSize());
    }
    
    @Test
    @DisplayName("Test CacheFactory LRU cache creation")
    void testFactoryLRUCache() {
        Cache<String, String> cache = CacheFactory.createLRUCache(3);
        assertNotNull(cache);
        
        cache.putValue("key1", "value1");
        cache.putValue("key2", "value2");
        cache.putValue("key3", "value3");
        assertEquals(3, cache.getSize());
        
        // Adding fourth item should trigger eviction
        cache.putValue("key4", "value4");
        assertEquals(3, cache.getSize());
    }
    
    @Test
    @DisplayName("Test CacheFactory size-bound cache creation")
    void testFactorySizeBoundCache() {
        Cache<String, String> cache = CacheFactory.createSizeBoundCache(2);
        assertNotNull(cache);
        
        cache.putValue("key1", "value1");
        cache.putValue("key2", "value2");
        assertEquals(2, cache.getSize());
        
        // Adding third item should trigger eviction
        cache.putValue("key3", "value3");
        assertEquals(2, cache.getSize());
    }
    
    @Test
    @DisplayName("Test CacheFactory cache with metrics")
    void testFactoryCacheWithMetrics() {
        Cache<String, String> cache = CacheFactory.createLRUCacheWithMetrics(3);
        assertNotNull(cache);
        
        cache.putValue("key1", "value1");
        cache.getValue("key1"); // hit
        cache.getValue("nonexistent"); // miss
        
        // Verify metrics are working (cache should be FlipCache instance)
        assertTrue(cache instanceof FlipCache);
        ((FlipCache<String, String>) cache).printStats();
    }
    
    @Test
    @DisplayName("Test CacheBuilder with LRU eviction")
    void testBuilderLRUEviction() {
        Cache<String, String> cache = new CacheBuilder<String, String>()
                .withLRUEviction(2)
                .build();
        
        assertNotNull(cache);
        
        cache.putValue("key1", "value1");
        cache.putValue("key2", "value2");
        assertEquals(2, cache.getSize());
        
        // Adding third item should trigger eviction
        cache.putValue("key3", "value3");
        assertEquals(2, cache.getSize());
    }
    
    @Test
    @DisplayName("Test CacheBuilder with time-bound eviction")
    void testBuilderTimeBoundEviction() {
        Cache<String, String> cache = new CacheBuilder<String, String>()
                .withTimeBoundEviction(5000L)
                .build();
        
        assertNotNull(cache);
        
        cache.putValue("key1", "value1");
        assertEquals("value1", cache.getValue("key1"));
    }
    
    @Test
    @DisplayName("Test CacheBuilder with size-bound eviction")
    void testBuilderSizeBoundEviction() {
        Cache<String, String> cache = new CacheBuilder<String, String>()
                .withSizeBoundEviction(3)
                .build();
        
        assertNotNull(cache);
        
        cache.putValue("key1", "value1");
        cache.putValue("key2", "value2");
        cache.putValue("key3", "value3");
        assertEquals(3, cache.getSize());
        
        // Adding fourth item should trigger eviction
        cache.putValue("key4", "value4");
        assertEquals(3, cache.getSize());
    }
    
    @Test
    @DisplayName("Test CacheBuilder with metrics")
    void testBuilderWithMetrics() {
        Cache<String, String> cache = new CacheBuilder<String, String>()
                .withLRUEviction(3)
                .withMetrics()
                .build();
        
        assertNotNull(cache);
        
        cache.putValue("key1", "value1");
        cache.getValue("key1"); // hit
        cache.getValue("nonexistent"); // miss
        
        // Verify metrics are working
        assertTrue(cache instanceof FlipCache);
        ((FlipCache<String, String>) cache).printStats();
    }
    
    @Test
    @DisplayName("Test CacheBuilder with custom components")
    void testBuilderWithCustomComponents() {
        Cache<String, String> cache = new CacheBuilder<String, String>()
                .withEvictionPolicy(new LRUEviction<>(5))
                .withDataSource(new InMemoryDataSource<>())
                .withMetrics(new HitMetric())
                .build();
        
        assertNotNull(cache);
        
        cache.putValue("key1", "value1");
        assertEquals("value1", cache.getValue("key1"));
    }
    
    @Test
    @DisplayName("Test CacheBuilder requires eviction policy")
    void testBuilderRequiresEvictionPolicy() {
        CacheBuilder<String, String> builder = new CacheBuilder<>();
        
        // Should throw IllegalStateException when no eviction policy is set
        assertThrows(IllegalStateException.class, builder::build);
    }
}