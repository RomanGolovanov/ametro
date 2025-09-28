package io.github.romangolovanov.apps.ametro.model.entities;

import java.util.HashMap;
import java.util.List;

public class MapLocale {

    private final HashMap<Integer,String> texts;

    private final HashMap<Integer,List<String>> allTexts;

    public MapLocale(HashMap<Integer, String> texts,HashMap<Integer, List<String>> allTexts) {
        this.texts = texts;
        this.allTexts = allTexts;
    }

    public String getText(int textId) {
        return texts.get(textId);
    }

    public List<String> getAllTexts(int textId) {return allTexts.get(textId);}
}
