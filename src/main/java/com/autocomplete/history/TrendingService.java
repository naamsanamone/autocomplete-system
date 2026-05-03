package com.autocomplete.history;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Tracks trending search queries across all users using time-bucketed counters.
 *
 * <h3>System Design Pattern: Sliding Window Counter</h3>
 * <p>This is a simplified version of the trending algorithm used by
 * Twitter, Google Trends, and YouTube. The approach:
 * <ol>
 *   <li>Maintain a counter for each search query</li>
 *   <li>Increment on each search</li>
 *   <li>Periodically decay all counters (every window interval)</li>
 *   <li>Queries with the highest counters are "trending"</li>
 * </ol>
 *
 * <h3>Why AtomicInteger?</h3>
 * <p>Multiple users search concurrently. {@link AtomicInteger} provides
 * lock-free thread-safe incrementing via CAS (Compare-And-Swap),
 * which is much faster than synchronized blocks under contention.</p>
 *
 * <h3>Scaling Discussion (Interview)</h3>
 * <p>In a distributed system, this would be replaced by:
 * <ul>
 *   <li>Redis Sorted Sets with TTL-based decay</li>
 *   <li>Apache Kafka for event streaming + Flink for real-time aggregation</li>
 *   <li>HyperLogLog for approximate unique query counting</li>
 * </ul>
 *
 * @author Sai Venkat
 */
@Service
public class TrendingService {

    /**
     * Global search counters: query → atomic count.
     * ConcurrentHashMap + AtomicInteger = lock-free concurrent updates.
     */
    private final ConcurrentHashMap<String, AtomicInteger> searchCounters;

    /**
     * Decay factor applied during each cleanup cycle.
     * 0.5 means counters are halved each window — queries must continue
     * being searched to remain trending.
     */
    private static final double DECAY_FACTOR = 0.5;

    /**
     * Minimum counter value before a query is removed entirely.
     * Prevents stale queries from lingering with tiny counts.
     */
    private static final int MIN_COUNTER_THRESHOLD = 2;

    private final int trendingWindowMinutes;

    // Metrics
    private long totalSearchesRecorded;
    private long lastDecayTimestamp;

    public TrendingService(
            @Value("${autocomplete.trending-window-minutes:60}") int trendingWindowMinutes
    ) {
        this.searchCounters = new ConcurrentHashMap<>();
        this.trendingWindowMinutes = trendingWindowMinutes;
        this.totalSearchesRecorded = 0;
        this.lastDecayTimestamp = System.currentTimeMillis();
    }

    // ═══════════════════════════════════════════════
    //  RECORD SEARCHES
    // ═══════════════════════════════════════════════

    /**
     * Records a search query for trending calculation.
     *
     * <p><b>Time:</b> O(1) — ConcurrentHashMap.computeIfAbsent + AtomicInteger.incrementAndGet
     * are both O(1) amortized and lock-free.</p>
     *
     * @param query the search query
     */
    public void recordSearch(String query) {
        if (query == null || query.isBlank()) {
            return;
        }

        String normalized = query.toLowerCase().trim();
        searchCounters
                .computeIfAbsent(normalized, k -> new AtomicInteger(0))
                .incrementAndGet();
        totalSearchesRecorded++;
    }

    // ═══════════════════════════════════════════════
    //  GET TRENDING
    // ═══════════════════════════════════════════════

    /**
     * Returns the top trending search queries.
     *
     * <p><b>Time:</b> O(N log K) — collects all counters, sorts by count,
     * takes top K. Could be optimized with a Max-Heap for streaming,
     * but N is typically small for trending (few hundred unique queries).</p>
     *
     * @param limit maximum number of trending queries to return
     * @return list of trending queries with their counts, sorted by count descending
     */
    public List<TrendingEntry> getTrending(int limit) {
        if (limit <= 0) {
            return List.of();
        }

        return searchCounters.entrySet().stream()
                .map(e -> new TrendingEntry(e.getKey(), e.getValue().get()))
                .filter(e -> e.count() > 0)
                .sorted(Comparator.comparingInt(TrendingEntry::count).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Returns the trending count for a specific query.
     *
     * @param query the search query
     * @return current trending count, or 0 if not tracked
     */
    public int getTrendingCount(String query) {
        if (query == null) return 0;
        AtomicInteger counter = searchCounters.get(query.toLowerCase().trim());
        return counter != null ? counter.get() : 0;
    }

    // ═══════════════════════════════════════════════
    //  DECAY (Periodic Cleanup)
    // ═══════════════════════════════════════════════

    /**
     * Applies time-decay to all counters.
     *
     * <p>This is scheduled to run at fixed intervals (configured via
     * {@code trendingWindowMinutes}). Each decay cycle:
     * <ol>
     *   <li>Multiplies each counter by the decay factor (0.5 = halved)</li>
     *   <li>Removes counters that fall below the minimum threshold</li>
     * </ol>
     *
     * <p>This ensures that trending queries must be <em>continuously</em>
     * searched to remain trending — old queries naturally decay away.</p>
     *
     * <p><b>Interview note:</b> In production, this would use Redis TTL
     * or time-bucketed counters with automatic expiry instead of a
     * scheduled task.</p>
     */
    @Scheduled(fixedDelayString = "#{${autocomplete.trending-window-minutes:60} * 60000}")
    public void decayCounters() {
        List<String> toRemove = new ArrayList<>();

        searchCounters.forEach((query, counter) -> {
            int decayed = (int) (counter.get() * DECAY_FACTOR);
            if (decayed < MIN_COUNTER_THRESHOLD) {
                toRemove.add(query);
            } else {
                counter.set(decayed);
            }
        });

        toRemove.forEach(searchCounters::remove);
        lastDecayTimestamp = System.currentTimeMillis();
    }

    /**
     * Manually triggers a decay cycle (useful for testing).
     */
    public void forceDecay() {
        decayCounters();
    }

    // ═══════════════════════════════════════════════
    //  METRICS
    // ═══════════════════════════════════════════════

    /**
     * Returns the number of unique queries currently being tracked.
     */
    public int getTrackedQueryCount() {
        return searchCounters.size();
    }

    /**
     * Returns the total number of searches recorded since startup.
     */
    public long getTotalSearchesRecorded() {
        return totalSearchesRecorded;
    }

    /**
     * Returns the timestamp of the last decay cycle.
     */
    public long getLastDecayTimestamp() {
        return lastDecayTimestamp;
    }

    /**
     * Returns the configured trending window in minutes.
     */
    public int getTrendingWindowMinutes() {
        return trendingWindowMinutes;
    }

    // ═══════════════════════════════════════════════
    //  TRENDING ENTRY RECORD
    // ═══════════════════════════════════════════════

    /**
     * A trending search entry with query and count.
     *
     * @param query the search query
     * @param count the current trending count
     */
    public record TrendingEntry(String query, int count) {

        @Override
        public String toString() {
            return String.format("%s (%d)", query, count);
        }
    }
}
