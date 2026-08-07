package ua.polodarb.gmsflags.data.repository.impl.settings.datasource

import android.content.SharedPreferences

internal interface OverrideControlPreferences {
    var paused: Boolean
}

internal class SharedPreferencesOverrideControlPreferences(
    private val preferences: SharedPreferences,
) : OverrideControlPreferences {
    override var paused: Boolean
        get() = preferences.getBoolean(KEY_PAUSED, false)
        set(value) { preferences.edit().putBoolean(KEY_PAUSED, value).apply() }

    private companion object {
        const val KEY_PAUSED = "overrides_paused"
    }
}
