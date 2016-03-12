package org.ametro.ui.navigation.adapter;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import org.ametro.R;
import org.ametro.ui.navigation.entities.NavigationCheckBoxItem;
import org.ametro.ui.navigation.entities.NavigationItem;

class NavigationCheckBoxItemHolder implements IHolder
{
    private final CheckBox checkBox;
    private final TextView textView;

    public NavigationCheckBoxItemHolder(View view){
        checkBox = (CheckBox)view.findViewById(R.id.checkbox);
        textView = (TextView)view.findViewById(R.id.text);
    }

    @Override
    public void update(NavigationItem item) {
        NavigationCheckBoxItem checkboxItem = (NavigationCheckBoxItem)item;
        checkBox.setChecked(checkboxItem.isChecked());
        textView.setText(checkboxItem.getText());
    }
}
