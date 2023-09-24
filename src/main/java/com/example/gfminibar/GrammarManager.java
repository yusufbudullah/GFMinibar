package com.example.gfminibar;

import org.grammaticalframework.pgf.*;

import java.io.FileNotFoundException;
import java.util.*;

public class GrammarManager {
    private String pgfName;
    private PGF defaultPGF;
    private Map<String, Concr> langs;

    /**
     * Default constructor.
     */
    public GrammarManager() {
        // Intentionally left empty
    }

    /**
     * Changes the default grammar to the selected one.
     *
     * @param pgfName  The name of the PGF.
     * @param pgfPath  The path to the PGF.
     */
    public void updateGrammar(String pgfName, String pgfPath) {
        loadGrammar(pgfName, pgfPath);
    }

    /**
     * Loads a grammar given its filename and filepath.
     *
     * @param fileName  The name of the file.
     * @param filePath  The path to the file.
     */
    public void loadGrammar(String fileName, String filePath) {
        try {
            defaultPGF = PGF.readPGF(filePath);
            pgfName = fileName.replace(".pgf", "");
            langs = defaultPGF.getLanguages();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the currently loaded grammar.
     *
     * @return The currently loaded PGF object.
     */
    public PGF getGrammar() {
        return defaultPGF;
    }

    /**
     * Lists available languages.
     *
     * @return A list of language names.
     */
    public List<String> getLanguages() {
        Object[] languages = langs.keySet().toArray();
        ArrayList<String> newLangs = new ArrayList<>();
        for (Object language : languages) {
            String word = language.toString();
            newLangs.add(word.substring(word.length() - 3)); // Assumes language names end with a 3-character suffix
        }
        return newLangs;
    }

    /**
     * Lists available categories.
     *
     * @return A list of category names.
     */
    public List<String> getCategories() {
        return defaultPGF.getCategories();
    }

    /**
     * Loads words based on the given parameters.
     *
     * @param from       Source language.
     * @param cat        Category.
     * @param userInput  User input for predictions.
     * @return A list of suggested words.
     */
    public List<String> loadWords(String from, String cat, String userInput) {
        try {
            ArrayList<String> words = new ArrayList<>();
            Concr languageX = langs.get(pgfName + from);
            Iterable<TokenProb> tokens = languageX.complete(cat, userInput, "");
            for (TokenProb tp : tokens) {
                words.add(tp.getToken());
            }
            return duplicateSearch(words);
        } catch (ParseError e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Translates a sentence from one language to another.
     *
     * @param sentence   The sentence to translate.
     * @param from       Source language.
     * @param to         Target language.
     * @return A list of translations.
     */
    public List<String> getTranslation(String sentence, String from, String to) {
        try {
            ArrayList<String> translation = new ArrayList<>();
            Concr languageX = langs.get(pgfName + from);
            Iterator<ExprProb> probEx = languageX.parse(defaultPGF.getStartCat(), sentence).iterator();

            while (probEx.hasNext()) {
                Expr exp = probEx.next().getExpr();
                if (to.equals("All")) {
                    for (String langName : langs.keySet()) {
                        Concr langGrammar = langs.get(langName);
                        translation.add(langName.replace(pgfName, "") + " = " + langGrammar.linearize(exp));
                    }
                } else {
                    Concr langGrammar = langs.get(pgfName + to);
                    translation.add(to + " = " + langGrammar.linearize(exp));
                }
            }
            return translation;
        } catch (ParseError e) {
            // Exception handling
            System.out.println("Parse error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Filters out duplicate words from a list.
     *
     * @param list  The list of words.
     * @return A list without duplicate words.
     */
    public List<String> duplicateSearch(ArrayList<String> list) {
        ArrayList<String> cleanList = new ArrayList<>();
        for (String item : list) {
            if (!cleanList.contains(item)) {
                cleanList.add(item);
            }
        }
        return cleanList;
    }

    /**
     * Generates and prints an abstract tree for a sentence.
     *
     * @param sentence  The sentence to generate a tree for.
     * @param from      The source language.
     * @throws ParseError when the parsing fails.
     */
    public void tree(String sentence, String from) throws ParseError {
        Concr languageX = langs.get(pgfName + from);
        Iterator<ExprProb> probEx = languageX.parse(defaultPGF.getStartCat(), sentence).iterator();
        while (probEx.hasNext()) {
            Expr exp = probEx.next().getExpr();
            System.out.println(defaultPGF.graphvizAbstractTree(exp));
        }
    }
}
