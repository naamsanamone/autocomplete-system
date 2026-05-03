/**
 * Main application orchestrator.
 * Initializes all modules and manages panels.
 */
const App = (() => {
    let historyListEl = null;
    let trendingListEl = null;
    let statsElements = {};

    function init() {
        // Initialize suggestion dropdown
        const dropdown = document.getElementById('suggestions-dropdown');
        Suggestions.init(dropdown, (word) => SearchBar.selectSuggestion(word));

        // Initialize search bar
        SearchBar.init({
            input: document.getElementById('search-input'),
            container: document.getElementById('search-container'),
            clearBtn: document.getElementById('search-clear'),
            timer: document.getElementById('search-timer'),
            keyboardHint: document.getElementById('keyboard-hint')
        });

        // Cache panel elements
        historyListEl = document.getElementById('history-list');
        trendingListEl = document.getElementById('trending-list');
        statsElements = {
            corpusSize: document.getElementById('stat-corpus'),
            nodeCount: document.getElementById('stat-nodes'),
            avgTime: document.getElementById('stat-avgtime'),
            searches: document.getElementById('stat-searches')
        };

        // Set up panel actions
        const clearHistoryBtn = document.getElementById('clear-history');
        if (clearHistoryBtn) {
            clearHistoryBtn.addEventListener('click', clearHistory);
        }

        // Load initial data
        refreshPanels();
        loadAnalytics();

        // Auto-refresh panels every 5 seconds
        setInterval(refreshPanels, 5000);

        // Focus search input
        document.getElementById('search-input').focus();

        console.log('🔍 Autocomplete System initialized');
    }

    async function refreshPanels() {
        await Promise.all([
            loadHistory(),
            loadTrending()
        ]);
    }

    async function loadHistory() {
        if (!historyListEl) return;
        try {
            const response = await API.getHistory(SearchBar.getUserId(), 6);
            renderHistory(response.searches || []);
        } catch (err) {
            console.error('History error:', err);
        }
    }

    function renderHistory(searches) {
        if (!historyListEl) return;
        if (searches.length === 0) {
            historyListEl.innerHTML = '<div class="panel-empty">No recent searches</div>';
            return;
        }

        historyListEl.innerHTML = searches.map(query => `
            <li class="history-item" onclick="SearchBar.setQuery('${escapeHtml(query)}')">
                <span class="history-icon">🕐</span>
                <span class="history-text">${escapeHtml(query)}</span>
            </li>
        `).join('');
    }

    async function loadTrending() {
        if (!trendingListEl) return;
        try {
            const response = await API.getTrending(6);
            renderTrending(response.trending || []);
        } catch (err) {
            console.error('Trending error:', err);
        }
    }

    function renderTrending(trending) {
        if (!trendingListEl) return;
        if (trending.length === 0) {
            trendingListEl.innerHTML = '<div class="panel-empty">No trending searches yet</div>';
            return;
        }

        trendingListEl.innerHTML = trending.map((item, i) => `
            <li class="trending-item" onclick="SearchBar.setQuery('${escapeHtml(item.query)}')">
                <span class="trending-rank">${i + 1}</span>
                <span class="trending-text">${escapeHtml(item.query)}</span>
                <span class="trending-count">${item.count}</span>
            </li>
        `).join('');
    }

    async function loadAnalytics() {
        try {
            const data = await API.getAnalytics();
            if (statsElements.corpusSize) statsElements.corpusSize.textContent = data.corpusSize;
            if (statsElements.nodeCount) statsElements.nodeCount.textContent = data.trieNodeCount;
            if (statsElements.avgTime) statsElements.avgTime.textContent = data.averageResponseTimeMs.toFixed(1) + 'ms';
            if (statsElements.searches) statsElements.searches.textContent = data.totalSuggestionRequests;
        } catch (err) {
            console.error('Analytics error:', err);
        }
    }

    async function clearHistory() {
        try {
            await API.clearHistory(SearchBar.getUserId());
            renderHistory([]);
        } catch (err) {
            console.error('Clear history error:', err);
        }
    }

    function escapeHtml(str) {
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    return { init, refreshPanels };
})();

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', App.init);
