package com.autocomplete.history;

/**
 * Represents a single search event in a user's history.
 *
 * <p>Each entry captures:
 * <ul>
 *   <li>The search query the user typed</li>
 *   <li>The timestamp when the search occurred</li>
 *   <li>Whether the user selected a suggestion (vs just typing)</li>
 * </ul>
 *
 * <p>The {@code wasSelected} flag is important for analytics:
 * a suggestion that was clicked is a stronger relevance signal
 * than one that was merely displayed.</p>
 *
 * @author Sai Venkat
 */
public class SearchEntry {

    private final String query;
    private final long timestamp;
    private final boolean wasSelected;

    public SearchEntry(String query, long timestamp, boolean wasSelected) {
        this.query = query;
        this.timestamp = timestamp;
        this.wasSelected = wasSelected;
    }

    /**
     * Creates a search entry with the current timestamp.
     *
     * @param query       the search query
     * @param wasSelected whether the user selected a suggestion
     * @return a new SearchEntry
     */
    public static SearchEntry now(String query, boolean wasSelected) {
        return new SearchEntry(query, System.currentTimeMillis(), wasSelected);
    }

    /**
     * Creates a search entry for a typed query (not a suggestion click).
     *
     * @param query the search query
     * @return a new SearchEntry with wasSelected = false
     */
    public static SearchEntry typed(String query) {
        return new SearchEntry(query, System.currentTimeMillis(), false);
    }

    /**
     * Creates a search entry for a selected suggestion.
     *
     * @param query the selected suggestion
     * @return a new SearchEntry with wasSelected = true
     */
    public static SearchEntry selected(String query) {
        return new SearchEntry(query, System.currentTimeMillis(), true);
    }

    public String getQuery() {
        return query;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean wasSelected() {
        return wasSelected;
    }

    @Override
    public String toString() {
        return String.format("SearchEntry{query='%s', time=%d, selected=%s}",
                query, timestamp, wasSelected);
    }
}
