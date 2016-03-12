package org.ametro.ui.navigation.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.ametro.R;
import org.ametro.ui.navigation.entities.NavigationItem;
import org.ametro.ui.navigation.entities.NavigationTextItem;

class NavigationTextItemHolder implements IHolder
{
    private final ImageView imageView;
    private final TextView textView;
    private final View container;

    public NavigationTextItemHolder(View view){
        imageView = (ImageView)view.findViewById(R.id.icon);
        textView = (TextView)view.findViewById(R.id.text);
        this.container = view;
    }

    @Override
    public void update(NavigationItem item) {
        NavigationTextItem textItem = (NavigationTextItem)item;
        imageView.setImageDrawable(textItem.getDrawable());
        textView.setText(textItem.getText());
        container.setBackgroundResource(textItem.isSelected() ? R.color.activated_color : android.R.color.transparent);
    }
}
