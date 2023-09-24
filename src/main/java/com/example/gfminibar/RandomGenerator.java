package com.example.gfminibar;

import org.grammaticalframework.pgf.PGF;

import java.util.List;

public class RandomGenerator {
    private PGF pgf;
    private GrammarManager gManager;

    /**
     * Constructor initializes the RandomGenerator with a GrammarManager instance.
     *
     * @param gm An instance of GrammarManager.
     */
    public RandomGenerator(GrammarManager gm) {
        pgf = gm.getGrammar();
        gManager = gm;
    }

    /**
     * Generates a random sentence based on a language and category.
     *
     * @param language  The language in which the sentence should be generated.
     * @param category  The grammatical category to use.
     * @return A randomly generated sentence.
     */
    public String generate(String language, String category) {
        String sentence = "";
        String chosen;
        int wordCount = 0;  // Counter for the number of words in the sentence

        // Initialize with a random word from the category
        List<String> words = gManager.loadWords(language, category, "");
        chosen = select(words);
        sentence += chosen + " ";
        wordCount++;

        // Continue to append words until an empty word is returned or the word count limit is reached
        while (!chosen.equals("") && wordCount < 10) {
            words = gManager.loadWords(language, pgf.getStartCat(), sentence);
            chosen = select(words);
            sentence += chosen + " ";
            wordCount++;
        }

        // Validate the sentence; if invalid, regenerate
        if (!validate(sentence, language)) {
            sentence = generate(language, category);
        }

        return sentence;
    }

    /**
     * Randomly selects a word from a list of words.
     *
     * @param words The list of words.
     * @return A randomly selected word.
     */
    private String select(List<String> words) {
        if (words.isEmpty()) {
            return "";
        }
        return words.get((int) (Math.random() * words.size()));
    }

    /**
     * Validates if a generated sentence can be translated.
     *
     * @param s        The sentence to validate.
     * @param language The language of the sentence.
     * @return True if the sentence can be translated, false otherwise.
     */
    private boolean validate(String s, String language) {
        return gManager.getTranslation(s, language, "All") != null;
    }
}
