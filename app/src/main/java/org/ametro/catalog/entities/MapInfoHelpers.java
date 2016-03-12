package org.ametro.catalog.entities;


import java.util.ArrayList;

public class MapInfoHelpers {

    public static ArrayList<String> toFileNameArray(MapInfo[] maps){
        ArrayList<String> fileNames = new ArrayList<>();
        for (MapInfo m : maps) {
            fileNames.add(m.getFileName());
        }
        return fileNames;
    }
}
