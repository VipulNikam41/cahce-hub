package com.lld.metrics;

import com.lld.config.Logger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe implementation of cache hit metrics.
 * Tracks cache hits and misses to calculate hit ratio.
 */
public class HitMetric implements Metrics {
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);

    @Override
    public void recordHit() {
        hitCount.incrementAndGet();
    }

    @Override
    public void recordMiss() {
        missCount.incrementAndGet();
    }

    @Override
    public void getStats() {
        long hits = hitCount.get();
        long misses = missCount.get();
        long total = hits + misses;
        
        if (total > 0) {
            double hitRatio = (double) hits / total;
            Logger.info(String.format("Cache Stats - Hits: %d, Misses: %d, Hit Ratio: %.2f%%", 
                       hits, misses, hitRatio * 100));
        } else {
            Logger.info("Cache Stats - No cache operations recorded yet");
        }
    }

    @Override
    public void printStats() {
        getStats();
    }

    @Override
    public double getHitRatio() {
        long hits = hitCount.get();
        long total = hits + missCount.get();
        return total > 0 ? (double) hits / total : 0.0;
    }
}
