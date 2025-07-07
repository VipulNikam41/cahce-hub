package com.lld.eviction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Test cases for eviction policies.
 */
class EvictionPolicyTest {
    
    @Test
    @DisplayName("Test TimeBoundEviction basic functionality")
    void testTimeBoundEviction() {
        TimeBoundEviction<String> eviction = new TimeBoundEviction<>(100L);
        
        // Add keys
        eviction.manageKey("key1");
        eviction.manageKey("key2");
        
        // Initially no eviction
        List<String> evicted = eviction.evict();
        assertTrue(evicted.isEmpty());
        
        // Wait for keys to expire
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Now keys should be evicted
        evicted = eviction.evict();
        assertEquals(2, evicted.size());
        assertTrue(evicted.contains("key1"));
        assertTrue(evicted.contains("key2"));
    }
    
    @Test
    @DisplayName("Test TimeBoundEviction duplicate key handling")
    void testTimeBoundEvictionDuplicateKeys() {
        TimeBoundEviction<String> eviction = new TimeBoundEviction<>(100L);
        
        // Add same key multiple times
        eviction.manageKey("key1");
        eviction.manageKey("key1");
        eviction.manageKey("key1");
        
        // Wait for expiry
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Should only evict once
        List<String> evicted = eviction.evict();
        assertEquals(1, evicted.size());
        assertEquals("key1", evicted.get(0));
    }
    
    @Test
    @DisplayName("Test LRUEviction basic functionality")
    void testLRUEviction() {
        LRUEviction<String> eviction = new LRUEviction<>(2);
        
        // Add keys within capacity
        eviction.manageKey("key1");
        eviction.manageKey("key2");
        
        // No eviction needed
        List<String> evicted = eviction.evict();
        assertTrue(evicted.isEmpty());
        
        // Add third key, should trigger eviction
        eviction.manageKey("key3");
        evicted = eviction.evict();
        assertEquals(1, evicted.size());
        assertEquals("key1", evicted.get(0)); // Least recently used
    }
    
    @Test
    @DisplayName("Test LRUEviction access pattern")
    void testLRUEvictionAccessPattern() {
        LRUEviction<String> eviction = new LRUEviction<>(2);
        
        // Add keys
        eviction.manageKey("key1");
        eviction.manageKey("key2");
        
        // Access key1 again (making it more recently used)
        eviction.manageKey("key1");
        
        // Add third key
        eviction.manageKey("key3");
        
        // key2 should be evicted (least recently used)
        List<String> evicted = eviction.evict();
        assertEquals(1, evicted.size());
        assertEquals("key2", evicted.get(0));
    }
    
    @Test
    @DisplayName("Test SizeBoundEviction basic functionality")
    void testSizeBoundEviction() {
        SizeBoundEviction<String> eviction = new SizeBoundEviction<>(2);
        
        // Add keys within capacity
        eviction.manageKey("key1");
        eviction.manageKey("key2");
        
        // No eviction needed
        List<String> evicted = eviction.evict();
        assertTrue(evicted.isEmpty());
        
        // Add third key, should trigger eviction
        eviction.manageKey("key3");
        evicted = eviction.evict();
        assertEquals(1, evicted.size());
        assertEquals("key1", evicted.get(0)); // First in, first out
    }
    
    @Test
    @DisplayName("Test SizeBoundEviction FIFO behavior")
    void testSizeBoundEvictionFIFO() {
        SizeBoundEviction<String> eviction = new SizeBoundEviction<>(3);
        
        // Add keys
        eviction.manageKey("key1");
        eviction.manageKey("key2");
        eviction.manageKey("key3");
        
        // Add two more keys
        eviction.manageKey("key4");
        eviction.manageKey("key5");
        
        // Should evict first two keys (FIFO)
        List<String> evicted = eviction.evict();
        assertEquals(2, evicted.size());
        assertTrue(evicted.contains("key1"));
        assertTrue(evicted.contains("key2"));
    }
    
    @Test
    @DisplayName("Test SizeBoundEviction duplicate key handling")
    void testSizeBoundEvictionDuplicateKeys() {
        SizeBoundEviction<String> eviction = new SizeBoundEviction<>(2);
        
        // Add same key multiple times
        eviction.manageKey("key1");
        eviction.manageKey("key1");
        eviction.manageKey("key2");
        
        // Should only track unique keys
        List<String> evicted = eviction.evict();
        assertTrue(evicted.isEmpty()); // Within capacity
        
        // Add third unique key
        eviction.manageKey("key3");
        evicted = eviction.evict();
        assertEquals(1, evicted.size());
    }
}