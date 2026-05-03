package com.autocomplete.ranking;

import org.springframework.stereotype.Component;

/**
 * Ranks suggestions by how closely the prefix matches the word.
 *
 * <p><b>Scoring formula:</b> {@code prefixLength / wordLength}</p>
 *
 * <p><b>Rationale:</b> When a user types "jav", the word "java" (3/4 = 0.75)
 * should rank higher than "javascript" (3/10 = 0.30) because it's a
 * tighter, more confident match. The user has typed a larger proportion
 * of the shorter word.</p>
 *
 * <p><b>Examples:</b></p>
 * <table>
 *   <tr><td>Prefix</td><td>Word</td><td>Score</td></tr>
 *   <tr><td>"spr"</td><td>"spring"</td><td>3/6 = <b>0.500</b></td></tr>
 *   <tr><td>"spr"</td><td>"spring boot"</td><td>3/11 = <b>0.273</b></td></tr>
 *   <tr><td>"spring"</td><td>"spring"</td><td>6/6 = <b>1.000</b> (exact match)</td></tr>
 *   <tr><td>"k"</td><td>"kubernetes"</td><td>1/10 = <b>0.100</b></td></tr>
 * </table>
 *
 * <p><b>Output range:</b> (0, 1] where 1.0 = exact match</p>
 *
 * @author Sai Venkat
 */
@Component
public class PrefixMatchRankingStrategy implements RankingStrategy {

    @Override
    public String getName() {
        return "prefix_match";
    }

    @Override
    public double score(String word, String prefix, RankingContext context) {
        if (word == null || word.isEmpty() || prefix == null || prefix.isEmpty()) {
            return 0.0;
        }

        String normalizedWord = word.toLowerCase().trim();
        String normalizedPrefix = prefix.toLowerCase().trim();

        // Verify the word actually starts with the prefix
        if (!normalizedWord.startsWith(normalizedPrefix)) {
            return 0.0;
        }

        // Score = prefix coverage ratio
        return (double) normalizedPrefix.length() / normalizedWord.length();
    }
}
