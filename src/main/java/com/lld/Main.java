package com.lld;

import com.lld.cache.Cache;
import com.lld.cache.CacheBuilder;
import com.lld.cache.CacheFactory;
import com.lld.cache.FlipCache;
import com.lld.config.Logger;
import com.lld.datastore.InMemoryDataSource;
import com.lld.eviction.TimeBoundEviction;

import java.util.Scanner;

/**
 * Main class demonstrating the FlipCache library usage.
 * Shows different cache configurations and usage patterns.
 */
public class Main {
    public static void main(String[] args) {
        // Demonstrate different cache creation approaches
        demonstrateCacheCreation();
        
        // Run interactive cache demo
        runInteractiveDemo();
    }
    
    private static void demonstrateCacheCreation() {
        Logger.info("=== Cache Creation Examples ===");
        
        // Using Factory pattern
        Cache<String, String> factoryCache = CacheFactory.createLRUCacheWithMetrics(10);
        Logger.info("Created LRU cache with metrics using Factory pattern");
        
        // Using Builder pattern  
        Cache<String, String> builderCache = new CacheBuilder<String, String>()
                .withLRUEviction(5)
                .withMetrics()
                .build();
        Logger.info("Created LRU cache with metrics using Builder pattern");
        
        // Traditional approach
        Cache<String, String> traditionalCache = new FlipCache<>(
                new TimeBoundEviction<>(5000L),
                new InMemoryDataSource<>()
        );
        Logger.info("Created time-bound cache using traditional approach");
        
        Logger.info("=== Demo Complete ===\n");
    }
    
    private static void runInteractiveDemo() {
        Logger.info("=== Interactive Cache Demo ===");
        Logger.info("Cache Configuration: LRU with max size 5 and metrics enabled");
        
        // Create cache with LRU eviction and metrics
        Cache<Integer, String> cache = new CacheBuilder<Integer, String>()
                .withLRUEviction(5)
                .withMetrics()
                .build();

        Scanner scanner = new Scanner(System.in);
        Logger.info("Commands: PUT <key> <value>, GET <key>, STATS, SIZE, EXIT");
        
        while (true) {
            System.out.print("FlipCache> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            String[] parts = input.split(" ");
            String command = parts[0].toUpperCase();
            
            try {
                switch (command) {
                    case "PUT":
                    case "P":
                        if (parts.length >= 3) {
                            cache.putValue(Integer.parseInt(parts[1]), parts[2]);
                        } else {
                            Logger.error("Usage: PUT <key> <value>");
                        }
                        break;
                        
                    case "GET":
                    case "G":
                        if (parts.length >= 2) {
                            String value = cache.getValue(Integer.parseInt(parts[1]));
                            if (value != null) {
                                Logger.info("Value: " + value);
                            }
                        } else {
                            Logger.error("Usage: GET <key>");
                        }
                        break;
                        
                    case "STATS":
                    case "S":
                        if (cache instanceof FlipCache) {
                            ((FlipCache<Integer, String>) cache).printStats();
                        }
                        break;
                        
                    case "SIZE":
                        Logger.info("Cache size: " + cache.getSize());
                        break;
                        
                    case "EXIT":
                    case "E":
                        Logger.info("Goodbye!");
                        return;
                        
                    default:
                        Logger.error("Unknown command. Available: PUT, GET, STATS, SIZE, EXIT");
                }
            } catch (NumberFormatException e) {
                Logger.error("Invalid number format");
            } catch (Exception e) {
                Logger.error("Error: " + e.getMessage());
            }
        }
    }
}