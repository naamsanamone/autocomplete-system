package com.autocomplete.config;

import com.autocomplete.datastructure.Trie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for core data structure beans.
 *
 * <p>Registers the Trie as a singleton Spring bean so it can be
 * injected into services and ranking strategies. The Trie is
 * application-scoped (lives for the lifetime of the server).</p>
 *
 * @author Sai Venkat
 */
@Configuration
public class DataStructureConfig {

    /**
     * Creates the singleton Trie instance for the application.
     *
     * <p>All services share this single Trie, which is populated
     * by the CorpusInitializer on startup and modified by the
     * AutocompleteService during runtime.</p>
     *
     * @return the application's Trie instance
     */
    @Bean
    public Trie trie() {
        return new Trie();
    }
}
