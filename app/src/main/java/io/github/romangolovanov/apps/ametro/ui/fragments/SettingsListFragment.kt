package io.github.romangolovanov.apps.ametro.ui.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import io.github.romangolovanov.apps.ametro.R

class SettingsListFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}
