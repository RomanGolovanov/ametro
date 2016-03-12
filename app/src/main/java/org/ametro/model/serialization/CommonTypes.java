package org.ametro.model.serialization;

import android.graphics.Color;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;

import org.ametro.app.Constants;
import org.ametro.model.entities.MapPoint;
import org.ametro.model.entities.MapRect;
import org.ametro.utils.StringUtils;

public class CommonTypes {

    public final static int DEFAULT_COLOR = 0x0000FF;
    public final static int DEFAULT_LABEL_BG_COLOR = 0x0000FF;

    public static String[] asStringArray(JsonNode node) {
        String[] array = new String[node.size()];
        for (int i = 0; i < node.size(); i++) {
            array[i] = node.get(i).asText();
        }
        return array;
    }

    public static int asColor(JsonNode node, int defaultColor) {
        if(node == null || node.isNull()){
            return defaultColor;
        }

        String text = node.asText();
        if(StringUtils.isNullOrEmpty(text))
            return defaultColor;

        if(text.equals("-1")){ // TODO: fix on server side
            return Color.parseColor("#FFFFFF");
        }
        try {
            return Color.parseColor('#' + text);
        }catch (Exception ex){
            Log.e(Constants.LOG, "Invalid color [" + text + "]");
            return defaultColor;
        }
    }

    public static MapRect asRect(JsonNode node) {
        if(node == null || node.isNull()){
            return null;
        }
        return new MapRect(
                node.get(0).asInt(),
                node.get(1).asInt(),
                node.get(2).asInt(),
                node.get(3).asInt());
    }

    public static MapPoint asPoint(JsonNode node) {
        if(node == null || node.isNull()){
            return null;
        }
        return new MapPoint(
                (float)node.get(0).asDouble(),
                (float)node.get(1).asDouble());
    }

    public static MapPoint[] asPointArray(JsonNode arrayNode) {
        if(arrayNode == null || arrayNode.isNull()){
            return new MapPoint[0];
        }
        MapPoint[] array = new MapPoint[arrayNode.size()];
        for (int i = 0; i < arrayNode.size(); i++) {
            array[i] = asPoint(arrayNode.get(i));
        }
        return array;
    }

    public static int[] asIntArray(JsonNode arrayNode) {
        if(arrayNode == null || arrayNode.isNull()){
            return new int[0];
        }
        int[] array = new int[arrayNode.size()];
        for (int i = 0; i < arrayNode.size(); i++) {
            array[i] = arrayNode.get(i).asInt();
        }
        return array;
    }
}
