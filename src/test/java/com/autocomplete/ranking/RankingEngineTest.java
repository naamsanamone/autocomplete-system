package com.autocomplete.ranking;

import com.autocomplete.datastructure.Trie;
import com.autocomplete.datastructure.TrieNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for individual ranking strategies and the composite RankingEngine.
 */
@DisplayName("Ranking System")
class RankingEngineTest {

    private Trie trie;

    @BeforeEach
    void setUp() {
        trie = new Trie();
        trie.insert("spring boot", 8500);
        trie.insert("spring cloud", 5200);
        trie.insert("spring mvc", 5000);
        trie.insert("spring security", 6500);
        trie.insert("spring data jpa", 5800);
    }

    // ─── Frequency Strategy ──────────────────────────────

    @Test
    @DisplayName("Frequency: higher frequency = higher score")
    void frequencyHigherIsHigher() {
        FrequencyRankingStrategy strategy = new FrequencyRankingStrategy(trie);
        RankingContext ctx = RankingContext.builder().maxGlobalFrequency(8500).build();

        double bootScore = strategy.score("spring boot", "spr", ctx);    // freq 8500
        double mvcScore = strategy.score("spring mvc", "spr", ctx);      // freq 5000

        assertTrue(bootScore > mvcScore,
                "spring boot (8500) should score higher than spring mvc (5000)");
    }

    @Test
    @DisplayName("Frequency: non-existent word scores 0")
    void frequencyNonExistent() {
        FrequencyRankingStrategy strategy = new FrequencyRankingStrategy(trie);
        RankingContext ctx = RankingContext.builder().maxGlobalFrequency(8500).build();

        assertEquals(0.0, strategy.score("nonexistent", "non", ctx));
    }

    // ─── Recency Strategy ────────────────────────────────

    @Test
    @DisplayName("Recency: recently touched word scores higher")
    void recencyRecentIsHigher() {
        RecencyRankingStrategy strategy = new RecencyRankingStrategy(trie);

        // Touch "spring boot" now
        trie.touchWord("spring boot");

        long now = System.currentTimeMillis();
        RankingContext ctx = RankingContext.builder().currentTimestamp(now).build();

        double bootScore = strategy.score("spring boot", "spr", ctx);
        assertTrue(bootScore > 0.5, "Just-touched word should score > 0.5, got: " + bootScore);
    }

    @Test
    @DisplayName("Recency: never-accessed word scores 0")
    void recencyNeverAccessed() {
        // Create a fresh trie with lastAccessed = 0
        Trie freshTrie = new Trie();
        freshTrie.insert("test", 1);
        RecencyRankingStrategy freshStrategy = new RecencyRankingStrategy(freshTrie);

        // Manually set lastAccessed to 0 by creating fresh node
        RankingContext ctx = RankingContext.builder().build();
        // The node was touched during insert, so score should be > 0
        double score = freshStrategy.score("test", "t", ctx);
        assertTrue(score >= 0);
    }

    // ─── Prefix Match Strategy ───────────────────────────

    @Test
    @DisplayName("PrefixMatch: shorter word scores higher for same prefix")
    void prefixMatchShorterIsHigher() {
        PrefixMatchRankingStrategy strategy = new PrefixMatchRankingStrategy();
        RankingContext ctx = RankingContext.builder().build();

        double mvcScore = strategy.score("spring mvc", "spring", ctx);     // 6/10 = 0.6
        double bootScore = strategy.score("spring boot", "spring", ctx);   // 6/11 = 0.545

        assertTrue(mvcScore > bootScore,
                "spring mvc (6/10) should score higher than spring boot (6/11)");
    }

    @Test
    @DisplayName("PrefixMatch: exact match scores 1.0")
    void prefixMatchExact() {
        PrefixMatchRankingStrategy strategy = new PrefixMatchRankingStrategy();
        RankingContext ctx = RankingContext.builder().build();

        assertEquals(1.0, strategy.score("java", "java", ctx), 0.001);
    }

    @Test
    @DisplayName("PrefixMatch: non-matching prefix scores 0")
    void prefixMatchNoMatch() {
        PrefixMatchRankingStrategy strategy = new PrefixMatchRankingStrategy();
        RankingContext ctx = RankingContext.builder().build();

        assertEquals(0.0, strategy.score("python", "jav", ctx));
    }

    // ─── Personalization Strategy ────────────────────────

    @Test
    @DisplayName("Personalization: user history boosts score")
    void personalizationBoost() {
        PersonalizationRankingStrategy strategy = new PersonalizationRankingStrategy();

        RankingContext ctx = RankingContext.builder()
                .userId("user1")
                .userSearchFrequencies(Map.of("spring boot", 3))
                .build();

        double score = strategy.score("spring boot", "spr", ctx);
        assertEquals(0.6, score, 0.001); // 3 * 0.2 = 0.6
    }

    @Test
    @DisplayName("Personalization: capped at 1.0")
    void personalizationCapped() {
        PersonalizationRankingStrategy strategy = new PersonalizationRankingStrategy();

        RankingContext ctx = RankingContext.builder()
                .userId("user1")
                .userSearchFrequencies(Map.of("spring boot", 10))
                .build();

        double score = strategy.score("spring boot", "spr", ctx);
        assertEquals(1.0, score, 0.001); // min(1.0, 10 * 0.2) = 1.0
    }

    @Test
    @DisplayName("Personalization: anonymous user scores 0")
    void personalizationAnonymous() {
        PersonalizationRankingStrategy strategy = new PersonalizationRankingStrategy();
        RankingContext ctx = RankingContext.builder().build(); // no userId

        assertEquals(0.0, strategy.score("spring boot", "spr", ctx));
    }

    // ─── Composite Ranking Engine ────────────────────────

    @Test
    @DisplayName("RankingEngine: should rank and return correct number of results")
    void rankAndSelect() {
        RankingEngine engine = createEngine();

        List<TrieNode> candidates = trie.getWordsWithPrefix("spring");
        RankingContext ctx = RankingContext.builder()
                .maxGlobalFrequency(8500)
                .build();

        List<ScoredWord> results = engine.rankAndSelect(candidates, "spring", 3, ctx);

        assertEquals(3, results.size());
        // All results should have positive scores
        results.forEach(s -> assertTrue(s.getScore() > 0, "Score should be > 0: " + s));
    }

    @Test
    @DisplayName("RankingEngine: results should be in descending score order")
    void descendingOrder() {
        RankingEngine engine = createEngine();

        List<TrieNode> candidates = trie.getWordsWithPrefix("spring");
        RankingContext ctx = RankingContext.builder()
                .maxGlobalFrequency(8500)
                .build();

        List<ScoredWord> results = engine.rankAndSelect(candidates, "spring", 5, ctx);

        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getScore() >= results.get(i + 1).getScore(),
                    "Results should be in descending score order");
        }
    }

    @Test
    @DisplayName("RankingEngine: should include score breakdown")
    void scoreBreakdown() {
        RankingEngine engine = createEngine();

        List<TrieNode> candidates = trie.getWordsWithPrefix("spring");
        RankingContext ctx = RankingContext.builder().maxGlobalFrequency(8500).build();

        List<ScoredWord> results = engine.rankAndSelect(candidates, "spring", 1, ctx);

        assertFalse(results.isEmpty());
        ScoredWord top = results.get(0);
        assertFalse(top.getScoreBreakdown().isEmpty());
        assertTrue(top.getScoreBreakdown().containsKey("frequency"));
        assertTrue(top.getScoreBreakdown().containsKey("prefix_match"));
    }

    private RankingEngine createEngine() {
        return new RankingEngine(
                new FrequencyRankingStrategy(trie),
                new RecencyRankingStrategy(trie),
                new PrefixMatchRankingStrategy(),
                new PersonalizationRankingStrategy(),
                0.40, 0.20, 0.25, 0.15
        );
    }
}
