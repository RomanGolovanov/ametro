package org.ametro.model.entities;

import java.util.HashMap;

public class MapLocale {

    private final HashMap<Integer,String> texts;

    public MapLocale(HashMap<Integer, String> texts) {
        this.texts = texts;
    }

    public String getText(int textId) {
        return texts.get(textId);
    }
}
