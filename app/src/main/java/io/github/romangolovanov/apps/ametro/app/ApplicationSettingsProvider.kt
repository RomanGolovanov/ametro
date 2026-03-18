package io.github.romangolovanov.apps.ametro.app

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.util.Locale

class ApplicationSettingsProvider(context: Context) {

    private val settings: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, 0)

    val currentMapFileName: String?
        get() = settings.getString(SELECTED_MAP, null)

    fun setCurrentMap(mapFileName: String) {
        settings.edit { putString(SELECTED_MAP, mapFileName) }
    }

    val defaultLanguage: String
        get() = Locale.getDefault().language.lowercase()

    val preferredMapLanguage: String
        get() = settings.getString(PREFERRED_LANGUAGE, null) ?: defaultLanguage

    companion object {
        private const val PREFS_NAME = "aMetroPreferences"
        private const val SELECTED_MAP = "selectedMap"
        private const val PREFERRED_LANGUAGE = "preferredLanguage"
    }
}
