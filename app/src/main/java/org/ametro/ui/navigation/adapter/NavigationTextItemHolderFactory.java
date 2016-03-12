package org.ametro.ui.navigation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ametro.R;

class NavigationTextItemHolderFactory implements IHolderFactory{

    @Override
    public IHolder createHolder(View convertView) {
        NavigationTextItemHolder holder = new NavigationTextItemHolder(convertView);
        convertView.setTag(holder);
        return holder;
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.drawer_text_item, parent, false);
    }

}



