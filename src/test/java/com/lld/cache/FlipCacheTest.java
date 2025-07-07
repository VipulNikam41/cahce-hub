package com.lld.cache;

import com.lld.datastore.InMemoryDataSource;
import com.lld.eviction.LRUEviction;
import com.lld.eviction.SizeBoundEviction;
import com.lld.eviction.TimeBoundEviction;
import com.lld.metrics.HitMetric;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Test cases for FlipCache implementation covering functionality, thread safety, and performance.
 */
class FlipCacheTest {
    
    private FlipCache<String, String> cache;
    private HitMetric metrics;
    
    @BeforeEach
    void setUp() {
        metrics = new HitMetric();
        cache = new FlipCache<>(
            new LRUEviction<>(3),
            new InMemoryDataSource<>(),
            metrics
        );
    }
    
    @Test
    @DisplayName("Test basic put and get operations")
    void testBasicOperations() {
        // Test put operation
        cache.putValue("key1", "value1");
        assertEquals(1, cache.getSize());
        
        // Test get operation
        String value = cache.getValue("key1");
        assertEquals("value1", value);
        
        // Test get non-existent key
        String nonExistent = cache.getValue("nonexistent");
        assertNull(nonExistent);
    }
    
    @Test
    @DisplayName("Test LRU eviction policy")
    void testLRUEviction() {
        // Fill cache to capacity
        cache.putValue("key1", "value1");
        cache.putValue("key2", "value2");
        cache.putValue("key3", "value3");
        assertEquals(3, cache.getSize());
        
        // Access key1 to make it recently used
        cache.getValue("key1");
        
        // Add new key, should evict key2 (least recently used)
        cache.putValue("key4", "value4");
        assertEquals(3, cache.getSize());
        
        // key2 should be evicted
        assertNull(cache.getValue("key2"));
        // key1 should still exist
        assertEquals("value1", cache.getValue("key1"));
        // key4 should exist
        assertEquals("value4", cache.getValue("key4"));
    }
    
    @Test
    @DisplayName("Test metrics tracking")
    void testMetrics() {
        // Initially no hits or misses
        assertEquals(0.0, metrics.getHitRatio(), 0.001);
        
        // Add some data
        cache.putValue("key1", "value1");
        
        // Test hit
        cache.getValue("key1");
        
        // Test miss
        cache.getValue("nonexistent");
        
        // Hit ratio should be 0.5 (1 hit out of 2 accesses)
        assertEquals(0.5, metrics.getHitRatio(), 0.001);
    }
    
    @Test
    @DisplayName("Test thread safety with concurrent operations")
    void testThreadSafety() throws InterruptedException {
        final int numThreads = 10;
        final int operationsPerThread = 100;
        final CountDownLatch latch = new CountDownLatch(numThreads);
        final ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        // Create cache with larger capacity for concurrent test
        FlipCache<Integer, String> concurrentCache = new FlipCache<>(
            new LRUEviction<>(1000),
            new InMemoryDataSource<>(),
            new HitMetric()
        );
        
        // Submit concurrent tasks
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        int key = threadId * operationsPerThread + j;
                        concurrentCache.putValue(key, "value" + key);
                        concurrentCache.getValue(key);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all threads to complete
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        
        // Verify final state
        assertEquals(numThreads * operationsPerThread, concurrentCache.getSize());
    }
    
    @Test
    @DisplayName("Test cache size management")
    void testCacheSize() {
        assertEquals(0, cache.getSize());
        
        cache.putValue("key1", "value1");
        assertEquals(1, cache.getSize());
        
        cache.putValue("key2", "value2");
        assertEquals(2, cache.getSize());
        
        cache.putValue("key3", "value3");
        assertEquals(3, cache.getSize());
        
        // Adding fourth item should trigger eviction, size should remain 3
        cache.putValue("key4", "value4");
        assertEquals(3, cache.getSize());
    }
    
    @Test
    @DisplayName("Test time-bound eviction")
    void testTimeBoundEviction() throws InterruptedException {
        FlipCache<String, String> timeBoundCache = new FlipCache<>(
            new TimeBoundEviction<>(100L), // 100ms expiry
            new InMemoryDataSource<>()
        );
        
        timeBoundCache.putValue("key1", "value1");
        assertEquals("value1", timeBoundCache.getValue("key1"));
        
        // Wait for expiry
        Thread.sleep(150);
        
        // Trigger eviction by accessing cache
        timeBoundCache.getValue("key1");
        
        // Key should be evicted
        assertNull(timeBoundCache.getValue("key1"));
    }
    
    @Test
    @DisplayName("Test size-bound eviction")
    void testSizeBoundEviction() {
        FlipCache<String, String> sizeBoundCache = new FlipCache<>(
            new SizeBoundEviction<>(2),
            new InMemoryDataSource<>()
        );
        
        sizeBoundCache.putValue("key1", "value1");
        sizeBoundCache.putValue("key2", "value2");
        assertEquals(2, sizeBoundCache.getSize());
        
        // Adding third item should trigger eviction
        sizeBoundCache.putValue("key3", "value3");
        assertEquals(2, sizeBoundCache.getSize());
        
        // key1 should be evicted (FIFO)
        assertNull(sizeBoundCache.getValue("key1"));
        assertEquals("value2", sizeBoundCache.getValue("key2"));
        assertEquals("value3", sizeBoundCache.getValue("key3"));
    }
}