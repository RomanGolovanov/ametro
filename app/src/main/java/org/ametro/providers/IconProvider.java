package org.ametro.providers;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class IconProvider {

    private final AssetManager assetManager;

    private final HashMap<String,Drawable> icons = new HashMap<>();
    private final HashSet<String> assets = new HashSet<>();
    private final Drawable defaultIcon;
    private final String assetPath;

    public IconProvider(Context context, Drawable defaultIcon, String assetPath){
        this.defaultIcon = defaultIcon;
        this.assetPath = assetPath;
        assetManager = context.getAssets();
        try {
            for (String assetName : assetManager.list(assetPath)) {
                assets.add(assetName.toLowerCase());
            }
        }catch(IOException ex){
            // no icons available
        }
    }

    public Drawable getIcon(String iso) {
        Drawable d = icons.get(iso);
        if (d == null) {
            String assetName = iso.toLowerCase() + ".png";
            if (assets.contains(assetName)) {
                try {
                    d = Drawable.createFromStream(
                            assetManager.open(assetPath + "/" + assetName), null);
                } catch (IOException e) {
                    d = defaultIcon;
                }
            } else {
                d = defaultIcon;
            }
            icons.put(iso, d);
        }
        return d;
    }
}
