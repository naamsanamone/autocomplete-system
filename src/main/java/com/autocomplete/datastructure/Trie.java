package com.autocomplete.datastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Trie (prefix tree) data structure for efficient autocomplete.
 *
 * <p>The Trie supports the following operations:
 * <ul>
 *   <li>{@link #insert(String, int)} — O(L) where L = word length</li>
 *   <li>{@link #search(String)} — O(L) exact match lookup</li>
 *   <li>{@link #startsWith(String)} — O(L) prefix existence check</li>
 *   <li>{@link #getWordsWithPrefix(String)} — O(L + N) where N = number of matching words</li>
 *   <li>{@link #delete(String)} — O(L) removal</li>
 * </ul>
 *
 * <h3>Why Trie over HashMap?</h3>
 * <p>A HashMap storing all words would require O(N) to find all words matching
 * a prefix (scan every key). A Trie navigates directly to the prefix node in
 * O(L) and then only visits matching subtrees — far more efficient for
 * autocomplete where prefix search is the dominant operation.</p>
 *
 * <h3>Space Complexity</h3>
 * <p>O(TOTAL_CHARACTERS) across all inserted words. In practice, shared
 * prefixes significantly reduce memory usage compared to storing each
 * word independently.</p>
 *
 * <p><b>Thread Safety:</b> This implementation is NOT thread-safe.
 * For concurrent access, wrap with external synchronization or use
 * a ConcurrentTrie wrapper (discussed as a scaling interview topic).
 *
 * @author Sai Venkat
 */
public class Trie {

    private final TrieNode root;
    private int wordCount;
    private int nodeCount;

    public Trie() {
        this.root = new TrieNode();
        this.wordCount = 0;
        this.nodeCount = 1; // root node
    }

    // ═══════════════════════════════════════════════
    //  INSERT
    // ═══════════════════════════════════════════════

    /**
     * Inserts a word into the Trie with the given frequency.
     *
     * <p><b>Time:</b> O(L) where L = word.length()</p>
     * <p><b>Space:</b> O(L) in the worst case (no shared prefix)</p>
     *
     * <p>If the word already exists, the frequency is added to the
     * existing frequency (accumulative), and the last-accessed
     * timestamp is updated.</p>
     *
     * @param word      the word to insert (case-insensitive, converted to lowercase)
     * @param frequency the base frequency / popularity score
     * @throws IllegalArgumentException if word is null
     */
    public void insert(String word, int frequency) {
        if (word == null) {
            throw new IllegalArgumentException("Word cannot be null");
        }

        String normalized = word.toLowerCase().trim();
        if (normalized.isEmpty()) {
            return;
        }

        TrieNode current = root;

        for (char ch : normalized.toCharArray()) {
            if (!current.hasChild(ch)) {
                current.putChild(ch, new TrieNode());
                nodeCount++;
            }
            current = current.getChild(ch);
        }

        // Mark terminal node
        if (!current.isEndOfWord()) {
            wordCount++;
        }
        current.setEndOfWord(true);
        current.setWord(normalized);
        current.incrementFrequency(frequency);
        current.touch();
    }

    /**
     * Inserts a word with a default frequency of 1.
     *
     * @param word the word to insert
     */
    public void insert(String word) {
        insert(word, 1);
    }

    // ═══════════════════════════════════════════════
    //  SEARCH (Exact Match)
    // ═══════════════════════════════════════════════

    /**
     * Searches for an exact word in the Trie.
     *
     * <p><b>Time:</b> O(L) where L = word.length()</p>
     *
     * @param word the word to search for
     * @return true if the exact word exists in the Trie
     */
    public boolean search(String word) {
        TrieNode node = findNode(word);
        return node != null && node.isEndOfWord();
    }

    /**
     * Searches for a word and returns its TrieNode if found.
     *
     * @param word the word to search for
     * @return the terminal TrieNode, or null if word not found
     */
    public TrieNode searchNode(String word) {
        TrieNode node = findNode(word);
        return (node != null && node.isEndOfWord()) ? node : null;
    }

    // ═══════════════════════════════════════════════
    //  PREFIX SEARCH
    // ═══════════════════════════════════════════════

    /**
     * Checks if any word in the Trie starts with the given prefix.
     *
     * <p><b>Time:</b> O(L) where L = prefix.length()</p>
     *
     * @param prefix the prefix to check
     * @return true if at least one word starts with this prefix
     */
    public boolean startsWith(String prefix) {
        return findNode(prefix) != null;
    }

    /**
     * Returns all words in the Trie that start with the given prefix.
     *
     * <p><b>Time:</b> O(L + N) where L = prefix length, N = number of
     * matching words in the subtree.</p>
     *
     * <p>Uses DFS traversal from the prefix node to collect all
     * terminal nodes in the subtree.</p>
     *
     * @param prefix the prefix to search for
     * @return list of all matching words (empty if no matches)
     */
    public List<TrieNode> getWordsWithPrefix(String prefix) {
        if (prefix == null) {
            return Collections.emptyList();
        }

        String normalized = prefix.toLowerCase().trim();
        if (normalized.isEmpty()) {
            // Return all words in the Trie
            List<TrieNode> results = new ArrayList<>();
            collectAllWords(root, results);
            return results;
        }

        TrieNode prefixNode = findNode(normalized);
        if (prefixNode == null) {
            return Collections.emptyList();
        }

        List<TrieNode> results = new ArrayList<>();
        collectAllWords(prefixNode, results);
        return results;
    }

    /**
     * Returns all words in the Trie that start with the given prefix,
     * limited to a maximum count. This is more efficient than
     * {@link #getWordsWithPrefix(String)} when only a few results are needed.
     *
     * <p><b>Time:</b> O(L + M) where M = maxResults (early termination)</p>
     *
     * @param prefix     the prefix to search for
     * @param maxResults maximum number of results to return
     * @return list of matching words, up to maxResults
     */
    public List<TrieNode> getWordsWithPrefix(String prefix, int maxResults) {
        if (prefix == null || maxResults <= 0) {
            return Collections.emptyList();
        }

        String normalized = prefix.toLowerCase().trim();
        TrieNode prefixNode;

        if (normalized.isEmpty()) {
            prefixNode = root;
        } else {
            prefixNode = findNode(normalized);
        }

        if (prefixNode == null) {
            return Collections.emptyList();
        }

        List<TrieNode> results = new ArrayList<>();
        collectWordsLimited(prefixNode, results, maxResults);
        return results;
    }

    // ═══════════════════════════════════════════════
    //  DELETE
    // ═══════════════════════════════════════════════

    /**
     * Deletes a word from the Trie.
     *
     * <p><b>Time:</b> O(L) where L = word.length()</p>
     *
     * <p>Uses recursive deletion: removes nodes bottom-up only if they
     * have no other children and are not terminal nodes for other words.
     * This prevents orphaned branches while preserving shared prefixes.</p>
     *
     * @param word the word to delete
     * @return true if the word was found and deleted
     */
    public boolean delete(String word) {
        if (word == null) {
            return false;
        }

        String normalized = word.toLowerCase().trim();
        if (normalized.isEmpty()) {
            return false;
        }

        boolean[] deleted = {false};
        deleteRecursive(root, normalized, 0, deleted);

        if (deleted[0]) {
            wordCount--;
        }
        return deleted[0];
    }

    /**
     * Recursive deletion helper.
     *
     * <p>Returns true if the current node should be removed by its parent
     * (i.e., it has no children and is not a terminal node).</p>
     */
    private boolean deleteRecursive(TrieNode current, String word, int index, boolean[] deleted) {
        if (index == word.length()) {
            // Reached the end of the word
            if (!current.isEndOfWord()) {
                return false; // Word doesn't exist
            }
            deleted[0] = true;
            current.setEndOfWord(false);
            current.setWord(null);
            current.setFrequency(0);
            current.setLastAccessed(0);

            // If no children, this node can be removed
            return current.getChildCount() == 0;
        }

        char ch = word.charAt(index);
        TrieNode child = current.getChild(ch);

        if (child == null) {
            return false; // Word doesn't exist
        }

        boolean shouldDeleteChild = deleteRecursive(child, word, index + 1, deleted);

        if (shouldDeleteChild) {
            current.removeChild(ch);
            nodeCount--;
            // Current node can be removed if it has no children and isn't terminal
            return current.getChildCount() == 0 && !current.isEndOfWord();
        }

        return false;
    }

    // ═══════════════════════════════════════════════
    //  UTILITY METHODS
    // ═══════════════════════════════════════════════

    /**
     * Returns the total number of words stored in the Trie.
     *
     * @return word count
     */
    public int size() {
        return wordCount;
    }

    /**
     * Returns the total number of nodes in the Trie.
     * Useful for memory analysis and analytics dashboard.
     *
     * @return node count (including root)
     */
    public int getNodeCount() {
        return nodeCount;
    }

    /**
     * Checks if the Trie is empty.
     *
     * @return true if no words are stored
     */
    public boolean isEmpty() {
        return wordCount == 0;
    }

    /**
     * Returns the maximum depth of the Trie (length of the longest word).
     * Useful for performance analytics.
     *
     * @return max depth
     */
    public int getMaxDepth() {
        return calculateMaxDepth(root);
    }

    /**
     * Returns all words stored in the Trie.
     *
     * @return list of all words
     */
    public List<String> getAllWords() {
        List<TrieNode> nodes = new ArrayList<>();
        collectAllWords(root, nodes);
        return nodes.stream()
                .map(TrieNode::getWord)
                .toList();
    }

    /**
     * Returns the root node of the Trie.
     * Used for visualization and testing.
     *
     * @return root TrieNode
     */
    public TrieNode getRoot() {
        return root;
    }

    /**
     * Updates the frequency of a word. If the word doesn't exist, does nothing.
     *
     * @param word      the word to update
     * @param frequency the new frequency value to add
     */
    public void updateFrequency(String word, int frequency) {
        TrieNode node = searchNode(word);
        if (node != null) {
            node.incrementFrequency(frequency);
            node.touch();
        }
    }

    /**
     * Touches a word (updates its last-accessed timestamp).
     *
     * @param word the word to touch
     */
    public void touchWord(String word) {
        TrieNode node = searchNode(word);
        if (node != null) {
            node.touch();
        }
    }

    // ═══════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════

    /**
     * Navigates the Trie to find the node for the given string.
     *
     * @param str the string to navigate to
     * @return the node at the end of the string path, or null if path doesn't exist
     */
    private TrieNode findNode(String str) {
        if (str == null) {
            return null;
        }

        String normalized = str.toLowerCase().trim();
        TrieNode current = root;

        for (char ch : normalized.toCharArray()) {
            if (!current.hasChild(ch)) {
                return null;
            }
            current = current.getChild(ch);
        }

        return current;
    }

    /**
     * DFS traversal to collect all terminal nodes in a subtree.
     *
     * @param node    the starting node
     * @param results the list to collect results into
     */
    private void collectAllWords(TrieNode node, List<TrieNode> results) {
        if (node.isEndOfWord()) {
            results.add(node);
        }

        for (TrieNode child : node.getChildren().values()) {
            collectAllWords(child, results);
        }
    }

    /**
     * DFS traversal with early termination after collecting enough results.
     *
     * @param node       the starting node
     * @param results    the list to collect results into
     * @param maxResults stop collecting after this many results
     */
    private void collectWordsLimited(TrieNode node, List<TrieNode> results, int maxResults) {
        if (results.size() >= maxResults) {
            return;
        }

        if (node.isEndOfWord()) {
            results.add(node);
        }

        for (TrieNode child : node.getChildren().values()) {
            if (results.size() >= maxResults) {
                return;
            }
            collectWordsLimited(child, results, maxResults);
        }
    }

    /**
     * Recursively calculates the maximum depth of the Trie.
     *
     * @param node the current node
     * @return max depth from this node
     */
    private int calculateMaxDepth(TrieNode node) {
        if (node.getChildCount() == 0) {
            return 0;
        }

        int maxChildDepth = 0;
        for (TrieNode child : node.getChildren().values()) {
            maxChildDepth = Math.max(maxChildDepth, calculateMaxDepth(child));
        }
        return 1 + maxChildDepth;
    }
}
