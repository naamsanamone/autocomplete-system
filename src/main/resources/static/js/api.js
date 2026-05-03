/**
 * API client for the Autocomplete System.
 * Handles all REST calls with error handling and request cancellation.
 */
const API = (() => {
    const BASE = '/api';
    let currentController = null;

    /**
     * Cancels any in-flight suggestion request.
     * Uses AbortController — a system design pattern for
     * preventing stale responses from overwriting newer ones.
     */
    function cancelPending() {
        if (currentController) {
            currentController.abort();
            currentController = null;
        }
    }

    async function fetchJSON(url, options = {}) {
        const response = await fetch(url, options);
        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            throw new Error(error.message || `HTTP ${response.status}`);
        }
        return response.json();
    }

    return {
        /**
         * Fetches autocomplete suggestions with request cancellation.
         */
        async suggest(prefix, limit = 5, userId = null) {
            cancelPending();
            currentController = new AbortController();

            let url = `${BASE}/suggest?q=${encodeURIComponent(prefix)}&limit=${limit}`;
            if (userId) url += `&userId=${encodeURIComponent(userId)}`;

            try {
                return await fetchJSON(url, { signal: currentController.signal });
            } catch (err) {
                if (err.name === 'AbortError') return null; // Cancelled
                throw err;
            }
        },

        async search(query) {
            return fetchJSON(`${BASE}/search?q=${encodeURIComponent(query)}`);
        },

        async addWord(word, frequency = 1) {
            return fetchJSON(`${BASE}/corpus`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ word, frequency })
            });
        },

        async removeWord(word) {
            return fetchJSON(`${BASE}/corpus/${encodeURIComponent(word)}`, {
                method: 'DELETE'
            });
        },

        async recordSearch(userId, query, selected = false) {
            return fetchJSON(
                `${BASE}/history/${encodeURIComponent(userId)}?query=${encodeURIComponent(query)}&selected=${selected}`,
                { method: 'POST' }
            );
        },

        async getHistory(userId, limit = 10) {
            return fetchJSON(`${BASE}/history/${encodeURIComponent(userId)}?limit=${limit}`);
        },

        async clearHistory(userId) {
            return fetchJSON(`${BASE}/history/${encodeURIComponent(userId)}`, {
                method: 'DELETE'
            });
        },

        async getTrending(limit = 10) {
            return fetchJSON(`${BASE}/trending?limit=${limit}`);
        },

        async getAnalytics() {
            return fetchJSON(`${BASE}/analytics`);
        },

        cancelPending
    };
})();
