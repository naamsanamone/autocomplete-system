package com.autocomplete.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Autocomplete REST API.
 * Tests full request/response cycle with the actual Spring context.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Autocomplete API Integration")
@SuppressWarnings("null")
class AutocompleteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ─── Suggestions ─────────────────────────────────────

    @Test
    @DisplayName("GET /api/suggest — returns suggestions for valid prefix")
    void suggestReturnsResults() throws Exception {
        mockMvc.perform(get("/api/suggest").param("q", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prefix").value("java"))
                .andExpect(jsonPath("$.suggestions").isArray())
                .andExpect(jsonPath("$.suggestions.length()", greaterThan(0)))
                .andExpect(jsonPath("$.suggestions[0].word").exists())
                .andExpect(jsonPath("$.suggestions[0].score").isNumber())
                .andExpect(jsonPath("$.responseTimeMs").isNumber());
    }

    @Test
    @DisplayName("GET /api/suggest — returns empty for no-match prefix")
    void suggestReturnsEmpty() throws Exception {
        mockMvc.perform(get("/api/suggest").param("q", "xyznonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions.length()", is(0)))
                .andExpect(jsonPath("$.totalMatches").value(0));
    }

    @Test
    @DisplayName("GET /api/suggest — respects limit parameter")
    void suggestRespectsLimit() throws Exception {
        mockMvc.perform(get("/api/suggest").param("q", "s").param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions.length()", lessThanOrEqualTo(3)));
    }

    @Test
    @DisplayName("GET /api/suggest — suggestions contain score breakdown")
    void suggestContainsBreakdown() throws Exception {
        mockMvc.perform(get("/api/suggest").param("q", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions[0].scoreBreakdown").exists())
                .andExpect(jsonPath("$.suggestions[0].scoreBreakdown.frequency").isNumber());
    }

    // ─── Search ──────────────────────────────────────────

    @Test
    @DisplayName("GET /api/search — finds existing word")
    void searchFindsWord() throws Exception {
        mockMvc.perform(get("/api/search").param("q", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(true));
    }

    @Test
    @DisplayName("GET /api/search — returns false for missing word")
    void searchMissing() throws Exception {
        mockMvc.perform(get("/api/search").param("q", "nonexistentword"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(false));
    }

    // ─── Corpus ──────────────────────────────────────────

    @Test
    @DisplayName("POST /api/corpus — adds a new word")
    void addWord() throws Exception {
        mockMvc.perform(post("/api/corpus")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"word\": \"testword123\", \"frequency\": 42}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("added"))
                .andExpect(jsonPath("$.word").value("testword123"));

        // Verify it's searchable
        mockMvc.perform(get("/api/search").param("q", "testword123"))
                .andExpect(jsonPath("$.found").value(true));
    }

    @Test
    @DisplayName("POST /api/corpus — validates blank word")
    void addBlankWord() throws Exception {
        mockMvc.perform(post("/api/corpus")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"word\": \"\", \"frequency\": 1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/corpus/{word} — deletes a word")
    void deleteWord() throws Exception {
        // Add first
        mockMvc.perform(post("/api/corpus")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"word\": \"deletetest\", \"frequency\": 1}"));

        // Delete
        mockMvc.perform(delete("/api/corpus/deletetest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("deleted"));
    }

    // ─── History ─────────────────────────────────────────

    @Test
    @DisplayName("POST + GET /api/history — records and retrieves history")
    void recordAndRetrieveHistory() throws Exception {
        String userId = "testuser_" + System.currentTimeMillis();

        // Record
        mockMvc.perform(post("/api/history/" + userId)
                        .param("query", "spring boot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("recorded"));

        // Retrieve
        mockMvc.perform(get("/api/history/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.searches.length()", greaterThan(0)));
    }

    @Test
    @DisplayName("DELETE /api/history/{userId} — clears history")
    void clearHistory() throws Exception {
        String userId = "clearuser_" + System.currentTimeMillis();

        mockMvc.perform(post("/api/history/" + userId).param("query", "test"));
        mockMvc.perform(delete("/api/history/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("cleared"));
    }

    // ─── Trending ────────────────────────────────────────

    @Test
    @DisplayName("GET /api/trending — returns trending list")
    void getTrending() throws Exception {
        mockMvc.perform(get("/api/trending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trending").isArray());
    }

    // ─── Analytics ───────────────────────────────────────

    @Test
    @DisplayName("GET /api/analytics — returns comprehensive metrics")
    void getAnalytics() throws Exception {
        mockMvc.perform(get("/api/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.corpusSize").isNumber())
                .andExpect(jsonPath("$.trieNodeCount").isNumber())
                .andExpect(jsonPath("$.trieMaxDepth").isNumber())
                .andExpect(jsonPath("$.rankingWeights").exists())
                .andExpect(jsonPath("$.javaVersion").exists());
    }
}
