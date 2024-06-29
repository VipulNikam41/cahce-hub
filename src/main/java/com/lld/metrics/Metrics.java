package com.lld.metrics;

import com.lld.config.Logger;

public interface Metrics {
    default void getStats() {
        Logger.info("Not implemented");
    }
    default void printStats() {
        Logger.info("Not implemented");
    }
}
