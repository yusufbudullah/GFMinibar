package com.example.gfminibar;

import org.grammaticalframework.pgf.*;
import java.util.List;
import java.util.Map;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;

public class GrammarManager {
    private String pgfName;
    private  PGF defaultPGF;
    private Map<String, Concr> langs;

    public GrammarManager(){
    }

    //Changes the default grammar to the selected one.
    public void updateGrammar(String pgfName, String pgfPath){
        loadGrammar(pgfName,pgfPath);

    }
    public void loadGrammar(String fileName, String filePath){
        try{
            defaultPGF = PGF.readPGF(filePath);
            pgfName = fileName.replace(".pgf", "");
            langs = defaultPGF.getLanguages();
        }
         catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public PGF getGrammar(){
        return defaultPGF;
    }


    //Listing languages
    public List<String> getLanguages(){
        Object[] languages = langs.keySet().toArray();
        ArrayList<String> newLangs = new ArrayList<String>();
        for(int i = 0; i< languages.length ;i++){
            String word = languages[i].toString();
            newLangs.add(word.substring(word.length()-3)); //applicable only if the languages are stored as e.gFoodsEng, FoodsSwe
        }
        return newLangs;
    }
    public List<String> getCategories(){
       return defaultPGF.getCategories();
    }

    public List<String> loadWords(String from, String cat, String userInput){
        try{
            ArrayList<String> words = new ArrayList<String>();
            Concr languageX = langs.get(pgfName+from);
            Iterable<TokenProb> tokens = languageX.complete(cat, userInput, ""); //figure out how you can change this dynamically for prefix when user types.

        for(TokenProb tp: tokens){
            words.add(tp.getToken());

        }
        return words;

        }
        catch (ParseError e) {
            throw new RuntimeException(e);
        }
        //return an arrayList of strings once you're done testing. **note to self
    }

    public List<String> getTranslation(String sentence,String from, String to) {//throws ParseError {

        try {
            ArrayList<String> translation = new ArrayList<String>(); // list to store the final translations.
            Concr languageX = langs.get(pgfName + from);
            Iterator<ExprProb> probEx = languageX.parse(defaultPGF.getStartCat(), sentence).iterator();

            while (probEx.hasNext()) {
                Expr exp = probEx.next().getExpr();
                //translating to all the languages
                if (to.equals("All")) {
                    Iterator<String> langNames = langs.keySet().iterator();
                    while (langNames.hasNext()) {
                        String langName = langNames.next();
                        Concr langGrammar = defaultPGF.getLanguages().get(langName);
                        translation.add(langName.replace(pgfName, "") + " = " + langGrammar.linearize(exp));
                    }

                }
                //translating to one language
                else {
                    Concr langGrammar = defaultPGF.getLanguages().get(pgfName + to);
                    translation.add(to + " = " + langGrammar.linearize(exp));

                }
            }
            return translation;
        }
         catch (ParseError e) {
            System.out.println(); //Either sentence is not complete or token can not be resolved.
        }

        return null;
    }

    //in progress
    public void tree(String sentence, String from) throws ParseError {

        Concr languageX = langs.get(pgfName+from);
        Iterator<ExprProb> probEx = languageX.parse(defaultPGF.getStartCat(), sentence).iterator();

        while(probEx.hasNext()) {
            Expr exp = probEx.next().getExpr();
            System.out.println(defaultPGF.graphvizAbstractTree(exp));
        }


    }



}
