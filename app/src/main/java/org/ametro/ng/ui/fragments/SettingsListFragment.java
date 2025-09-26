package org.ametro.ng.ui.fragments;


import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.ametro.ng.R;

public class SettingsListFragment extends PreferenceFragment
{
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}