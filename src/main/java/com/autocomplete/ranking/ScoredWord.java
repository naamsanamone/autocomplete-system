package com.autocomplete.ranking;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a word with its computed ranking score.
 *
 * <p>Each ScoredWord carries:
 * <ul>
 *   <li>The word itself</li>
 *   <li>A composite score (weighted sum of all ranking strategies)</li>
 *   <li>A score breakdown showing each strategy's contribution (for transparency)</li>
 *   <li>Raw metadata (frequency, recency) for the frontend to display</li>
 * </ul>
 *
 * <p>Implements {@link Comparable} to enable natural ordering by score
 * (ascending for Min-Heap usage in TopKEngine).</p>
 *
 * @author Sai Venkat
 */
public class ScoredWord implements Comparable<ScoredWord> {

    private final String word;
    private double score;
    private final Map<String, Double> scoreBreakdown;

    // Raw metadata for frontend display
    private int frequency;
    private long lastAccessed;
    private String category;

    public ScoredWord(String word) {
        this.word = word;
        this.score = 0.0;
        this.scoreBreakdown = new HashMap<>();
    }

    public ScoredWord(String word, double score) {
        this.word = word;
        this.score = score;
        this.scoreBreakdown = new HashMap<>();
    }

    /**
     * Compares by score (ascending order).
     *
     * <p>Ascending order is intentional — the {@code TopKEngine} uses a
     * Min-Heap where the <em>smallest</em> score sits at the top and gets
     * evicted when a better candidate arrives.</p>
     *
     * @param other the other ScoredWord
     * @return negative if this.score < other.score
     */
    @Override
    public int compareTo(ScoredWord other) {
        int cmp = Double.compare(this.score, other.score);
        if (cmp != 0) {
            return cmp;
        }
        // Tie-breaker: alphabetical order (shorter/earlier word wins)
        return this.word.compareTo(other.word);
    }

    /**
     * Adds a strategy's score contribution to the breakdown.
     *
     * @param strategyName the name of the ranking strategy
     * @param value        the score contribution
     */
    public void addScoreComponent(String strategyName, double value) {
        scoreBreakdown.put(strategyName, value);
    }

    // ──────────────────────────────────────────────
    //  Getters & Setters
    // ──────────────────────────────────────────────

    public String getWord() {
        return word;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Map<String, Double> getScoreBreakdown() {
        return scoreBreakdown;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public long getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return String.format("ScoredWord{word='%s', score=%.4f, breakdown=%s}",
                word, score, scoreBreakdown);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScoredWord that = (ScoredWord) o;
        return word.equals(that.word);
    }

    @Override
    public int hashCode() {
        return word.hashCode();
    }
}
