package com.lld.metrics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Test cases for metrics implementations.
 */
class MetricsTest {
    
    private HitMetric hitMetric;
    
    @BeforeEach
    void setUp() {
        hitMetric = new HitMetric();
    }
    
    @Test
    @DisplayName("Test initial state")
    void testInitialState() {
        assertEquals(0.0, hitMetric.getHitRatio(), 0.001);
    }
    
    @Test
    @DisplayName("Test hit ratio calculation")
    void testHitRatioCalculation() {
        // Record 3 hits and 2 misses
        hitMetric.recordHit();
        hitMetric.recordHit();
        hitMetric.recordHit();
        hitMetric.recordMiss();
        hitMetric.recordMiss();
        
        // Hit ratio should be 3/5 = 0.6
        assertEquals(0.6, hitMetric.getHitRatio(), 0.001);
    }
    
    @Test
    @DisplayName("Test only hits")
    void testOnlyHits() {
        hitMetric.recordHit();
        hitMetric.recordHit();
        hitMetric.recordHit();
        
        // Hit ratio should be 1.0
        assertEquals(1.0, hitMetric.getHitRatio(), 0.001);
    }
    
    @Test
    @DisplayName("Test only misses")
    void testOnlyMisses() {
        hitMetric.recordMiss();
        hitMetric.recordMiss();
        hitMetric.recordMiss();
        
        // Hit ratio should be 0.0
        assertEquals(0.0, hitMetric.getHitRatio(), 0.001);
    }
    
    @Test
    @DisplayName("Test stats output")
    void testStatsOutput() {
        // Record some hits and misses
        hitMetric.recordHit();
        hitMetric.recordHit();
        hitMetric.recordMiss();
        
        // Should not throw exception
        assertDoesNotThrow(() -> hitMetric.getStats());
        assertDoesNotThrow(() -> hitMetric.printStats());
    }
    
    @Test
    @DisplayName("Test thread safety")
    void testThreadSafety() throws InterruptedException {
        final int numThreads = 10;
        final int operationsPerThread = 1000;
        final CountDownLatch latch = new CountDownLatch(numThreads);
        final ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        // Submit concurrent tasks
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        if (threadId % 2 == 0) {
                            hitMetric.recordHit();
                        } else {
                            hitMetric.recordMiss();
                        }
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
        // We should have equal hits and misses from even/odd threads
        assertEquals(0.5, hitMetric.getHitRatio(), 0.001);
    }
    
    @Test
    @DisplayName("Test large number of operations")
    void testLargeNumbers() {
        // Record large number of hits and misses
        for (int i = 0; i < 1000000; i++) {
            if (i % 3 == 0) {
                hitMetric.recordHit();
            } else {
                hitMetric.recordMiss();
            }
        }
        
        // Hit ratio should be approximately 1/3
        assertEquals(1.0/3.0, hitMetric.getHitRatio(), 0.001);
    }
}