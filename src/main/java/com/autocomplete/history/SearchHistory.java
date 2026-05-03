package com.autocomplete.history;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Per-user search history service for personalization.
 *
 * <p>Maintains a bounded search history for each user, enabling:
 * <ul>
 *   <li>Recent searches display (like Google's "Recent searches")</li>
 *   <li>Personalization scoring (frequently searched terms rank higher)</li>
 *   <li>User-level analytics (search patterns, most searched terms)</li>
 *   <li>History clearing (GDPR-compliant data deletion)</li>
 * </ul>
 *
 * <h3>Data Structure Choice</h3>
 * <p>Uses {@code Map<String, Deque<SearchEntry>>} where:
 * <ul>
 *   <li>Outer Map: userId → user's history (O(1) user lookup)</li>
 *   <li>Inner Deque: bounded FIFO queue of search entries
 *       (O(1) add to front, O(1) remove from back)</li>
 * </ul>
 *
 * <h3>Bounded History</h3>
 * <p>Each user's history is capped at {@code maxHistoryPerUser} entries.
 * When the limit is reached, the oldest entry is evicted — this prevents
 * unbounded memory growth in a real production system.</p>
 *
 * <h3>Thread Safety</h3>
 * <p>Uses {@link ConcurrentHashMap} for the outer map to support
 * concurrent user requests. Individual user histories are synchronized
 * via the Deque operations.</p>
 *
 * @author Sai Venkat
 */
@Service
public class SearchHistory {

    private final ConcurrentHashMap<String, Deque<SearchEntry>> userHistories;
    private final int maxHistoryPerUser;

    public SearchHistory(
            @Value("${autocomplete.max-history-per-user:100}") int maxHistoryPerUser
    ) {
        this.userHistories = new ConcurrentHashMap<>();
        this.maxHistoryPerUser = maxHistoryPerUser;
    }

    // ═══════════════════════════════════════════════
    //  RECORD SEARCHES
    // ═══════════════════════════════════════════════

    /**
     * Records a search event for a user.
     *
     * <p><b>Time:</b> O(1) amortized</p>
     *
     * <p>Adds the entry to the front of the user's history deque.
     * If the history exceeds the max size, the oldest entry is evicted.</p>
     *
     * @param userId the user identifier
     * @param query  the search query
     * @param wasSelected whether the user selected a suggestion
     */
    public void addSearch(String userId, String query, boolean wasSelected) {
        if (userId == null || userId.isBlank() || query == null || query.isBlank()) {
            return;
        }

        String normalizedQuery = query.toLowerCase().trim();
        SearchEntry entry = SearchEntry.now(normalizedQuery, wasSelected);

        Deque<SearchEntry> history = userHistories.computeIfAbsent(
                userId, k -> new ArrayDeque<>()
        );

        synchronized (history) {
            history.addFirst(entry);

            // Evict oldest if over capacity
            while (history.size() > maxHistoryPerUser) {
                history.removeLast();
            }
        }
    }

    /**
     * Records a typed search (not a suggestion selection).
     *
     * @param userId the user identifier
     * @param query  the search query
     */
    public void addSearch(String userId, String query) {
        addSearch(userId, query, false);
    }

    // ═══════════════════════════════════════════════
    //  RETRIEVE HISTORY
    // ═══════════════════════════════════════════════

    /**
     * Returns the user's most recent searches.
     *
     * @param userId the user identifier
     * @param limit  maximum number of entries to return
     * @return list of recent SearchEntries, newest first
     */
    public List<SearchEntry> getRecentSearches(String userId, int limit) {
        if (userId == null || userId.isBlank()) {
            return Collections.emptyList();
        }

        Deque<SearchEntry> history = userHistories.get(userId);
        if (history == null || history.isEmpty()) {
            return Collections.emptyList();
        }

        synchronized (history) {
            return history.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Returns the unique recent search queries (deduplicated).
     *
     * @param userId the user identifier
     * @param limit  maximum number of unique queries
     * @return list of unique recent queries, newest first
     */
    public List<String> getRecentUniqueQueries(String userId, int limit) {
        if (userId == null || userId.isBlank()) {
            return Collections.emptyList();
        }

        Deque<SearchEntry> history = userHistories.get(userId);
        if (history == null || history.isEmpty()) {
            return Collections.emptyList();
        }

        synchronized (history) {
            return history.stream()
                    .map(SearchEntry::getQuery)
                    .distinct()
                    .limit(limit)
                    .collect(Collectors.toList());
        }
    }

    // ═══════════════════════════════════════════════
    //  FREQUENCY DATA (for PersonalizationRankingStrategy)
    // ═══════════════════════════════════════════════

    /**
     * Computes the search frequency map for a user.
     *
     * <p>Returns how many times the user has searched for each query.
     * This data feeds into the {@code PersonalizationRankingStrategy}.</p>
     *
     * @param userId the user identifier
     * @return map of query → search count
     */
    public Map<String, Integer> getSearchFrequencies(String userId) {
        if (userId == null || userId.isBlank()) {
            return Collections.emptyMap();
        }

        Deque<SearchEntry> history = userHistories.get(userId);
        if (history == null || history.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Integer> frequencies = new HashMap<>();

        synchronized (history) {
            for (SearchEntry entry : history) {
                frequencies.merge(entry.getQuery(), 1, (a, b) -> a + b);
            }
        }

        return frequencies;
    }

    /**
     * Gets the search count for a specific query by a specific user.
     *
     * @param userId the user identifier
     * @param query  the search query
     * @return search count, or 0 if never searched
     */
    public int getSearchFrequency(String userId, String query) {
        return getSearchFrequencies(userId).getOrDefault(query.toLowerCase().trim(), 0);
    }

    // ═══════════════════════════════════════════════
    //  MANAGEMENT
    // ═══════════════════════════════════════════════

    /**
     * Clears all search history for a user.
     *
     * <p>This is the GDPR-compliant "right to be forgotten" operation.
     * In a real system, this would also clear from persistent storage.</p>
     *
     * @param userId the user identifier
     */
    public void clearHistory(String userId) {
        if (userId != null) {
            userHistories.remove(userId);
        }
    }

    /**
     * Returns the total number of users with history.
     *
     * @return user count
     */
    public int getUserCount() {
        return userHistories.size();
    }

    /**
     * Returns the total number of search entries across all users.
     *
     * @return total entry count
     */
    public int getTotalEntryCount() {
        return userHistories.values().stream()
                .mapToInt(Deque::size)
                .sum();
    }

    /**
     * Returns the history size for a specific user.
     *
     * @param userId the user identifier
     * @return history size, or 0 if no history
     */
    public int getHistorySize(String userId) {
        Deque<SearchEntry> history = userHistories.get(userId);
        return history != null ? history.size() : 0;
    }
}
