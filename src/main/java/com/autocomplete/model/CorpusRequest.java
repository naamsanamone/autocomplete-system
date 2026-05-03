package com.autocomplete.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for adding words to the corpus.
 *
 * @author Sai Venkat
 */
public class CorpusRequest {

    @NotBlank(message = "Word cannot be blank")
    @Size(min = 1, max = 100, message = "Word must be between 1 and 100 characters")
    private String word;

    @Min(value = 0, message = "Frequency must be non-negative")
    private int frequency = 1;

    private String category;

    public CorpusRequest() {}

    public CorpusRequest(String word, int frequency) {
        this.word = word;
        this.frequency = frequency;
    }

    public CorpusRequest(String word, int frequency, String category) {
        this.word = word;
        this.frequency = frequency;
        this.category = category;
    }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public int getFrequency() { return frequency; }
    public void setFrequency(int frequency) { this.frequency = frequency; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
