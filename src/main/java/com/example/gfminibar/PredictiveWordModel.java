package com.example.gfminibar;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.*;

/**
 * PredictiveWordModel is a class responsible for training an N-gram model
 * and providing predictions based on the model.
 */
public class PredictiveWordModel {
    private Table<String, String, Integer> nGramCounts;
    private Map<String, Double> nGramProbabilities;
    private List<String> corpus;

    /**
     * Constructor to initialize member variables.
     */
    public PredictiveWordModel() {
        nGramCounts = HashBasedTable.create();
        nGramProbabilities = new HashMap<>();
        corpus = new ArrayList<>();
    }

    /**
     * Trains the N-gram model based on a given corpus and N value.
     *
     * @param corpus List of sentences used for training.
     * @param n      Size of the N-gram.
     */
    public void train(List<String> corpus, int n) {
        // Initialize nGramCounts if null
        if (nGramCounts == null) {
            nGramCounts = HashBasedTable.create();
        }

        // Counting the n-grams in the corpus
        for (String sentence : corpus) {
            String[] words = sentence.split(" ");
            for (int i = 0; i < words.length - n + 1; i++) {
                StringBuilder nGramBuilder = new StringBuilder();
                for (int j = 0; j < n; j++) {
                    nGramBuilder.append(words[i + j]);
                    if (j < n - 1) {
                        nGramBuilder.append(" ");
                    }
                }
                String nGram = nGramBuilder.toString();
                String prefix = nGram.substring(0, n - 1);
                Integer count = nGramCounts.get(prefix, nGram);
                if (count == null) {
                    count = 0;
                }
                nGramCounts.put(prefix, nGram, count + 1);
            }
        }

        // Calculate the probabilities of each n-gram
        for (Table.Cell<String, String, Integer> cell : nGramCounts.cellSet()) {
            String prefix = cell.getRowKey();
            int totalCount = nGramCounts.row(prefix).values().stream().mapToInt(Integer::intValue).sum();
            double probability = (double) cell.getValue() / totalCount;
            nGramProbabilities.put(cell.getColumnKey(), probability);
        }
    }

    /**
     * Predicts a list of possible next words based on a given prefix.
     *
     * @param prefix The prefix for which to generate predictions.
     * @return A sorted list of predicted words.
     */
    public List<String> predictWordsStartingWith(String prefix) {
        List<String> predictions = new ArrayList<>();

        // Populate the predictions list
        for (Map.Entry<String, Double> entry : nGramProbabilities.entrySet()) {
            String nGram = entry.getKey();
            if (nGram.startsWith(prefix)) {
                predictions.add(nGram);
            }
        }

        // Sort predictions by their probabilities
        predictions.sort((nGram1, nGram2) -> -Double.compare(nGramProbabilities.get(nGram1), nGramProbabilities.get(nGram2)));

        return predictions;
    }

    /**
     * Predicts a list of possible next words for an incomplete sentence.
     *
     * @param incompleteSentence The incomplete sentence for which to generate predictions.
     * @return A list of predicted words.
     */
    public List<String> predictWordsForIncompleteSentence(String incompleteSentence) {
        String[] words = incompleteSentence.split(" ");
        if (words.length == 0) {
            return null; // Return null for an empty sentence
        }

        List<String> predictions = new ArrayList<>();

        // Try to match the incomplete sentence with existing sentences in the corpus
        for (String sentence : corpus) {
            String[] sentenceWords = sentence.split(" ");
            int sentenceLength = sentenceWords.length;
            if (sentenceLength <= words.length) {
                continue;
            }

            boolean matches = true;
            for (int i = 0; i < words.length; i++) {
                if (!sentenceWords[sentenceLength - words.length + i].equals(words[i])) {
                    matches = false;
                    break;
                }
            }

            if (matches && sentenceLength > words.length) {
                String prediction = sentenceWords[sentenceLength - 1];
                predictions.add(prediction);
            }
        }

        // Fall back to the N-gram model if no matches in the corpus
        if (predictions.isEmpty()) {
            String lastWord = words[words.length - 1];
            predictions = predictWordsStartingWith(lastWord);
        }

        // Format the predictions
        List<String> resultPredictions = new ArrayList<>();
        for (String prediction : predictions) {
            String[] predictionWords = prediction.split(" ");
            if (predictionWords.length > 1) {
                resultPredictions.add(predictionWords[1]); // Assuming bigrams
            }
        }

        return resultPredictions.isEmpty() ? null : resultPredictions;
    }

    /**
     * Adds a sentence to the corpus and retrains the model.
     *
     * @param sentence The sentence to be added to the corpus.
     */
    public void addSentenceToCorpus(String sentence) {
        if (corpus == null) {
            corpus = new ArrayList<>();
        }
        corpus.add(sentence);
        train(corpus, 2); // Retraining the model with bigrams
    }
}
