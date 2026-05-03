package com.autocomplete.ranking;

/**
 * Strategy interface for ranking autocomplete suggestions.
 *
 * <p>Implements the <b>Strategy Pattern</b> — each ranking dimension
 * (frequency, recency, prefix match, personalization) is encapsulated
 * in its own strategy class. The {@link RankingEngine} composes multiple
 * strategies with configurable weights to produce a final score.</p>
 *
 * <h3>Why Strategy Pattern?</h3>
 * <ul>
 *   <li><b>Open/Closed Principle:</b> Add new ranking factors without modifying existing code</li>
 *   <li><b>Testability:</b> Each strategy is unit-testable in isolation</li>
 *   <li><b>Configurability:</b> Weights can be tuned per-deployment via application.yml</li>
 *   <li><b>Interview talking point:</b> Shows design pattern mastery beyond just DSA</li>
 * </ul>
 *
 * @author Sai Venkat
 */
public interface RankingStrategy {

    /**
     * Returns the human-readable name of this ranking strategy.
     * Used in score breakdowns and analytics dashboards.
     *
     * @return strategy name (e.g., "frequency", "recency")
     */
    String getName();

    /**
     * Computes a score for the given word based on this strategy's criteria.
     *
     * <p>The score should be normalized to approximately [0, 1] range
     * to ensure fair weighting when combined with other strategies.
     * Exact range may vary, but should be bounded and consistent.</p>
     *
     * @param word    the candidate word being scored
     * @param prefix  the user's input prefix that triggered this suggestion
     * @param context the ranking context with user data and global stats
     * @return a score value (higher = more relevant)
     */
    double score(String word, String prefix, RankingContext context);
}
