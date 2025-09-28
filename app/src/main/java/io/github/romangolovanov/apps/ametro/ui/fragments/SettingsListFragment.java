package io.github.romangolovanov.apps.ametro.ui.fragments;


import android.os.Bundle;
import android.preference.PreferenceFragment;

import io.github.romangolovanov.apps.ametro.R;

public class SettingsListFragment extends PreferenceFragment
{
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}