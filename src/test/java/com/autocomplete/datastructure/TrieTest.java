package com.autocomplete.datastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the Trie data structure.
 * Covers insert, search, prefix search, delete, and edge cases.
 */
@DisplayName("Trie Data Structure")
class TrieTest {

    private Trie trie;

    @BeforeEach
    void setUp() {
        trie = new Trie();
    }

    @Nested
    @DisplayName("Insert & Search")
    class InsertAndSearch {

        @Test
        @DisplayName("Should insert and find a single word")
        void insertAndSearchSingleWord() {
            trie.insert("java", 100);
            assertTrue(trie.search("java"));
            assertEquals(1, trie.size());
        }

        @Test
        @DisplayName("Should insert multiple words and find all")
        void insertMultipleWords() {
            trie.insert("java", 100);
            trie.insert("javascript", 90);
            trie.insert("python", 80);

            assertTrue(trie.search("java"));
            assertTrue(trie.search("javascript"));
            assertTrue(trie.search("python"));
            assertEquals(3, trie.size());
        }

        @Test
        @DisplayName("Should return false for non-existent word")
        void searchNonExistent() {
            trie.insert("java", 100);
            assertFalse(trie.search("kotlin"));
            assertFalse(trie.search("jav")); // prefix, not complete word
        }

        @Test
        @DisplayName("Should be case-insensitive")
        void caseInsensitive() {
            trie.insert("Java", 100);
            assertTrue(trie.search("java"));
            assertTrue(trie.search("JAVA"));
            assertTrue(trie.search("Java"));
            assertEquals(1, trie.size());
        }

        @Test
        @DisplayName("Should accumulate frequency on duplicate insert")
        void duplicateInsertAccumulatesFrequency() {
            trie.insert("java", 100);
            trie.insert("java", 50);

            assertTrue(trie.search("java"));
            assertEquals(1, trie.size()); // still one word

            TrieNode node = trie.searchNode("java");
            assertNotNull(node);
            assertEquals(150, node.getFrequency()); // 100 + 50
        }

        @Test
        @DisplayName("Should handle empty string gracefully")
        void emptyString() {
            trie.insert("", 10);
            assertEquals(0, trie.size());
            assertFalse(trie.search(""));
        }

        @Test
        @DisplayName("Should handle null word gracefully")
        void nullWord() {
            assertThrows(IllegalArgumentException.class, () -> trie.insert(null, 10));
        }

        @Test
        @DisplayName("Should handle single character words")
        void singleCharacter() {
            trie.insert("a", 10);
            trie.insert("b", 20);

            assertTrue(trie.search("a"));
            assertTrue(trie.search("b"));
            assertEquals(2, trie.size());
        }

        @Test
        @DisplayName("Should store word at terminal node")
        void storeWordAtTerminal() {
            trie.insert("spring boot", 500);
            TrieNode node = trie.searchNode("spring boot");
            assertNotNull(node);
            assertEquals("spring boot", node.getWord());
            assertEquals(500, node.getFrequency());
        }
    }

    @Nested
    @DisplayName("Prefix Search")
    class PrefixSearch {

        @BeforeEach
        void loadWords() {
            trie.insert("java", 100);
            trie.insert("javascript", 90);
            trie.insert("javafx", 50);
            trie.insert("python", 80);
            trie.insert("spring", 70);
            trie.insert("spring boot", 85);
        }

        @Test
        @DisplayName("Should check if prefix exists")
        void startsWith() {
            assertTrue(trie.startsWith("jav"));
            assertTrue(trie.startsWith("java"));
            assertTrue(trie.startsWith("py"));
            assertFalse(trie.startsWith("xyz"));
        }

        @Test
        @DisplayName("Should return all words with prefix")
        void getWordsWithPrefix() {
            List<TrieNode> results = trie.getWordsWithPrefix("jav");
            assertEquals(3, results.size());

            List<String> words = results.stream().map(TrieNode::getWord).sorted().toList();
            assertEquals(List.of("java", "javafx", "javascript"), words);
        }

        @Test
        @DisplayName("Should return words matching exact word as prefix")
        void exactWordAsPrefix() {
            List<TrieNode> results = trie.getWordsWithPrefix("spring");
            assertEquals(2, results.size()); // "spring" and "spring boot"
        }

        @Test
        @DisplayName("Should return empty list for no match")
        void noMatchPrefix() {
            List<TrieNode> results = trie.getWordsWithPrefix("xyz");
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Should limit results with maxResults")
        void limitedPrefixSearch() {
            List<TrieNode> results = trie.getWordsWithPrefix("jav", 2);
            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("Should return all words for empty prefix")
        void emptyPrefix() {
            List<TrieNode> results = trie.getWordsWithPrefix("");
            assertEquals(6, results.size());
        }

        @Test
        @DisplayName("Should handle null prefix")
        void nullPrefix() {
            List<TrieNode> results = trie.getWordsWithPrefix(null);
            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {

        @BeforeEach
        void loadWords() {
            trie.insert("java", 100);
            trie.insert("javascript", 90);
            trie.insert("javafx", 50);
        }

        @Test
        @DisplayName("Should delete a word and reduce count")
        void deleteWord() {
            assertTrue(trie.delete("javafx"));
            assertFalse(trie.search("javafx"));
            assertEquals(2, trie.size());
        }

        @Test
        @DisplayName("Should not affect other words after deletion")
        void deletePreservesOtherWords() {
            trie.delete("javafx");
            assertTrue(trie.search("java"));
            assertTrue(trie.search("javascript"));
        }

        @Test
        @DisplayName("Should handle deleting non-existent word")
        void deleteNonExistent() {
            assertFalse(trie.delete("python"));
            assertEquals(3, trie.size());
        }

        @Test
        @DisplayName("Should handle deleting from shared prefix")
        void deleteSharedPrefix() {
            // "java" is prefix of "javascript" — deleting "java" should keep "javascript"
            trie.delete("java");
            assertFalse(trie.search("java"));
            assertTrue(trie.search("javascript"));
            assertTrue(trie.search("javafx"));
        }

        @Test
        @DisplayName("Should clean up orphaned nodes after deletion")
        void cleanupOrphanedNodes() {
            int nodesBefore = trie.getNodeCount();
            trie.insert("unique", 10);
            trie.delete("unique");
            // Nodes for "unique" should be cleaned up since no other word shares the prefix
            assertTrue(trie.getNodeCount() <= nodesBefore);
        }

        @Test
        @DisplayName("Should handle null and empty delete")
        void nullAndEmptyDelete() {
            assertFalse(trie.delete(null));
            assertFalse(trie.delete(""));
        }
    }

    @Nested
    @DisplayName("Utility Methods")
    class UtilityMethods {

        @Test
        @DisplayName("Should report correct size")
        void size() {
            assertEquals(0, trie.size());
            trie.insert("a", 1);
            assertEquals(1, trie.size());
            trie.insert("b", 1);
            assertEquals(2, trie.size());
        }

        @Test
        @DisplayName("Should report isEmpty correctly")
        void isEmpty() {
            assertTrue(trie.isEmpty());
            trie.insert("test", 1);
            assertFalse(trie.isEmpty());
        }

        @Test
        @DisplayName("Should calculate max depth")
        void maxDepth() {
            trie.insert("a", 1);
            trie.insert("abcdef", 1);
            assertEquals(6, trie.getMaxDepth());
        }

        @Test
        @DisplayName("Should return all words")
        void getAllWords() {
            trie.insert("java", 1);
            trie.insert("python", 1);
            trie.insert("go", 1);

            List<String> words = trie.getAllWords();
            assertEquals(3, words.size());
            assertTrue(words.containsAll(List.of("java", "python", "go")));
        }

        @Test
        @DisplayName("Should update frequency")
        void updateFrequency() {
            trie.insert("java", 100);
            trie.updateFrequency("java", 50);

            TrieNode node = trie.searchNode("java");
            assertEquals(150, node.getFrequency());
        }

        @Test
        @DisplayName("Should track node count")
        void nodeCount() {
            assertEquals(1, trie.getNodeCount()); // root
            trie.insert("ab", 1); // adds 2 nodes: 'a', 'b'
            assertEquals(3, trie.getNodeCount());
        }
    }
}
