package com.autocomplete;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Autocomplete System — DSA + System Design Hybrid
 *
 * A production-grade search autocomplete engine powered by:
 * - Trie data structure for O(L) prefix matching
 * - Min-Heap for O(N log K) Top-K extraction
 * - Composite ranking with Strategy Pattern (frequency, recency, personalization)
 * - Real-time debounced API with keyboard navigation
 *
 * @author Sai Venkat
 */
@SpringBootApplication
@EnableScheduling
public class AutocompleteApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutocompleteApplication.class, args);
    }
}
