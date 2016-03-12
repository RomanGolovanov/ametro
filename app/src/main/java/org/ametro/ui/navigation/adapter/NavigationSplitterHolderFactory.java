package org.ametro.ui.navigation.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ametro.R;

class NavigationSplitterHolderFactory implements IHolderFactory{

    @Override
    public IHolder createHolder(View convertView) {
        NavigationSplitterHolder holder = new NavigationSplitterHolder();
        convertView.setTag(holder);
        return holder;
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.drawer_splitter_item, parent, false);
    }

}