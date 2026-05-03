package com.autocomplete.service;

import com.autocomplete.datastructure.Trie;
import com.autocomplete.datastructure.TrieNode;
import com.autocomplete.history.SearchHistory;
import com.autocomplete.history.TrendingService;
import com.autocomplete.model.AnalyticsResponse;
import com.autocomplete.model.SuggestionResponse;
import com.autocomplete.ranking.RankingContext;
import com.autocomplete.ranking.RankingEngine;
import com.autocomplete.ranking.ScoredWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Core autocomplete service orchestrating all components.
 *
 * <p>This service ties together:
 * <ul>
 *   <li><b>Trie</b> — prefix-based candidate retrieval</li>
 *   <li><b>RankingEngine</b> — composite scoring + Top-K extraction</li>
 *   <li><b>SearchHistory</b> — per-user search tracking</li>
 *   <li><b>TrendingService</b> — global trending computation</li>
 * </ul>
 *
 * <h3>Suggestion Pipeline</h3>
 * <pre>
 * User types prefix
 *     → Trie.getWordsWithPrefix(prefix)          [O(L + N)]
 *     → RankingEngine.rankAndSelect(candidates)   [O(N log K)]
 *     → Return Top-K suggestions
 * </pre>
 *
 * @author Sai Venkat
 */
@Service
public class AutocompleteService {

    private static final Logger log = LoggerFactory.getLogger(AutocompleteService.class);

    private final Trie trie;
    private final RankingEngine rankingEngine;
    private final SearchHistory searchHistory;
    private final TrendingService trendingService;

    private final int defaultSuggestions;
    private final int maxSuggestions;

    // Performance metrics
    private final AtomicLong totalSuggestionRequests = new AtomicLong(0);
    private final AtomicLong totalResponseTimeNanos = new AtomicLong(0);
    private final long startupTimestamp = System.currentTimeMillis();

    // Track max frequency for normalization
    private volatile int maxGlobalFrequency = 1;

    public AutocompleteService(
            Trie trie,
            RankingEngine rankingEngine,
            SearchHistory searchHistory,
            TrendingService trendingService,
            @Value("${autocomplete.default-suggestions:5}") int defaultSuggestions,
            @Value("${autocomplete.max-suggestions:10}") int maxSuggestions
    ) {
        this.trie = trie;
        this.rankingEngine = rankingEngine;
        this.searchHistory = searchHistory;
        this.trendingService = trendingService;
        this.defaultSuggestions = defaultSuggestions;
        this.maxSuggestions = maxSuggestions;
    }

    // ═══════════════════════════════════════════════
    //  SUGGEST (Main Pipeline)
    // ═══════════════════════════════════════════════

    /**
     * Returns ranked autocomplete suggestions for the given prefix.
     *
     * <p>This is the main API endpoint pipeline:
     * <ol>
     *   <li>Retrieve all words matching the prefix from the Trie</li>
     *   <li>Build ranking context with user history and global stats</li>
     *   <li>Score and rank candidates using the composite RankingEngine</li>
     *   <li>Extract Top-K results via Min-Heap</li>
     *   <li>Track performance metrics</li>
     * </ol>
     *
     * @param prefix the search prefix
     * @param limit  number of suggestions (clamped to maxSuggestions)
     * @param userId optional user ID for personalization
     * @return response with ranked suggestions and metadata
     */
    public SuggestionResponse suggest(String prefix, Integer limit, String userId) {
        long startTime = System.nanoTime();

        // Determine effective limit
        int effectiveLimit = (limit != null && limit > 0)
                ? Math.min(limit, maxSuggestions)
                : defaultSuggestions;

        // Validate prefix
        if (prefix == null || prefix.trim().isEmpty()) {
            return new SuggestionResponse(prefix, List.of(), 0, 0);
        }

        String normalizedPrefix = prefix.toLowerCase().trim();

        // Step 1: Get all matching candidates from Trie
        List<TrieNode> candidates = trie.getWordsWithPrefix(normalizedPrefix);

        if (candidates.isEmpty()) {
            double elapsed = (System.nanoTime() - startTime) / 1_000_000.0;
            return new SuggestionResponse(normalizedPrefix, List.of(), 0, elapsed);
        }

        // Step 2: Build ranking context
        RankingContext context = buildContext(userId);

        // Step 3: Rank and select Top-K
        List<ScoredWord> ranked = rankingEngine.rankAndSelect(
                candidates, normalizedPrefix, effectiveLimit, context
        );

        // Step 4: Convert to response
        List<SuggestionResponse.Suggestion> suggestions = ranked.stream()
                .map(SuggestionResponse.Suggestion::new)
                .toList();

        long elapsed = System.nanoTime() - startTime;
        double elapsedMs = elapsed / 1_000_000.0;

        // Track metrics
        totalSuggestionRequests.incrementAndGet();
        totalResponseTimeNanos.addAndGet(elapsed);

        log.debug("Suggest '{}': {} matches, {} returned in {:.3f}ms",
                normalizedPrefix, candidates.size(), suggestions.size(), elapsedMs);

        return new SuggestionResponse(normalizedPrefix, suggestions,
                candidates.size(), elapsedMs);
    }

    // ═══════════════════════════════════════════════
    //  CORPUS MANAGEMENT
    // ═══════════════════════════════════════════════

    /**
     * Adds a word to the corpus.
     *
     * @param word      the word to add
     * @param frequency the base frequency
     */
    public void addWord(String word, int frequency) {
        trie.insert(word, frequency);
        updateMaxFrequency(frequency);
        log.debug("Added '{}' with frequency {}", word, frequency);
    }

    /**
     * Removes a word from the corpus.
     *
     * @param word the word to remove
     * @return true if the word was found and removed
     */
    public boolean removeWord(String word) {
        boolean deleted = trie.delete(word);
        if (deleted) {
            log.debug("Deleted '{}' from corpus", word);
        }
        return deleted;
    }

    /**
     * Checks if a word exists in the corpus.
     *
     * @param word the word to search for
     * @return true if the word exists
     */
    public boolean searchWord(String word) {
        return trie.search(word);
    }

    // ═══════════════════════════════════════════════
    //  SEARCH RECORDING
    // ═══════════════════════════════════════════════

    /**
     * Records a user search and updates all related systems.
     *
     * @param userId      the user identifier
     * @param query       the search query
     * @param wasSelected whether the user clicked a suggestion
     */
    public void recordSearch(String userId, String query, boolean wasSelected) {
        if (query == null || query.isBlank()) return;

        String normalized = query.toLowerCase().trim();

        // Update user history
        if (userId != null && !userId.isBlank()) {
            searchHistory.addSearch(userId, normalized, wasSelected);
        }

        // Update trending
        trendingService.recordSearch(normalized);

        // Update Trie recency
        trie.touchWord(normalized);

        // Boost frequency for selected suggestions
        if (wasSelected) {
            trie.updateFrequency(normalized, 1);
        }

        log.debug("Recorded search: user={}, query='{}', selected={}", userId, normalized, wasSelected);
    }

    // ═══════════════════════════════════════════════
    //  HISTORY
    // ═══════════════════════════════════════════════

    /**
     * Returns recent search queries for a user.
     */
    public List<String> getRecentSearches(String userId, int limit) {
        return searchHistory.getRecentUniqueQueries(userId, limit);
    }

    /**
     * Clears search history for a user.
     */
    public void clearHistory(String userId) {
        searchHistory.clearHistory(userId);
    }

    // ═══════════════════════════════════════════════
    //  TRENDING
    // ═══════════════════════════════════════════════

    /**
     * Returns trending search queries.
     */
    public List<TrendingService.TrendingEntry> getTrending(int limit) {
        return trendingService.getTrending(limit);
    }

    // ═══════════════════════════════════════════════
    //  ANALYTICS
    // ═══════════════════════════════════════════════

    /**
     * Returns comprehensive system analytics.
     */
    public AnalyticsResponse getAnalytics() {
        AnalyticsResponse analytics = new AnalyticsResponse();

        // Corpus
        analytics.setCorpusSize(trie.size());
        analytics.setTrieNodeCount(trie.getNodeCount());
        analytics.setTrieMaxDepth(trie.getMaxDepth());

        // Search
        long requests = totalSuggestionRequests.get();
        analytics.setTotalSuggestionRequests(requests);
        analytics.setTotalSearches(trendingService.getTotalSearchesRecorded());

        double avgMs = requests > 0
                ? (totalResponseTimeNanos.get() / (double) requests) / 1_000_000.0
                : 0;
        analytics.setAverageResponseTimeMs(Math.round(avgMs * 1000.0) / 1000.0);

        // History
        analytics.setTotalUsersWithHistory(searchHistory.getUserCount());
        analytics.setTotalHistoryEntries(searchHistory.getTotalEntryCount());

        // Trending
        analytics.setTrackedTrendingQueries(trendingService.getTrackedQueryCount());
        analytics.setTotalTrendingSearches(trendingService.getTotalSearchesRecorded());

        // Ranking
        analytics.setRankingWeights(rankingEngine.getStrategyWeights());

        // System
        analytics.setUptimeMs(System.currentTimeMillis() - startupTimestamp);
        analytics.setJavaVersion(System.getProperty("java.version"));

        return analytics;
    }

    // ═══════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════

    private RankingContext buildContext(String userId) {
        RankingContext.Builder builder = RankingContext.builder()
                .currentTimestamp(System.currentTimeMillis())
                .maxGlobalFrequency(maxGlobalFrequency);

        if (userId != null && !userId.isBlank()) {
            builder.userId(userId)
                    .userSearchFrequencies(searchHistory.getSearchFrequencies(userId));
        }

        return builder.build();
    }

    /**
     * Tracks the maximum frequency for normalization.
     */
    public void updateMaxFrequency(int freq) {
        if (freq > maxGlobalFrequency) {
            maxGlobalFrequency = freq;
        }
    }

    public int getMaxGlobalFrequency() {
        return maxGlobalFrequency;
    }
}
