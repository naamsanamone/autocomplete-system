package com.autocomplete.model;

import java.util.Map;

/**
 * Response DTO for system analytics.
 *
 * @author Sai Venkat
 */
public class AnalyticsResponse {

    // Corpus stats
    private int corpusSize;
    private int trieNodeCount;
    private int trieMaxDepth;

    // Search stats
    private long totalSearches;
    private long totalSuggestionRequests;
    private double averageResponseTimeMs;

    // History stats
    private int totalUsersWithHistory;
    private int totalHistoryEntries;

    // Trending stats
    private int trackedTrendingQueries;
    private long totalTrendingSearches;

    // Ranking weights
    private Map<String, Double> rankingWeights;

    // System info
    private long uptimeMs;
    private String javaVersion;

    public AnalyticsResponse() {}

    // ── Getters & Setters ──

    public int getCorpusSize() { return corpusSize; }
    public void setCorpusSize(int corpusSize) { this.corpusSize = corpusSize; }

    public int getTrieNodeCount() { return trieNodeCount; }
    public void setTrieNodeCount(int trieNodeCount) { this.trieNodeCount = trieNodeCount; }

    public int getTrieMaxDepth() { return trieMaxDepth; }
    public void setTrieMaxDepth(int trieMaxDepth) { this.trieMaxDepth = trieMaxDepth; }

    public long getTotalSearches() { return totalSearches; }
    public void setTotalSearches(long totalSearches) { this.totalSearches = totalSearches; }

    public long getTotalSuggestionRequests() { return totalSuggestionRequests; }
    public void setTotalSuggestionRequests(long totalSuggestionRequests) { this.totalSuggestionRequests = totalSuggestionRequests; }

    public double getAverageResponseTimeMs() { return averageResponseTimeMs; }
    public void setAverageResponseTimeMs(double averageResponseTimeMs) { this.averageResponseTimeMs = averageResponseTimeMs; }

    public int getTotalUsersWithHistory() { return totalUsersWithHistory; }
    public void setTotalUsersWithHistory(int totalUsersWithHistory) { this.totalUsersWithHistory = totalUsersWithHistory; }

    public int getTotalHistoryEntries() { return totalHistoryEntries; }
    public void setTotalHistoryEntries(int totalHistoryEntries) { this.totalHistoryEntries = totalHistoryEntries; }

    public int getTrackedTrendingQueries() { return trackedTrendingQueries; }
    public void setTrackedTrendingQueries(int trackedTrendingQueries) { this.trackedTrendingQueries = trackedTrendingQueries; }

    public long getTotalTrendingSearches() { return totalTrendingSearches; }
    public void setTotalTrendingSearches(long totalTrendingSearches) { this.totalTrendingSearches = totalTrendingSearches; }

    public Map<String, Double> getRankingWeights() { return rankingWeights; }
    public void setRankingWeights(Map<String, Double> rankingWeights) { this.rankingWeights = rankingWeights; }

    public long getUptimeMs() { return uptimeMs; }
    public void setUptimeMs(long uptimeMs) { this.uptimeMs = uptimeMs; }

    public String getJavaVersion() { return javaVersion; }
    public void setJavaVersion(String javaVersion) { this.javaVersion = javaVersion; }
}
