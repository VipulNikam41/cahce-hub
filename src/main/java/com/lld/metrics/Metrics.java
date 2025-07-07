package com.lld.metrics;

import com.lld.config.Logger;

/**
 * Interface for cache metrics collection and reporting.
 */
public interface Metrics {
    /**
     * Record a cache hit.
     */
    void recordHit();
    
    /**
     * Record a cache miss.
     */
    void recordMiss();
    
    /**
     * Get hit ratio statistics.
     */
    void getStats();
    
    /**
     * Print statistics to console.
     */
    void printStats();
    
    /**
     * Get the hit ratio as a percentage.
     * @return hit ratio (0.0 to 1.0)
     */
    double getHitRatio();
}
