package com.autocomplete.ranking;

import com.autocomplete.datastructure.Trie;
import com.autocomplete.datastructure.TrieNode;
import org.springframework.stereotype.Component;

/**
 * Ranks suggestions by how recently they were accessed or searched.
 *
 * <p><b>Scoring formula:</b> {@code 1.0 / (1 + hoursSinceLastAccess)}</p>
 *
 * <p><b>Time-decay function:</b></p>
 * <ul>
 *   <li>Just accessed (0 hours ago): 1.0 / (1 + 0) = <b>1.0</b></li>
 *   <li>1 hour ago: 1.0 / (1 + 1) = <b>0.50</b></li>
 *   <li>3 hours ago: 1.0 / (1 + 3) = <b>0.25</b></li>
 *   <li>24 hours ago: 1.0 / (1 + 24) = <b>0.04</b></li>
 *   <li>Never accessed: <b>0.0</b></li>
 * </ul>
 *
 * <p>This decay curve ensures recently searched terms get a significant
 * boost while older terms gracefully fade — similar to how Google
 * Trending works for autocomplete.</p>
 *
 * <p><b>Output range:</b> [0, 1] where 1.0 = just accessed this moment</p>
 *
 * @author Sai Venkat
 */
@Component
public class RecencyRankingStrategy implements RankingStrategy {

    private static final double MILLIS_PER_HOUR = 3_600_000.0;

    private final Trie trie;

    public RecencyRankingStrategy(Trie trie) {
        this.trie = trie;
    }

    @Override
    public String getName() {
        return "recency";
    }

    @Override
    public double score(String word, String prefix, RankingContext context) {
        TrieNode node = trie.searchNode(word);
        if (node == null || node.getLastAccessed() == 0) {
            return 0.0;
        }

        long lastAccessed = node.getLastAccessed();
        long currentTime = context.getCurrentTimestamp();

        if (currentTime <= lastAccessed) {
            return 1.0; // Just accessed
        }

        double hoursSinceAccess = (currentTime - lastAccessed) / MILLIS_PER_HOUR;

        // Inverse time-decay: 1 / (1 + hours)
        return 1.0 / (1.0 + hoursSinceAccess);
    }
}
