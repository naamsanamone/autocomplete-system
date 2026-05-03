package com.autocomplete.ranking;

import java.util.Collections;
import java.util.Map;

/**
 * Context object carrying all data needed by ranking strategies.
 *
 * <p>This decouples ranking strategies from the data sources. Each strategy
 * reads only the context fields it needs, enabling clean dependency
 * injection and testability.</p>
 *
 * <p>Contains:
 * <ul>
 *   <li>User ID for personalization</li>
 *   <li>Current timestamp for recency calculations</li>
 *   <li>User search history frequencies for personalization scoring</li>
 *   <li>Global statistics for normalization</li>
 * </ul>
 *
 * @author Sai Venkat
 */
public class RankingContext {

    private final String userId;
    private final long currentTimestamp;
    private final Map<String, Integer> userSearchFrequencies;
    private final int maxGlobalFrequency;
    private final long oldestAccessTimestamp;

    private RankingContext(Builder builder) {
        this.userId = builder.userId;
        this.currentTimestamp = builder.currentTimestamp;
        this.userSearchFrequencies = builder.userSearchFrequencies != null
                ? Collections.unmodifiableMap(builder.userSearchFrequencies)
                : Collections.emptyMap();
        this.maxGlobalFrequency = builder.maxGlobalFrequency;
        this.oldestAccessTimestamp = builder.oldestAccessTimestamp;
    }

    // ──────────────────────────────────────────────
    //  Getters
    // ──────────────────────────────────────────────

    public String getUserId() {
        return userId;
    }

    public long getCurrentTimestamp() {
        return currentTimestamp;
    }

    /**
     * Returns the user's search frequency map.
     * Key = search query, Value = number of times the user searched for it.
     */
    public Map<String, Integer> getUserSearchFrequencies() {
        return userSearchFrequencies;
    }

    /**
     * Gets the user's search count for a specific word.
     *
     * @param word the word to look up
     * @return the user's search count, or 0 if never searched
     */
    public int getUserSearchCount(String word) {
        return userSearchFrequencies.getOrDefault(word, 0);
    }

    /**
     * The maximum frequency of any word in the corpus.
     * Used for normalizing frequency scores to [0, 1].
     */
    public int getMaxGlobalFrequency() {
        return maxGlobalFrequency;
    }

    /**
     * The oldest last-accessed timestamp in the corpus.
     * Used for normalizing recency scores.
     */
    public long getOldestAccessTimestamp() {
        return oldestAccessTimestamp;
    }

    /**
     * Whether this context has a valid user for personalization.
     */
    public boolean hasUser() {
        return userId != null && !userId.isBlank();
    }

    // ──────────────────────────────────────────────
    //  Builder Pattern
    // ──────────────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userId;
        private long currentTimestamp = System.currentTimeMillis();
        private Map<String, Integer> userSearchFrequencies;
        private int maxGlobalFrequency = 1;
        private long oldestAccessTimestamp = 0;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder currentTimestamp(long currentTimestamp) {
            this.currentTimestamp = currentTimestamp;
            return this;
        }

        public Builder userSearchFrequencies(Map<String, Integer> frequencies) {
            this.userSearchFrequencies = frequencies;
            return this;
        }

        public Builder maxGlobalFrequency(int maxFrequency) {
            this.maxGlobalFrequency = Math.max(1, maxFrequency);
            return this;
        }

        public Builder oldestAccessTimestamp(long oldest) {
            this.oldestAccessTimestamp = oldest;
            return this;
        }

        public RankingContext build() {
            return new RankingContext(this);
        }
    }
}
