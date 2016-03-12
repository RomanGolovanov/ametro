package org.ametro.ui.fragments;


import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.ametro.R;

public class SettingsListFragment extends PreferenceFragment
{
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}