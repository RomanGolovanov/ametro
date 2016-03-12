package org.ametro.ui.navigation.entities;

public abstract class NavigationItem {

    public final static int INVALID_ACTION = -1;

    private final int action;
    private final Object source;

    private boolean enabled;
    private boolean selected;

    public NavigationItem() {
        this(INVALID_ACTION, false);
    }

    public NavigationItem(int action) {
        this(action, false, false, null);
    }

    public NavigationItem(int action, boolean enabled) {

        this(action, enabled, false, null);
    }

    public NavigationItem(int action, boolean enabled, Object source) {
        this(action, enabled, false, source);
    }

    public NavigationItem(int action, boolean enabled, boolean selected, Object source) {
        this.action = action;
        this.enabled = enabled;
        this.source = source;
        this.selected = selected;
    }

        public int getAction() {
        return action;
    }

    public Object getSource() {
        return source;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
