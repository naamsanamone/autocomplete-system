/**
 * Suggestions dropdown renderer.
 * Handles rendering, highlighting, and animation of suggestion items.
 */
const Suggestions = (() => {
    let dropdownEl = null;
    let onSelectCallback = null;

    function init(dropdown, onSelect) {
        dropdownEl = dropdown;
        onSelectCallback = onSelect;
    }

    /**
     * Renders the suggestion dropdown.
     * @param {Object} response - SuggestionResponse from the API
     * @param {string} prefix - The current search prefix
     */
    function render(response, prefix) {
        if (!dropdownEl) return;

        if (!response || !response.suggestions || response.suggestions.length === 0) {
            renderNoResults(prefix);
            return;
        }

        const maxScore = response.suggestions.length > 0
            ? Math.max(...response.suggestions.map(s => s.score))
            : 1;

        let html = `
            <div class="suggestion-header">
                <span class="suggestion-label">Suggestions</span>
                <span class="suggestion-count">${response.returnedCount} of ${response.totalMatches}</span>
            </div>
        `;

        response.suggestions.forEach((suggestion, index) => {
            const highlighted = highlightMatch(suggestion.word, prefix);
            const scorePercent = maxScore > 0 ? (suggestion.score / maxScore) * 100 : 0;
            const breakdownHtml = renderScoreBreakdown(suggestion.scoreBreakdown);

            html += `
                <div class="suggestion-item fade-in" 
                     data-index="${index}" 
                     data-word="${escapeHtml(suggestion.word)}"
                     style="animation-delay: ${index * 30}ms"
                     onclick="Suggestions.select(${index})">
                    <span class="suggestion-icon">🔍</span>
                    <div class="suggestion-content">
                        <div class="suggestion-word">${highlighted}</div>
                    </div>
                    <div class="suggestion-meta">
                        <span class="suggestion-freq">⚡${suggestion.frequency}</span>
                        <div class="score-bar-container">
                            <div class="score-bar" style="width: ${scorePercent}%"></div>
                        </div>
                        <span class="suggestion-score">${suggestion.score.toFixed(3)}</span>
                    </div>
                    <div class="score-tooltip">${breakdownHtml}</div>
                </div>
            `;
        });

        dropdownEl.innerHTML = html;
        dropdownEl.classList.add('open');
    }

    function renderNoResults(prefix) {
        dropdownEl.innerHTML = `
            <div class="no-results">
                <div class="no-results-icon">🔎</div>
                <div>No suggestions found for "<strong>${escapeHtml(prefix)}</strong>"</div>
            </div>
        `;
        dropdownEl.classList.add('open');
    }

    function renderLoading() {
        if (!dropdownEl) return;
        dropdownEl.innerHTML = `
            <div class="typing-indicator">
                <div class="typing-dot"></div>
                <div class="typing-dot"></div>
                <div class="typing-dot"></div>
            </div>
        `;
        dropdownEl.classList.add('open');
    }

    function hide() {
        if (dropdownEl) {
            dropdownEl.classList.remove('open');
        }
    }

    function select(index) {
        const items = dropdownEl.querySelectorAll('.suggestion-item');
        if (index >= 0 && index < items.length) {
            const word = items[index].dataset.word;
            if (onSelectCallback) onSelectCallback(word);
        }
    }

    /**
     * Highlights the matching prefix portion in bold.
     */
    function highlightMatch(word, prefix) {
        const normalizedWord = word.toLowerCase();
        const normalizedPrefix = prefix.toLowerCase();

        if (normalizedWord.startsWith(normalizedPrefix)) {
            const matched = word.substring(0, prefix.length);
            const rest = word.substring(prefix.length);
            return `<span class="match">${escapeHtml(matched)}</span>${escapeHtml(rest)}`;
        }
        return escapeHtml(word);
    }

    function renderScoreBreakdown(breakdown) {
        if (!breakdown) return '';
        let html = '';
        for (const [key, value] of Object.entries(breakdown)) {
            const label = key.replace(/_/g, ' ');
            html += `
                <div class="score-tooltip-row">
                    <span class="score-tooltip-label">${label}</span>
                    <span class="score-tooltip-value">${value.toFixed(3)}</span>
                </div>
            `;
        }
        return html;
    }

    /**
     * Manages keyboard selection state.
     */
    function getItemCount() {
        return dropdownEl ? dropdownEl.querySelectorAll('.suggestion-item').length : 0;
    }

    function setSelected(index) {
        if (!dropdownEl) return;
        const items = dropdownEl.querySelectorAll('.suggestion-item');
        items.forEach(item => item.classList.remove('selected'));
        if (index >= 0 && index < items.length) {
            items[index].classList.add('selected');
            items[index].scrollIntoView({ block: 'nearest' });
        }
    }

    function getSelectedWord(index) {
        if (!dropdownEl) return null;
        const items = dropdownEl.querySelectorAll('.suggestion-item');
        return index >= 0 && index < items.length ? items[index].dataset.word : null;
    }

    function isOpen() {
        return dropdownEl && dropdownEl.classList.contains('open');
    }

    function escapeHtml(str) {
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    return { init, render, renderLoading, hide, select, setSelected, getSelectedWord, getItemCount, isOpen };
})();
