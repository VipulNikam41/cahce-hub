package com.lld;

import com.lld.cache.Cache;
import com.lld.cache.CacheBuilder;
import com.lld.cache.CacheFactory;
import com.lld.cache.FlipCache;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Demonstration class showing advanced cache features including
 * thread safety, different eviction policies, and metrics.
 */
public class CacheDemo {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== FlipCache Advanced Demonstration ===\n");
        
        // Demonstrate different eviction policies
        demonstrateEvictionPolicies();
        
        // Demonstrate thread safety
        demonstrateThreadSafety();
        
        // Demonstrate metrics
        demonstrateMetrics();
        
        System.out.println("=== Demonstration Complete ===");
    }
    
    private static void demonstrateEvictionPolicies() {
        System.out.println("1. Eviction Policies Demonstration");
        
        // LRU Cache
        System.out.println("\n--- LRU Cache (size=3) ---");
        Cache<String, String> lruCache = CacheFactory.createLRUCache(3);
        
        lruCache.putValue("A", "Value A");
        lruCache.putValue("B", "Value B"); 
        lruCache.putValue("C", "Value C");
        System.out.println("Added A, B, C. Size: " + lruCache.getSize());
        
        lruCache.getValue("A"); // Make A recently used
        lruCache.putValue("D", "Value D"); // Should evict B
        
        System.out.println("After accessing A and adding D:");
        System.out.println("A exists: " + (lruCache.getValue("A") != null));
        System.out.println("B exists: " + (lruCache.getValue("B") != null)); // Should be false
        System.out.println("C exists: " + (lruCache.getValue("C") != null));
        System.out.println("D exists: " + (lruCache.getValue("D") != null));
        
        // Size-bound Cache
        System.out.println("\n--- Size-bound Cache (size=2) ---");
        Cache<String, String> sizeBoundCache = CacheFactory.createSizeBoundCache(2);
        
        sizeBoundCache.putValue("X", "Value X");
        sizeBoundCache.putValue("Y", "Value Y");
        sizeBoundCache.putValue("Z", "Value Z"); // Should evict X (FIFO)
        
        System.out.println("Added X, Y, Z. Size: " + sizeBoundCache.getSize());
        System.out.println("X exists: " + (sizeBoundCache.getValue("X") != null)); // Should be false
        System.out.println("Y exists: " + (sizeBoundCache.getValue("Y") != null));
        System.out.println("Z exists: " + (sizeBoundCache.getValue("Z") != null));
        
        // Time-bound Cache
        System.out.println("\n--- Time-bound Cache (500ms expiry) ---");
        Cache<String, String> timeBoundCache = CacheFactory.createTimeBoundCache(500L);
        
        timeBoundCache.putValue("T1", "Temporary Value");
        System.out.println("Added T1, exists: " + (timeBoundCache.getValue("T1") != null));
        
        try {
            Thread.sleep(600); // Wait for expiry
            timeBoundCache.getValue("T1"); // Trigger eviction
            System.out.println("After 600ms, T1 exists: " + (timeBoundCache.getValue("T1") != null));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static void demonstrateThreadSafety() throws InterruptedException {
        System.out.println("\n2. Thread Safety Demonstration");
        
        Cache<Integer, String> cache = new CacheBuilder<Integer, String>()
                .withLRUEviction(1000)
                .withMetrics()
                .build();
        
        int numThreads = 5;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        System.out.println("Starting " + numThreads + " threads, " + operationsPerThread + " operations each...");
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    int key = threadId * operationsPerThread + j;
                    cache.putValue(key, "Thread-" + threadId + "-Value-" + j);
                    cache.getValue(key); // Access the value to test thread safety
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        long endTime = System.currentTimeMillis();
        
        System.out.println("Completed in " + (endTime - startTime) + "ms");
        System.out.println("Final cache size: " + cache.getSize());
        System.out.println("Expected operations: " + (numThreads * operationsPerThread));
        
        if (cache instanceof FlipCache) {
            ((FlipCache<Integer, String>) cache).printStats();
        }
    }
    
    private static void demonstrateMetrics() {
        System.out.println("\n3. Metrics Demonstration");
        
        Cache<String, String> cache = new CacheBuilder<String, String>()
                .withLRUEviction(5)
                .withMetrics()
                .build();
        
        // Perform various operations
        cache.putValue("key1", "value1");
        cache.putValue("key2", "value2");
        cache.putValue("key3", "value3");
        
        // Generate some hits
        cache.getValue("key1"); // Hit
        cache.getValue("key2"); // Hit
        cache.getValue("key1"); // Hit again
        
        // Generate some misses
        cache.getValue("nonexistent1"); // Miss
        cache.getValue("nonexistent2"); // Miss
        
        System.out.println("Operations performed:");
        System.out.println("- 3 PUT operations");
        System.out.println("- 3 successful GET operations (hits)");
        System.out.println("- 2 failed GET operations (misses)");
        System.out.println("- Expected hit ratio: 60% (3 hits out of 5 total gets)");
        
        if (cache instanceof FlipCache) {
            ((FlipCache<String, String>) cache).printStats();
        }
    }
}