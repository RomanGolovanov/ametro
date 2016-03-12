package org.ametro.ui.navigation.entities;

public class NavigationCheckBoxItem extends NavigationItem {

    private final CharSequence text;

    private boolean checked;

    public NavigationCheckBoxItem(int action, CharSequence text, boolean checked, Object source) {
        super(action, true, source);
        this.text = text;
        this.checked = checked;
    }

    public CharSequence getText() {
        return text;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}

