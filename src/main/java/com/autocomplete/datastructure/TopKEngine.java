package com.autocomplete.datastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Top-K engine using a Min-Heap to efficiently extract the K highest-scored items.
 *
 * <h3>The Classic Interview Pattern: Top-K with Min-Heap</h3>
 *
 * <p><b>Problem:</b> Given N candidates, find the K with the highest scores.</p>
 *
 * <p><b>Naive approach:</b> Sort all N candidates → O(N log N)</p>
 *
 * <p><b>Optimal approach (this class):</b> Maintain a Min-Heap of size K:</p>
 * <ol>
 *   <li>For each candidate:
 *     <ul>
 *       <li>If heap.size &lt; K → insert into heap</li>
 *       <li>Else if candidate.score &gt; heap.peek().score → extractMin + insert</li>
 *       <li>Else → skip (this candidate can't be in Top-K)</li>
 *     </ul>
 *   </li>
 *   <li>After processing all candidates, the heap contains exactly the Top-K</li>
 *   <li>Extract all elements and sort descending for final result</li>
 * </ol>
 *
 * <p><b>Why Min-Heap and not Max-Heap?</b></p>
 * <p>We want to <em>discard the smallest</em> of the current K candidates
 * when a better one arrives. A Min-Heap keeps the smallest at the top,
 * making it O(1) to check and O(log K) to replace. A Max-Heap would
 * require O(K) to find and remove the smallest.</p>
 *
 * <h3>Complexity Analysis</h3>
 * <table>
 *   <tr><td>Time</td><td>O(N log K) — each of N candidates does at most one insert + extract</td></tr>
 *   <tr><td>Space</td><td>O(K) — heap never exceeds K elements</td></tr>
 * </table>
 *
 * <p>When K &lt;&lt; N (e.g., K=5, N=10000), this is dramatically faster
 * than sorting: O(10000 × log 5) ≈ O(23000) vs O(10000 × log 10000) ≈ O(133000)</p>
 *
 * @param <T> the type of elements to rank
 * @author Sai Venkat
 */
public class TopKEngine<T> {

    private final Comparator<T> comparator;

    /**
     * Creates a TopKEngine with the given comparator.
     *
     * <p>The comparator determines the ranking order.
     * For Top-K <em>highest</em>: use natural ascending order (min-heap pops the smallest).
     * For Top-K <em>lowest</em>: use reversed comparator.</p>
     *
     * @param comparator comparator for element ordering
     */
    public TopKEngine(Comparator<T> comparator) {
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator cannot be null");
        }
        this.comparator = comparator;
    }

    /**
     * Extracts the Top-K elements from the given candidates.
     *
     * <p><b>Time:</b> O(N log K) where N = candidates.size()</p>
     * <p><b>Space:</b> O(K)</p>
     *
     * <p>The result is sorted in <em>descending</em> order by score
     * (highest first), which is the natural order for autocomplete
     * suggestions.</p>
     *
     * @param candidates the list of all candidates to consider
     * @param k          the number of top elements to return
     * @return list of Top-K elements, sorted descending by comparator
     */
    public List<T> getTopK(List<T> candidates, int k) {
        if (candidates == null || candidates.isEmpty() || k <= 0) {
            return Collections.emptyList();
        }

        // Clamp K to the number of candidates
        int effectiveK = Math.min(k, candidates.size());

        // Create a Min-Heap of size K
        // The min-heap keeps the SMALLEST of the top-K at the root
        MinHeap<T> minHeap = new MinHeap<>(comparator);

        for (T candidate : candidates) {
            if (minHeap.size() < effectiveK) {
                // Phase 1: Fill the heap until we have K elements
                minHeap.insert(candidate);
            } else if (comparator.compare(candidate, minHeap.peek()) > 0) {
                // Phase 2: This candidate is better than the current minimum of Top-K
                // Remove the smallest and insert the better candidate
                minHeap.extractMin();
                minHeap.insert(candidate);
            }
            // If candidate <= heap.peek(), skip — it can't be in Top-K
        }

        // Extract all elements from the heap
        List<T> result = new ArrayList<>(effectiveK);
        while (!minHeap.isEmpty()) {
            result.add(minHeap.extractMin());
        }

        // Reverse to get descending order (highest score first)
        Collections.reverse(result);

        return result;
    }

    /**
     * Extracts the Top-K elements with performance metrics.
     *
     * <p>Returns a {@link TopKResult} containing the results plus
     * metadata about the operation (candidates processed, time taken).</p>
     *
     * @param candidates the list of all candidates
     * @param k          the number of top elements to return
     * @return TopKResult with elements and metrics
     */
    public TopKResult<T> getTopKWithMetrics(List<T> candidates, int k) {
        long startTime = System.nanoTime();
        List<T> results = getTopK(candidates, k);
        long durationNanos = System.nanoTime() - startTime;

        return new TopKResult<>(
                results,
                candidates != null ? candidates.size() : 0,
                k,
                durationNanos
        );
    }

    /**
     * Result container for Top-K operations, including performance metrics.
     *
     * @param <T> the element type
     */
    public static class TopKResult<T> {
        private final List<T> elements;
        private final int totalCandidates;
        private final int requestedK;
        private final long durationNanos;

        public TopKResult(List<T> elements, int totalCandidates, int requestedK, long durationNanos) {
            this.elements = elements;
            this.totalCandidates = totalCandidates;
            this.requestedK = requestedK;
            this.durationNanos = durationNanos;
        }

        public List<T> getElements() {
            return elements;
        }

        public int getTotalCandidates() {
            return totalCandidates;
        }

        public int getRequestedK() {
            return requestedK;
        }

        public long getDurationNanos() {
            return durationNanos;
        }

        public double getDurationMillis() {
            return durationNanos / 1_000_000.0;
        }

        @Override
        public String toString() {
            return String.format("TopKResult{elements=%d, candidates=%d, k=%d, time=%.3fms}",
                    elements.size(), totalCandidates, requestedK, getDurationMillis());
        }
    }
}
