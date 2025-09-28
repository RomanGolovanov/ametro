package io.github.romangolovanov.apps.ametro.ui.navigation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.romangolovanov.apps.ametro.R;

class NavigationHeaderHolderFactory implements IHolderFactory{

    @Override
    public IHolder createHolder(View convertView) {
        NavigationHeaderHolder holder = new NavigationHeaderHolder(convertView);
        convertView.setTag(holder);
        return holder;
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.drawer_header_item, parent, false);
    }

}