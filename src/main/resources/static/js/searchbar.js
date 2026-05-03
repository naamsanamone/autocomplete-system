/**
 * Search bar controller.
 * Handles debounced input, keyboard navigation, and search state.
 */
const SearchBar = (() => {
    let inputEl = null;
    let containerEl = null;
    let clearBtn = null;
    let timerEl = null;
    let keyboardHintEl = null;

    let debounceTimer = null;
    let selectedIndex = -1;
    const DEBOUNCE_MS = 150;

    const userId = 'user_' + Math.random().toString(36).substring(2, 8);

    function init(config) {
        inputEl = config.input;
        containerEl = config.container;
        clearBtn = config.clearBtn;
        timerEl = config.timer;
        keyboardHintEl = config.keyboardHint;

        // Focus/blur events
        inputEl.addEventListener('focus', () => {
            containerEl.classList.add('focused');
            if (keyboardHintEl) keyboardHintEl.classList.add('visible');
        });

        inputEl.addEventListener('blur', () => {
            containerEl.classList.remove('focused');
            if (keyboardHintEl) keyboardHintEl.classList.remove('visible');
            // Delay hide to allow click events on suggestions
            setTimeout(() => Suggestions.hide(), 200);
        });

        // Input event (debounced)
        inputEl.addEventListener('input', onInput);

        // Keyboard navigation
        inputEl.addEventListener('keydown', onKeydown);

        // Clear button
        if (clearBtn) {
            clearBtn.addEventListener('click', () => {
                inputEl.value = '';
                clearBtn.classList.remove('visible');
                if (timerEl) timerEl.classList.remove('visible');
                Suggestions.hide();
                selectedIndex = -1;
                inputEl.focus();
            });
        }

        // Click outside to close
        document.addEventListener('click', (e) => {
            if (!containerEl.contains(e.target) && !e.target.closest('.suggestions-dropdown')) {
                Suggestions.hide();
            }
        });
    }

    function onInput() {
        const value = inputEl.value.trim();

        // Show/hide clear button
        if (clearBtn) {
            clearBtn.classList.toggle('visible', value.length > 0);
        }

        // Reset selection
        selectedIndex = -1;

        // Clear previous timer
        if (debounceTimer) clearTimeout(debounceTimer);

        if (value.length === 0) {
            Suggestions.hide();
            if (timerEl) timerEl.classList.remove('visible');
            return;
        }

        // Show loading
        Suggestions.renderLoading();

        // Debounce the API call
        debounceTimer = setTimeout(() => fetchSuggestions(value), DEBOUNCE_MS);
    }

    async function fetchSuggestions(prefix) {
        try {
            const response = await API.suggest(prefix, 8, userId);
            if (!response) return; // Cancelled

            // Render suggestions
            Suggestions.render(response, prefix);

            // Update timer
            if (timerEl) {
                timerEl.textContent = `${response.responseTimeMs.toFixed(1)}ms`;
                timerEl.classList.add('visible');
            }
        } catch (err) {
            console.error('Suggest error:', err);
        }
    }

    function onKeydown(e) {
        if (!Suggestions.isOpen()) return;

        const count = Suggestions.getItemCount();
        if (count === 0) return;

        switch (e.key) {
            case 'ArrowDown':
                e.preventDefault();
                selectedIndex = (selectedIndex + 1) % count;
                Suggestions.setSelected(selectedIndex);
                break;

            case 'ArrowUp':
                e.preventDefault();
                selectedIndex = selectedIndex <= 0 ? count - 1 : selectedIndex - 1;
                Suggestions.setSelected(selectedIndex);
                break;

            case 'Enter':
                e.preventDefault();
                if (selectedIndex >= 0) {
                    const word = Suggestions.getSelectedWord(selectedIndex);
                    if (word) selectSuggestion(word);
                }
                break;

            case 'Escape':
                e.preventDefault();
                Suggestions.hide();
                selectedIndex = -1;
                break;
        }
    }

    async function selectSuggestion(word) {
        inputEl.value = word;
        Suggestions.hide();
        selectedIndex = -1;
        if (clearBtn) clearBtn.classList.add('visible');

        // Record the search
        try {
            await API.recordSearch(userId, word, true);
            // Refresh panels
            if (typeof App !== 'undefined') {
                App.refreshPanels();
            }
        } catch (err) {
            console.error('Record search error:', err);
        }
    }

    function getUserId() {
        return userId;
    }

    function setQuery(query) {
        inputEl.value = query;
        if (clearBtn) clearBtn.classList.toggle('visible', query.length > 0);
        onInput();
    }

    return { init, selectSuggestion, getUserId, setQuery };
})();
