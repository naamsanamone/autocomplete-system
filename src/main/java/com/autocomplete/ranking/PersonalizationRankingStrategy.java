package com.autocomplete.ranking;

import org.springframework.stereotype.Component;

/**
 * Ranks suggestions based on the current user's personal search history.
 *
 * <p><b>Scoring formula:</b> {@code min(1.0, userSearchCount * 0.2)}</p>
 *
 * <p><b>Rationale:</b> If a user frequently searches for "spring boot",
 * that term should rank higher in <em>their</em> suggestions than it would
 * for a new user. This mimics how Google personalizes autocomplete based
 * on your past searches.</p>
 *
 * <p><b>Examples:</b></p>
 * <table>
 *   <tr><td>User searchCount</td><td>Score</td></tr>
 *   <tr><td>0 (never searched)</td><td><b>0.00</b></td></tr>
 *   <tr><td>1</td><td><b>0.20</b></td></tr>
 *   <tr><td>3</td><td><b>0.60</b></td></tr>
 *   <tr><td>5+</td><td><b>1.00</b> (capped)</td></tr>
 * </table>
 *
 * <p>The cap at 1.0 prevents a single obsessively-searched term from
 * permanently dominating all suggestions.</p>
 *
 * <p><b>Graceful degradation:</b> Returns 0.0 for anonymous users
 * (no userId in context), ensuring the system works without personalization.</p>
 *
 * <p><b>Output range:</b> [0, 1]</p>
 *
 * @author Sai Venkat
 */
@Component
public class PersonalizationRankingStrategy implements RankingStrategy {

    private static final double PERSONALIZATION_FACTOR = 0.2;
    private static final double MAX_SCORE = 1.0;

    @Override
    public String getName() {
        return "personalization";
    }

    @Override
    public double score(String word, String prefix, RankingContext context) {
        if (!context.hasUser()) {
            return 0.0; // No personalization for anonymous users
        }

        int userSearchCount = context.getUserSearchCount(word);

        if (userSearchCount <= 0) {
            return 0.0;
        }

        // Linear scaling capped at MAX_SCORE
        return Math.min(MAX_SCORE, userSearchCount * PERSONALIZATION_FACTOR);
    }
}
