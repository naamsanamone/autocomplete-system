package com.autocomplete.datastructure;

import java.util.HashMap;
import java.util.Map;

/**
 * A single node in the Trie data structure.
 *
 * <p>Each node represents a character in the Trie and maintains:
 * <ul>
 *   <li>A map of child nodes keyed by character</li>
 *   <li>A flag indicating if this node marks the end of a complete word</li>
 *   <li>The complete word string (stored only at terminal nodes)</li>
 *   <li>Frequency count for popularity-based ranking</li>
 *   <li>Timestamp for recency-based ranking</li>
 * </ul>
 *
 * <p><b>Space per node:</b> O(ALPHABET_SIZE) for the children map,
 * though HashMap only allocates for existing children (sparse storage).
 *
 * @author Sai Venkat
 */
public class TrieNode {

    /**
     * Child nodes mapped by character.
     * Using HashMap instead of fixed-size array for memory efficiency
     * when the alphabet is large (supports Unicode, not just a-z).
     */
    private final Map<Character, TrieNode> children;

    /**
     * True if this node represents the last character of a valid word.
     */
    private boolean endOfWord;

    /**
     * The complete word stored at this terminal node.
     * Only populated when {@code endOfWord} is true.
     */
    private String word;

    /**
     * Base frequency / popularity count for this word.
     * Used by the FrequencyRankingStrategy.
     */
    private int frequency;

    /**
     * Timestamp (epoch millis) of the last time this word was accessed or searched.
     * Used by the RecencyRankingStrategy for time-decay scoring.
     */
    private long lastAccessed;

    public TrieNode() {
        this.children = new HashMap<>();
        this.endOfWord = false;
        this.word = null;
        this.frequency = 0;
        this.lastAccessed = 0;
    }

    // ──────────────────────────────────────────────
    //  Child operations
    // ──────────────────────────────────────────────

    /**
     * Checks if this node has a child for the given character.
     *
     * @param ch the character to check
     * @return true if a child exists for this character
     */
    public boolean hasChild(char ch) {
        return children.containsKey(ch);
    }

    /**
     * Gets the child node for the given character.
     *
     * @param ch the character
     * @return the child TrieNode, or null if not present
     */
    public TrieNode getChild(char ch) {
        return children.get(ch);
    }

    /**
     * Adds or replaces a child node for the given character.
     *
     * @param ch   the character
     * @param node the child TrieNode
     */
    public void putChild(char ch, TrieNode node) {
        children.put(ch, node);
    }

    /**
     * Removes the child node for the given character.
     *
     * @param ch the character to remove
     */
    public void removeChild(char ch) {
        children.remove(ch);
    }

    /**
     * Returns the map of all children.
     *
     * @return unmodifiable view of children
     */
    public Map<Character, TrieNode> getChildren() {
        return children;
    }

    /**
     * Returns the number of children this node has.
     *
     * @return child count
     */
    public int getChildCount() {
        return children.size();
    }

    // ──────────────────────────────────────────────
    //  Word / Terminal node properties
    // ──────────────────────────────────────────────

    public boolean isEndOfWord() {
        return endOfWord;
    }

    public void setEndOfWord(boolean endOfWord) {
        this.endOfWord = endOfWord;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    /**
     * Increments the frequency by the given amount.
     *
     * @param amount the increment value
     */
    public void incrementFrequency(int amount) {
        this.frequency += amount;
    }

    public long getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    /**
     * Updates the last accessed timestamp to the current time.
     */
    public void touch() {
        this.lastAccessed = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "TrieNode{" +
                "children=" + children.size() +
                ", endOfWord=" + endOfWord +
                ", word='" + word + '\'' +
                ", frequency=" + frequency +
                '}';
    }
}
