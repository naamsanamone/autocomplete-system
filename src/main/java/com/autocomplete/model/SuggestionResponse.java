package com.autocomplete.model;

import com.autocomplete.ranking.ScoredWord;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * API response for autocomplete suggestions.
 *
 * <p>Contains the ranked suggestions along with metadata about
 * the query performance — response time, total matches found,
 * and the prefix that was searched.</p>
 *
 * @author Sai Venkat
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuggestionResponse {

    private String prefix;
    private int totalMatches;
    private int returnedCount;
    private double responseTimeMs;
    private List<Suggestion> suggestions;

    public SuggestionResponse() {}

    public SuggestionResponse(String prefix, List<Suggestion> suggestions,
                              int totalMatches, double responseTimeMs) {
        this.prefix = prefix;
        this.suggestions = suggestions;
        this.totalMatches = totalMatches;
        this.returnedCount = suggestions.size();
        this.responseTimeMs = responseTimeMs;
    }

    // ── Getters & Setters ──

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public int getTotalMatches() { return totalMatches; }
    public void setTotalMatches(int totalMatches) { this.totalMatches = totalMatches; }

    public int getReturnedCount() { return returnedCount; }
    public void setReturnedCount(int returnedCount) { this.returnedCount = returnedCount; }

    public double getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(double responseTimeMs) { this.responseTimeMs = responseTimeMs; }

    public List<Suggestion> getSuggestions() { return suggestions; }
    public void setSuggestions(List<Suggestion> suggestions) { this.suggestions = suggestions; }

    /**
     * A single suggestion item with score breakdown.
     */
    public static class Suggestion {
        private String word;
        private double score;
        private int frequency;
        private Map<String, Double> scoreBreakdown;

        public Suggestion() {}

        public Suggestion(ScoredWord scored) {
            this.word = scored.getWord();
            this.score = scored.getScore();
            this.frequency = scored.getFrequency();
            this.scoreBreakdown = scored.getScoreBreakdown();
        }

        public String getWord() { return word; }
        public void setWord(String word) { this.word = word; }

        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }

        public int getFrequency() { return frequency; }
        public void setFrequency(int frequency) { this.frequency = frequency; }

        public Map<String, Double> getScoreBreakdown() { return scoreBreakdown; }
        public void setScoreBreakdown(Map<String, Double> scoreBreakdown) { this.scoreBreakdown = scoreBreakdown; }
    }
}
