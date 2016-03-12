package org.ametro.ui.navigation.entities;

public class NavigationSubHeader extends NavigationItem implements INavigationItemGroup {

    private final CharSequence text;
    private final NavigationItem[] items;

    public NavigationSubHeader(CharSequence text, NavigationItem[] items){
        this(INVALID_ACTION, text, items);
    }

    public NavigationSubHeader(int action, CharSequence text, NavigationItem[] items){
        super(action);
        this.text = text;
        this.items = items;
    }

    public CharSequence getText() {
        return text;
    }

    public NavigationItem[] getItems() {
        return items;
    }
}
