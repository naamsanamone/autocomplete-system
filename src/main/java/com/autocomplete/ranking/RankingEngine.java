package com.autocomplete.ranking;

import com.autocomplete.datastructure.TopKEngine;
import com.autocomplete.datastructure.TrieNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The composite ranking engine that orchestrates all ranking strategies.
 *
 * <p>This is the core orchestrator that:
 * <ol>
 *   <li>Takes raw prefix matches from the Trie</li>
 *   <li>Scores each match using ALL registered strategies</li>
 *   <li>Combines scores with configurable weights (weighted sum)</li>
 *   <li>Extracts Top-K results using the MinHeap-based TopKEngine</li>
 * </ol>
 *
 * <h3>Composite Scoring</h3>
 * <pre>
 * finalScore = Σ (weight_i × strategy_i.score(word, prefix, context))
 *
 * Default weights:
 *   Frequency:       0.40  (popularity matters most)
 *   Prefix Match:    0.25  (tighter matches rank higher)
 *   Recency:         0.20  (recently searched = likely relevant)
 *   Personalization: 0.15  (user-specific boost)
 *                    ────
 *                    1.00
 * </pre>
 *
 * <h3>Design Decision: Why Not Just Sort by Frequency?</h3>
 * <p>Single-factor ranking creates a poor user experience:
 * <ul>
 *   <li>"java" would always beat "javafx" regardless of what the user actually wants</li>
 *   <li>New terms could never surface because they start with frequency = 0</li>
 *   <li>Personalization becomes impossible</li>
 * </ul>
 * Composite scoring balances multiple signals, just like real search engines.</p>
 *
 * @author Sai Venkat
 */
@Component
public class RankingEngine {

    private final TopKEngine<ScoredWord> topKEngine;
    private final Map<RankingStrategy, Double> strategyWeights;

    public RankingEngine(
            FrequencyRankingStrategy frequencyStrategy,
            RecencyRankingStrategy recencyStrategy,
            PrefixMatchRankingStrategy prefixMatchStrategy,
            PersonalizationRankingStrategy personalizationStrategy,
            @Value("${autocomplete.ranking.frequency-weight:0.40}") double frequencyWeight,
            @Value("${autocomplete.ranking.recency-weight:0.20}") double recencyWeight,
            @Value("${autocomplete.ranking.prefix-match-weight:0.25}") double prefixMatchWeight,
            @Value("${autocomplete.ranking.personalization-weight:0.15}") double personalizationWeight
    ) {
        this.topKEngine = new TopKEngine<>(Comparator.<ScoredWord>naturalOrder());

        // Register strategies with their weights (order preserved for display)
        this.strategyWeights = new LinkedHashMap<>();
        this.strategyWeights.put(frequencyStrategy, frequencyWeight);
        this.strategyWeights.put(recencyStrategy, recencyWeight);
        this.strategyWeights.put(prefixMatchStrategy, prefixMatchWeight);
        this.strategyWeights.put(personalizationStrategy, personalizationWeight);
    }

    /**
     * Ranks a list of candidate words and returns the Top-K suggestions.
     *
     * <p><b>Pipeline:</b>
     * <ol>
     *   <li>Score each candidate using all strategies → O(N × S) where S = strategy count</li>
     *   <li>Extract Top-K using MinHeap → O(N log K)</li>
     *   <li>Return sorted descending (highest score first)</li>
     * </ol>
     *
     * <p><b>Total time:</b> O(N × S + N log K) ≈ O(N log K) since S is constant (4)</p>
     *
     * @param candidateNodes the Trie nodes matching the prefix
     * @param prefix         the user's input prefix
     * @param topK           the number of top results to return
     * @param context        the ranking context
     * @return Top-K scored words, sorted by score descending
     */
    public List<ScoredWord> rankAndSelect(
            List<TrieNode> candidateNodes,
            String prefix,
            int topK,
            RankingContext context
    ) {
        // Score all candidates
        List<ScoredWord> scoredCandidates = new ArrayList<>(candidateNodes.size());

        for (TrieNode node : candidateNodes) {
            ScoredWord scored = scoreWord(node, prefix, context);
            scoredCandidates.add(scored);
        }

        // Extract Top-K using MinHeap (O(N log K))
        return topKEngine.getTopK(scoredCandidates, topK);
    }

    /**
     * Ranks with performance metrics for the analytics dashboard.
     *
     * @param candidateNodes the Trie nodes matching the prefix
     * @param prefix         the user's input prefix
     * @param topK           the number of top results
     * @param context        the ranking context
     * @return TopKResult with elements and timing metrics
     */
    public TopKEngine.TopKResult<ScoredWord> rankAndSelectWithMetrics(
            List<TrieNode> candidateNodes,
            String prefix,
            int topK,
            RankingContext context
    ) {
        List<ScoredWord> scoredCandidates = new ArrayList<>(candidateNodes.size());

        for (TrieNode node : candidateNodes) {
            ScoredWord scored = scoreWord(node, prefix, context);
            scoredCandidates.add(scored);
        }

        return topKEngine.getTopKWithMetrics(scoredCandidates, topK);
    }

    /**
     * Scores a single word using all registered strategies.
     *
     * <p>Computes: finalScore = Σ (weight × strategy.score())</p>
     *
     * @param node    the Trie node
     * @param prefix  the search prefix
     * @param context the ranking context
     * @return a ScoredWord with composite score and breakdown
     */
    private ScoredWord scoreWord(TrieNode node, String prefix, RankingContext context) {
        String word = node.getWord();
        ScoredWord scored = new ScoredWord(word);
        scored.setFrequency(node.getFrequency());
        scored.setLastAccessed(node.getLastAccessed());

        double totalScore = 0.0;

        for (Map.Entry<RankingStrategy, Double> entry : strategyWeights.entrySet()) {
            RankingStrategy strategy = entry.getKey();
            double weight = entry.getValue();

            double strategyScore = strategy.score(word, prefix, context);
            double weightedScore = weight * strategyScore;

            scored.addScoreComponent(strategy.getName(), strategyScore);
            totalScore += weightedScore;
        }

        scored.setScore(totalScore);
        return scored;
    }

    /**
     * Returns the current strategy weights for display/debugging.
     *
     * @return map of strategy name → weight
     */
    public Map<String, Double> getStrategyWeights() {
        Map<String, Double> weights = new LinkedHashMap<>();
        for (Map.Entry<RankingStrategy, Double> entry : strategyWeights.entrySet()) {
            weights.put(entry.getKey().getName(), entry.getValue());
        }
        return weights;
    }
}
