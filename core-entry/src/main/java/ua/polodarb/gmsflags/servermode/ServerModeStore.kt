package ua.polodarb.gmsflags.servermode

import android.content.Context

interface ServerModeStore {
    fun read(): String?

    fun write(raw: String)
}

class SharedPreferencesServerModeStore(context: Context) : ServerModeStore {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override fun read(): String? = prefs.getString(KEY_PAYLOAD, null)

    override fun write(raw: String) {
        prefs.edit().putString(KEY_PAYLOAD, raw).apply()
    }

    private companion object {
        const val PREFS = "server_mode_prefs"
        const val KEY_PAYLOAD = "offline_mode_json"
    }
}
