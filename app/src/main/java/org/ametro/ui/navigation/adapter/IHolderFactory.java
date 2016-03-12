package org.ametro.ui.navigation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

interface IHolderFactory
{
    View createView(LayoutInflater inflater, ViewGroup parent);
    IHolder createHolder(View view);
}
