package com.autocomplete.datastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the custom MinHeap implementation.
 * Validates heap property, insert/extract, edge cases, and ordering.
 */
@DisplayName("MinHeap Data Structure")
class MinHeapTest {

    private MinHeap<Integer> heap;

    @BeforeEach
    void setUp() {
        heap = new MinHeap<>();
    }

    @Nested
    @DisplayName("Basic Operations")
    class BasicOperations {

        @Test
        @DisplayName("Should insert and peek minimum")
        void insertAndPeek() {
            heap.insert(5);
            heap.insert(3);
            heap.insert(7);

            assertEquals(3, heap.peek());
            assertEquals(3, heap.size());
        }

        @Test
        @DisplayName("Should extract elements in ascending order")
        void extractInOrder() {
            heap.insert(5);
            heap.insert(1);
            heap.insert(3);
            heap.insert(2);
            heap.insert(4);

            assertEquals(1, heap.extractMin());
            assertEquals(2, heap.extractMin());
            assertEquals(3, heap.extractMin());
            assertEquals(4, heap.extractMin());
            assertEquals(5, heap.extractMin());
        }

        @Test
        @DisplayName("Should maintain heap property after operations")
        void heapPropertyMaintained() {
            heap.insert(10);
            heap.insert(4);
            heap.insert(15);
            heap.insert(1);
            heap.insert(8);

            assertTrue(heap.isValid());
            assertEquals(1, heap.extractMin());
            assertTrue(heap.isValid());
            assertEquals(4, heap.extractMin());
            assertTrue(heap.isValid());
        }

        @Test
        @DisplayName("Should handle single element")
        void singleElement() {
            heap.insert(42);
            assertEquals(42, heap.peek());
            assertEquals(42, heap.extractMin());
            assertTrue(heap.isEmpty());
        }

        @Test
        @DisplayName("Should handle duplicate values")
        void duplicateValues() {
            heap.insert(5);
            heap.insert(5);
            heap.insert(5);

            assertEquals(3, heap.size());
            assertEquals(5, heap.extractMin());
            assertEquals(5, heap.extractMin());
            assertEquals(5, heap.extractMin());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should throw on extract from empty heap")
        void extractFromEmpty() {
            assertThrows(NoSuchElementException.class, () -> heap.extractMin());
        }

        @Test
        @DisplayName("Should throw on peek at empty heap")
        void peekAtEmpty() {
            assertThrows(NoSuchElementException.class, () -> heap.peek());
        }

        @Test
        @DisplayName("Should throw on null insert")
        void nullInsert() {
            assertThrows(IllegalArgumentException.class, () -> heap.insert(null));
        }

        @Test
        @DisplayName("Should report isEmpty correctly")
        void isEmpty() {
            assertTrue(heap.isEmpty());
            heap.insert(1);
            assertFalse(heap.isEmpty());
            heap.extractMin();
            assertTrue(heap.isEmpty());
        }

        @Test
        @DisplayName("Should handle dynamic resizing")
        void dynamicResize() {
            // Insert more than default capacity (16)
            for (int i = 50; i >= 1; i--) {
                heap.insert(i);
            }

            assertEquals(50, heap.size());
            assertTrue(heap.isValid());

            // Extract all in order
            for (int i = 1; i <= 50; i++) {
                assertEquals(i, heap.extractMin());
            }
        }

        @Test
        @DisplayName("Should clear all elements")
        void clear() {
            heap.insert(1);
            heap.insert(2);
            heap.insert(3);
            heap.clear();
            assertTrue(heap.isEmpty());
            assertEquals(0, heap.size());
        }
    }

    @Nested
    @DisplayName("Custom Comparator")
    class CustomComparator {

        @Test
        @DisplayName("Should work as max-heap with reversed comparator")
        void maxHeap() {
            MinHeap<Integer> maxHeap = new MinHeap<Integer>(Comparator.reverseOrder());
            maxHeap.insert(1);
            maxHeap.insert(5);
            maxHeap.insert(3);

            assertEquals(5, maxHeap.extractMin()); // reversed = max first
            assertEquals(3, maxHeap.extractMin());
            assertEquals(1, maxHeap.extractMin());
        }

        @Test
        @DisplayName("Should work with string length comparator")
        void stringLengthComparator() {
            MinHeap<String> stringHeap = new MinHeap<String>(Comparator.comparingInt(String::length));
            stringHeap.insert("java");
            stringHeap.insert("go");
            stringHeap.insert("python");

            assertEquals("go", stringHeap.extractMin());     // length 2
            assertEquals("java", stringHeap.extractMin());   // length 4
            assertEquals("python", stringHeap.extractMin()); // length 6
        }
    }
}
