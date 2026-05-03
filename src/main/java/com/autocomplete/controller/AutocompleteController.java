package com.autocomplete.controller;

import com.autocomplete.history.TrendingService;
import com.autocomplete.model.AnalyticsResponse;
import com.autocomplete.model.CorpusRequest;
import com.autocomplete.model.SuggestionResponse;
import com.autocomplete.service.AutocompleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API controller for the Autocomplete System.
 *
 * <p>Provides endpoints for:
 * <ul>
 *   <li>Autocomplete suggestions (the main feature)</li>
 *   <li>Corpus management (add/remove words)</li>
 *   <li>Search history (per-user)</li>
 *   <li>Trending queries (global)</li>
 *   <li>System analytics</li>
 * </ul>
 *
 * @author Sai Venkat
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Autocomplete API", description = "Search autocomplete with Trie + Heap ranking")
public class AutocompleteController {

    private final AutocompleteService autocompleteService;

    public AutocompleteController(AutocompleteService autocompleteService) {
        this.autocompleteService = autocompleteService;
    }

    // ═══════════════════════════════════════════════
    //  SUGGESTIONS
    // ═══════════════════════════════════════════════

    @GetMapping("/suggest")
    @Operation(summary = "Get autocomplete suggestions",
            description = "Returns Top-K ranked suggestions for the given prefix using Trie + Min-Heap")
    public ResponseEntity<SuggestionResponse> suggest(
            @Parameter(description = "Search prefix", example = "spr")
            @RequestParam String q,
            @Parameter(description = "Max suggestions to return (default: 5, max: 10)")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "User ID for personalized ranking")
            @RequestParam(required = false) String userId
    ) {
        SuggestionResponse response = autocompleteService.suggest(q, limit, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Exact word search", description = "Checks if a word exists in the corpus")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam String q
    ) {
        boolean found = autocompleteService.searchWord(q);
        return ResponseEntity.ok(Map.of(
                "query", q,
                "found", found
        ));
    }

    // ═══════════════════════════════════════════════
    //  CORPUS MANAGEMENT
    // ═══════════════════════════════════════════════

    @PostMapping("/corpus")
    @Operation(summary = "Add word to corpus", description = "Inserts a new word with frequency into the Trie")
    public ResponseEntity<Map<String, Object>> addWord(
            @Valid @RequestBody CorpusRequest request
    ) {
        autocompleteService.addWord(request.getWord(), request.getFrequency());
        return ResponseEntity.ok(Map.of(
                "status", "added",
                "word", request.getWord(),
                "frequency", request.getFrequency()
        ));
    }

    @DeleteMapping("/corpus/{word}")
    @Operation(summary = "Remove word from corpus", description = "Deletes a word from the Trie")
    public ResponseEntity<Map<String, Object>> removeWord(
            @PathVariable String word
    ) {
        boolean deleted = autocompleteService.removeWord(word);
        return ResponseEntity.ok(Map.of(
                "status", deleted ? "deleted" : "not_found",
                "word", word
        ));
    }

    // ═══════════════════════════════════════════════
    //  SEARCH HISTORY
    // ═══════════════════════════════════════════════

    @PostMapping("/history/{userId}")
    @Operation(summary = "Record a search", description = "Records a user search for history and trending")
    public ResponseEntity<Map<String, String>> recordSearch(
            @PathVariable String userId,
            @RequestParam String query,
            @RequestParam(defaultValue = "false") boolean selected
    ) {
        autocompleteService.recordSearch(userId, query, selected);
        return ResponseEntity.ok(Map.of(
                "status", "recorded",
                "userId", userId,
                "query", query
        ));
    }

    @GetMapping("/history/{userId}")
    @Operation(summary = "Get search history", description = "Returns recent unique searches for a user")
    public ResponseEntity<Map<String, Object>> getHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<String> recentSearches = autocompleteService.getRecentSearches(userId, limit);
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "searches", recentSearches,
                "count", recentSearches.size()
        ));
    }

    @DeleteMapping("/history/{userId}")
    @Operation(summary = "Clear search history", description = "Deletes all search history for a user (GDPR)")
    public ResponseEntity<Map<String, String>> clearHistory(
            @PathVariable String userId
    ) {
        autocompleteService.clearHistory(userId);
        return ResponseEntity.ok(Map.of(
                "status", "cleared",
                "userId", userId
        ));
    }

    // ═══════════════════════════════════════════════
    //  TRENDING
    // ═══════════════════════════════════════════════

    @GetMapping("/trending")
    @Operation(summary = "Get trending searches", description = "Returns globally trending search queries")
    public ResponseEntity<Map<String, Object>> getTrending(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<TrendingService.TrendingEntry> trending = autocompleteService.getTrending(limit);
        List<Map<String, Object>> entries = trending.stream()
                .map(e -> Map.<String, Object>of("query", e.query(), "count", e.count()))
                .toList();

        return ResponseEntity.ok(Map.of(
                "trending", entries,
                "count", entries.size()
        ));
    }

    // ═══════════════════════════════════════════════
    //  ANALYTICS
    // ═══════════════════════════════════════════════

    @GetMapping("/analytics")
    @Operation(summary = "System analytics", description = "Returns comprehensive system metrics")
    public ResponseEntity<AnalyticsResponse> getAnalytics() {
        return ResponseEntity.ok(autocompleteService.getAnalytics());
    }
}
