package org.ametro.ui.navigation.entities;

import android.graphics.drawable.Drawable;

public class NavigationTextItem extends NavigationItem {

    private final Drawable icon;
    private final CharSequence text;

    public NavigationTextItem(int action, Drawable icon, CharSequence text) {
        this(action, icon, text, true, null);
    }

    public NavigationTextItem(int action, Drawable icon, CharSequence text, boolean enabled, Object source) {
        super(action, enabled, source);
        this.icon = icon;
        this.text = text;
    }

    public CharSequence getText() {
        return text;
    }

    public Drawable getDrawable() {
        return icon;
    }
}

