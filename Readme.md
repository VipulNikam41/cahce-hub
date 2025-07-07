# FlipCache - Thread-Safe Cache Library

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/VipulNikam41/cahce-hub)
[![Java Version](https://img.shields.io/badge/java-17-blue)](https://openjdk.java.net/projects/jdk/17/)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

FlipCache is a high-performance, thread-safe cache library designed for Java applications. It provides configurable eviction policies, metrics tracking, and robust concurrency support.

## Features

- **Thread-Safe**: Built with concurrent collections and proper synchronization
- **Multiple Eviction Policies**: Time-bound, LRU, Size-bound, and extensible
- **Metrics Support**: Built-in hit ratio tracking and performance monitoring
- **Design Patterns**: Factory, Builder, Strategy, and Observer patterns
- **Parallelism**: Optimized for concurrent access with ReadWriteLock
- **Flexible Configuration**: Multiple ways to configure cache instances

## Architecture

### Core Components

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Cache       │    │ EvictionPolicy  │    │   DataSource    │
│   (Interface)   │    │   (Interface)   │    │   (Interface)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   FlipCache     │    │ TimeBoundEvic.  │    │InMemoryDataSrc  │
│ (Implementation)│    │ LRUEviction     │    │ (Implementation)│
│                 │    │ SizeBoundEvic.  │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Design Patterns Used

1. **Strategy Pattern**: Pluggable eviction policies
2. **Factory Pattern**: Easy cache creation with preset configurations
3. **Builder Pattern**: Flexible cache configuration
4. **Observer Pattern**: Metrics collection and monitoring
5. **Template Method**: Consistent cache operation flow

## Quick Start

### Using Factory Pattern

```java
// Create LRU cache with metrics
Cache<String, String> cache = CacheFactory.createLRUCacheWithMetrics(100);

// Create time-bound cache
Cache<String, String> cache = CacheFactory.createTimeBoundCache(5000L);

// Create size-bound cache
Cache<String, String> cache = CacheFactory.createSizeBoundCache(50);
```

### Using Builder Pattern

```java
Cache<String, String> cache = new CacheBuilder<String, String>()
    .withLRUEviction(100)
    .withMetrics()
    .build();
```

### Basic Operations

```java
// Put values
cache.putValue("key1", "value1");
cache.putValue("key2", "value2");

// Get values
String value = cache.getValue("key1"); // Returns "value1"
String missing = cache.getValue("key3"); // Returns null

// Check size
int size = cache.getSize();

// Get statistics (if metrics enabled)
if (cache instanceof FlipCache) {
    ((FlipCache<String, String>) cache).printStats();
}
```

## Eviction Policies

### 1. Time-Bound Eviction

Evicts keys after a specified time period, regardless of access frequency.

```java
Cache<String, String> cache = new CacheBuilder<String, String>()
    .withTimeBoundEviction(5000L) // 5 seconds
    .build();
```

### 2. LRU (Least Recently Used) Eviction

Evicts the least recently used keys when capacity is exceeded.

```java
Cache<String, String> cache = new CacheBuilder<String, String>()
    .withLRUEviction(100) // Max 100 entries
    .build();
```

### 3. Size-Bound Eviction

Evicts keys in FIFO order when size limit is exceeded.

```java
Cache<String, String> cache = new CacheBuilder<String, String>()
    .withSizeBoundEviction(50) // Max 50 entries
    .build();
```

### 4. Custom Eviction Policy

Implement your own eviction strategy:

```java
public class CustomEviction<K> implements EvictionPolicy<K> {
    @Override
    public List<K> evict() {
        // Your eviction logic here
        return keysToEvict;
    }
    
    @Override
    public void manageKey(K key) {
        // Track key usage
    }
}
```

## Thread Safety

FlipCache is designed for high-concurrency environments:

- **ConcurrentHashMap**: Thread-safe data storage
- **ReadWriteLock**: Coordinated read/write operations
- **Atomic Operations**: Thread-safe metrics tracking
- **Lock-free Eviction**: Efficient concurrent eviction

```java
// Safe for concurrent access
Cache<String, String> cache = CacheFactory.createLRUCacheWithMetrics(1000);

// Multiple threads can safely access
ExecutorService executor = Executors.newFixedThreadPool(10);
for (int i = 0; i < 10; i++) {
    executor.submit(() -> {
        cache.putValue("key" + Thread.currentThread().getId(), "value");
        cache.getValue("key" + Thread.currentThread().getId());
    });
}
```

## Metrics and Monitoring

Track cache performance with built-in metrics:

```java
Cache<String, String> cache = new CacheBuilder<String, String>()
    .withLRUEviction(100)
    .withMetrics()
    .build();

// Perform operations
cache.putValue("key1", "value1");
cache.getValue("key1"); // Hit
cache.getValue("key2"); // Miss

// Print statistics
((FlipCache<String, String>) cache).printStats();
// Output: Cache Stats - Hits: 1, Misses: 1, Hit Ratio: 50.00%
```

## Building and Running

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Build

```bash
mvn clean compile
```

### Run Tests

```bash
mvn test
```

### Create JAR

```bash
mvn package
```

### Run Application

```bash
java -jar target/flip-cache-1.0-SNAPSHOT.jar
```

## Interactive Demo

The main class provides an interactive cache demo:

```
FlipCache> PUT key1 value1
Info :: Ok

FlipCache> GET key1
Info :: Value: value1

FlipCache> STATS
Info :: Cache Stats - Hits: 1, Misses: 0, Hit Ratio: 100.00%

FlipCache> EXIT
Info :: Goodbye!
```

Available commands:
- `PUT <key> <value>` - Add key-value pair
- `GET <key>` - Retrieve value by key
- `STATS` - Show cache statistics
- `SIZE` - Show current cache size
- `EXIT` - Exit application

## Performance Characteristics

| Operation | Time Complexity | Space Complexity |
|-----------|-----------------|-------------------|
| PUT       | O(1) avg        | O(1)             |
| GET       | O(1) avg        | O(1)             |
| EVICT     | O(k) where k=evicted items | O(1) |

## Configuration Examples

### High-Performance Configuration

```java
Cache<String, String> cache = new CacheBuilder<String, String>()
    .withLRUEviction(10000)
    .withMetrics()
    .build();
```

### Memory-Constrained Configuration

```java
Cache<String, String> cache = new CacheBuilder<String, String>()
    .withSizeBoundEviction(100)
    .withMetrics()
    .build();
```

### Time-Sensitive Configuration

```java
Cache<String, String> cache = new CacheBuilder<String, String>()
    .withTimeBoundEviction(30000L) // 30 seconds
    .withMetrics()
    .build();
```

## Extension Points

### Custom Data Source

```java
public class RedisDataSource<K, V> implements DataSource<K, V> {
    // Redis implementation
}

Cache<String, String> cache = new CacheBuilder<String, String>()
    .withLRUEviction(1000)
    .withDataSource(new RedisDataSource<>())
    .build();
```

### Custom Metrics

```java
public class DetailedMetrics implements Metrics {
    // Advanced metrics implementation
}

Cache<String, String> cache = new CacheBuilder<String, String>()
    .withLRUEviction(1000)
    .withMetrics(new DetailedMetrics())
    .build();
```

## Best Practices

1. **Choose Appropriate Eviction Policy**
   - Use LRU for access-pattern-based caching
   - Use time-bound for time-sensitive data
   - Use size-bound for memory-constrained environments

2. **Enable Metrics in Production**
   - Monitor hit ratios to optimize cache configuration
   - Track performance over time

3. **Size Your Cache Appropriately**
   - Balance memory usage with hit ratio
   - Consider your application's access patterns

4. **Handle Null Values**
   - Cache returns null for missing keys
   - Implement appropriate null checks

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Changelog

### Version 1.0.0
- Initial release with thread-safe cache implementation
- Support for multiple eviction policies
- Built-in metrics and monitoring
- Factory and Builder patterns
- Comprehensive test coverage

