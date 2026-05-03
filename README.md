# Autocomplete Search System 🔍

A production-grade, real-time Autocomplete Search Engine built as a hybrid **Data Structures & Algorithms (DSA)** and **System Design** portfolio project.

This system demonstrates the implementation of custom, high-performance data structures (Trie, Min-Heap) orchestrated by a modern Spring Boot backend and presented with a premium glassmorphism UI.

![Autocomplete UI Demo](.system_generated/click_feedback/click_feedback_1776107642666.png)

## 🚀 Features

*   **Custom Trie Implementation:** Hand-coded $O(L)$ prefix matching engine supporting Unicode and sparse child mapping.
*   **Custom Min-Heap (Top-K Engine):** Hand-coded $O(N \log K)$ Top-K extraction engine to guarantee high performance when ranking thousands of candidates.
*   **Composite Ranking Engine:** Uses the Strategy Pattern to blend multiple scoring dimensions:
    *   *Frequency:* Logarithmic dampening of popular terms.
    *   *Recency:* Inverse time-decay function (Google Trending style).
    *   *Prefix Match:* Tighter bounds scoring (e.g., "jav" → "java" > "javascript").
    *   *Personalization:* Per-user search history boosting.
*   **Real-time Glassmorphism UI:** Stunning frontend with animated gradients, blur effects, prefix highlighting, score bars, and 150ms debouncing.
*   **Live Analytics Dashboard:** Real-time metrics for corpus size, system performance, trending searches, and ranking weights.

## 🛠️ Architecture & Tech Stack

**Backend:**
*   **Java 17 / Spring Boot 3.2**
*   **Design Patterns:** Strategy Pattern (Ranking), Builder Pattern (Context), Factory Methods (History).
*   **Concurrency:** `ConcurrentHashMap` and `AtomicInteger` for lock-free sliding window trending counters.

**Frontend:**
*   **HTML5 / Vanilla CSS3 / Vanilla JS** (Zero dependencies)
*   Glassmorphism UI / UX Micro-interactions
*   `AbortController` for handling API race conditions

## ⚙️ Core Data Structures

### 1. Trie (Prefix Match)
*   **Time Complexity:** Insert/Search: $O(L)$ where $L$ is word length.
*   **Space Complexity:** $O(N \times L)$ sparse storage using `HashMap<Character, TrieNode>`.
*   Includes recursive partial deletion with garbage collection of orphaned nodes.

### 2. Min-Heap (Top-K Extraction)
*   **Time Complexity:** 
    *   Add to heap: $O(\log K)$
    *   Process $N$ items: $O(N \log K)$
*   Limits memory usage by maintaining a constant size $K$, dropping the lowest-scored items immediately.

## 📊 The Ranking Algorithm

The final score for a suggestion is a weighted sum of normalized factors:

```
Score = (0.40 × Frequency) + (0.25 × PrefixMatch) + (0.20 × Recency) + (0.15 × Personalization)
```
*Weights are fully configurable via `application.yml`.*

## 🏃‍♂️ Getting Started

### Prerequisites
*   Java 17 or higher
*   Maven

### Running Locally
1. Clone the repository
2. Navigate to the root directory
3. Run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Access the UI: `http://localhost:8080`
5. Access the Analytics: `http://localhost:8080/analytics`
6. Access API Docs: `http://localhost:8080/swagger-ui.html`

## 👨‍💻 Author
**Sai Venkat**
