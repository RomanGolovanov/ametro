package org.ametro.ui.navigation.adapter;

import android.view.View;
import android.widget.TextView;

import org.ametro.R;
import org.ametro.ui.navigation.entities.NavigationItem;
import org.ametro.ui.navigation.entities.NavigationSubHeader;

class NavigationSubHeaderHolder implements IHolder
{
    private final TextView textView;

    public NavigationSubHeaderHolder(View view){
        textView = (TextView)view.findViewById(R.id.text);
    }

    @Override
    public void update(NavigationItem item) {
        NavigationSubHeader textItem = (NavigationSubHeader)item;
        textView.setText(textItem.getText());
    }
}
