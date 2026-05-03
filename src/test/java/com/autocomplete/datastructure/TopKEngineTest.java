package com.autocomplete.datastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the TopKEngine — validates O(N log K) extraction correctness.
 */
@DisplayName("TopKEngine")
class TopKEngineTest {

    private final TopKEngine<Integer> engine =
            new TopKEngine<Integer>(Comparator.naturalOrder());

    @Test
    @DisplayName("Should extract Top-K elements in descending order")
    void topKDescendingOrder() {
        List<Integer> candidates = List.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3);
        List<Integer> topK = engine.getTopK(candidates, 3);

        assertEquals(List.of(9, 6, 5), topK);
    }

    @Test
    @DisplayName("Should handle K = 1")
    void topOne() {
        List<Integer> candidates = List.of(10, 50, 30, 20, 40);
        List<Integer> topK = engine.getTopK(candidates, 1);

        assertEquals(List.of(50), topK);
    }

    @Test
    @DisplayName("Should handle K > N (return all sorted)")
    void kGreaterThanN() {
        List<Integer> candidates = List.of(3, 1, 2);
        List<Integer> topK = engine.getTopK(candidates, 10);

        assertEquals(3, topK.size());
        assertEquals(List.of(3, 2, 1), topK);
    }

    @Test
    @DisplayName("Should handle K = 0")
    void kZero() {
        List<Integer> candidates = List.of(1, 2, 3);
        List<Integer> topK = engine.getTopK(candidates, 0);

        assertTrue(topK.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty candidates")
    void emptyCandidates() {
        List<Integer> topK = engine.getTopK(List.of(), 5);
        assertTrue(topK.isEmpty());
    }

    @Test
    @DisplayName("Should handle null candidates")
    void nullCandidates() {
        List<Integer> topK = engine.getTopK(null, 5);
        assertTrue(topK.isEmpty());
    }

    @Test
    @DisplayName("Should handle duplicate values")
    void duplicates() {
        List<Integer> candidates = List.of(5, 5, 5, 3, 3, 1);
        List<Integer> topK = engine.getTopK(candidates, 3);

        assertEquals(List.of(5, 5, 5), topK);
    }

    @Test
    @DisplayName("Should include metrics in getTopKWithMetrics")
    void withMetrics() {
        List<Integer> candidates = List.of(10, 20, 30, 40, 50);
        TopKEngine.TopKResult<Integer> result = engine.getTopKWithMetrics(candidates, 2);

        assertEquals(List.of(50, 40), result.getElements());
        assertEquals(5, result.getTotalCandidates());
        assertEquals(2, result.getRequestedK());
        assertTrue(result.getDurationNanos() > 0);
        assertTrue(result.getDurationMillis() >= 0);
    }

    @Test
    @DisplayName("Should work with string comparator")
    void stringTopK() {
        TopKEngine<String> stringEngine = new TopKEngine<String>(Comparator.naturalOrder());
        List<String> candidates = List.of("banana", "apple", "cherry", "date", "elderberry");
        List<String> topK = stringEngine.getTopK(candidates, 2);

        assertEquals(List.of("elderberry", "date"), topK);
    }

    @Test
    @DisplayName("Should handle large dataset efficiently")
    void largeDataset() {
        List<Integer> candidates = new java.util.ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            candidates.add(i);
        }
        java.util.Collections.shuffle(candidates);

        List<Integer> topK = engine.getTopK(candidates, 5);

        assertEquals(5, topK.size());
        assertEquals(List.of(9999, 9998, 9997, 9996, 9995), topK);
    }
}
