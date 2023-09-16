package com.example.gfminibar;

import com.example.gfminibar.GrammarManager;
import org.grammaticalframework.pgf.*;

import java.util.ArrayList;
import java.util.List;

public class RandomGenerator{
    private String random;
    private PGF pgf;
    private GrammarManager gManager;

    public RandomGenerator(GrammarManager gm){
        pgf = gm.getGrammar();
        gManager = gm;
    }

    public String generate(String language,String category){

            String sentence = "";
            List<String> words = gManager.loadWords(language, category, ""); //
            String chosen = select(words);
            sentence = sentence + chosen + " ";

            while(!chosen.equals("")){

                words = gManager.loadWords(language, pgf.getStartCat(), sentence);
                chosen = select(words);
                sentence = sentence + chosen + " ";
            }
            return sentence;

    }

    //selects a random word in a given array
    private String select(List<String> words){
        String word="";
        if(words.size() != 0){
         word = words.get((int) (Math.random() * words.size()));}

        else{}

        return word;
    }
}
