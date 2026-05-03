package com.autocomplete.ranking;

import com.autocomplete.datastructure.Trie;
import com.autocomplete.datastructure.TrieNode;
import org.springframework.stereotype.Component;

/**
 * Ranks suggestions by their global popularity / frequency count.
 *
 * <p><b>Scoring formula:</b> {@code log(1 + frequency) / log(1 + maxFrequency)}</p>
 *
 * <p><b>Why logarithmic dampening?</b></p>
 * <ul>
 *   <li>Prevents extremely popular terms from dominating all results</li>
 *   <li>A word with frequency 10000 shouldn't score 100x higher than one with 100</li>
 *   <li>log(10001)/log(10001) = 1.0 vs log(101)/log(10001) ≈ 0.50 — much fairer</li>
 *   <li>Without dampening: 10000/10000 = 1.0 vs 100/10000 = 0.01 — too extreme</li>
 * </ul>
 *
 * <p><b>Output range:</b> [0, 1] where 1.0 = most popular word in corpus</p>
 *
 * @author Sai Venkat
 */
@Component
public class FrequencyRankingStrategy implements RankingStrategy {

    private final Trie trie;

    public FrequencyRankingStrategy(Trie trie) {
        this.trie = trie;
    }

    @Override
    public String getName() {
        return "frequency";
    }

    @Override
    public double score(String word, String prefix, RankingContext context) {
        TrieNode node = trie.searchNode(word);
        if (node == null) {
            return 0.0;
        }

        int frequency = node.getFrequency();
        int maxFrequency = context.getMaxGlobalFrequency();

        if (maxFrequency <= 0) {
            return 0.0;
        }

        // Logarithmic dampening: prevents runaway popularity
        return Math.log(1 + frequency) / Math.log(1 + maxFrequency);
    }
}
