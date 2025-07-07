package com.lld.datastore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Test cases for data source implementations.
 */
class DataSourceTest {
    
    private InMemoryDataSource<String, String> dataSource;
    
    @BeforeEach
    void setUp() {
        dataSource = new InMemoryDataSource<>();
    }
    
    @Test
    @DisplayName("Test basic put and get operations")
    void testBasicOperations() {
        // Test put and get
        dataSource.put("key1", "value1");
        assertEquals("value1", dataSource.get("key1"));
        
        // Test get non-existent key
        assertNull(dataSource.get("nonexistent"));
        
        // Test size
        assertEquals(1, dataSource.size());
    }
    
    @Test
    @DisplayName("Test single key deletion")
    void testSingleKeyDeletion() {
        dataSource.put("key1", "value1");
        dataSource.put("key2", "value2");
        assertEquals(2, dataSource.size());
        
        // Delete single key
        dataSource.delete("key1");
        assertEquals(1, dataSource.size());
        assertNull(dataSource.get("key1"));
        assertEquals("value2", dataSource.get("key2"));
    }
    
    @Test
    @DisplayName("Test multiple keys deletion")
    void testMultipleKeysDeletion() {
        dataSource.put("key1", "value1");
        dataSource.put("key2", "value2");
        dataSource.put("key3", "value3");
        assertEquals(3, dataSource.size());
        
        // Delete multiple keys
        List<String> keysToDelete = Arrays.asList("key1", "key3");
        dataSource.delete(keysToDelete);
        
        assertEquals(1, dataSource.size());
        assertNull(dataSource.get("key1"));
        assertEquals("value2", dataSource.get("key2"));
        assertNull(dataSource.get("key3"));
    }
    
    @Test
    @DisplayName("Test overwrite existing key")
    void testOverwriteExistingKey() {
        dataSource.put("key1", "value1");
        assertEquals("value1", dataSource.get("key1"));
        assertEquals(1, dataSource.size());
        
        // Overwrite with new value
        dataSource.put("key1", "newvalue1");
        assertEquals("newvalue1", dataSource.get("key1"));
        assertEquals(1, dataSource.size()); // Size should remain the same
    }
    
    @Test
    @DisplayName("Test delete non-existent key")
    void testDeleteNonExistentKey() {
        dataSource.put("key1", "value1");
        assertEquals(1, dataSource.size());
        
        // Delete non-existent key should not affect size
        dataSource.delete("nonexistent");
        assertEquals(1, dataSource.size());
        assertEquals("value1", dataSource.get("key1"));
    }
    
    @Test
    @DisplayName("Test empty data source")
    void testEmptyDataSource() {
        assertEquals(0, dataSource.size());
        assertNull(dataSource.get("anykey"));
        
        // Delete from empty data source should not cause issues
        dataSource.delete("anykey");
        dataSource.delete(Arrays.asList("key1", "key2"));
        assertEquals(0, dataSource.size());
    }
    
    @Test
    @DisplayName("Test thread safety")
    void testThreadSafety() throws InterruptedException {
        final int numThreads = 10;
        final int operationsPerThread = 100;
        final CountDownLatch latch = new CountDownLatch(numThreads);
        final ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        // Submit concurrent tasks
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String key = "key" + threadId + "_" + j;
                        String value = "value" + threadId + "_" + j;
                        
                        dataSource.put(key, value);
                        assertEquals(value, dataSource.get(key));
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
        assertEquals(numThreads * operationsPerThread, dataSource.size());
        
        // Verify all keys are present
        for (int i = 0; i < numThreads; i++) {
            for (int j = 0; j < operationsPerThread; j++) {
                String key = "key" + i + "_" + j;
                String expectedValue = "value" + i + "_" + j;
                assertEquals(expectedValue, dataSource.get(key));
            }
        }
    }
    
    @Test
    @DisplayName("Test concurrent put and delete operations")
    void testConcurrentPutAndDelete() throws InterruptedException {
        final int numKeys = 1000;
        final CountDownLatch latch = new CountDownLatch(2);
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        
        // Thread 1: Put operations
        executor.submit(() -> {
            try {
                for (int i = 0; i < numKeys; i++) {
                    dataSource.put("key" + i, "value" + i);
                }
            } finally {
                latch.countDown();
            }
        });
        
        // Thread 2: Delete operations (with some delay to allow puts)
        executor.submit(() -> {
            try {
                Thread.sleep(10); // Small delay to allow some puts
                for (int i = 0; i < numKeys / 2; i++) {
                    dataSource.delete("key" + i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });
        
        // Wait for all threads to complete
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        
        // Verify final state - should have roughly half the keys
        assertTrue(dataSource.size() >= numKeys / 2);
        assertTrue(dataSource.size() <= numKeys);
    }
}