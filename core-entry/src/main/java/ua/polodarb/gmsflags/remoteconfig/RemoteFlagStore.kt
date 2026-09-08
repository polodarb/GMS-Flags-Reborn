package ua.polodarb.gmsflags.remoteconfig

import android.content.Context

interface RemoteFlagStore {
    fun read(key: String): String?

    fun write(key: String, raw: String)
}

class SharedPreferencesRemoteFlagStore(context: Context) : RemoteFlagStore {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override fun read(key: String): String? = prefs.getString(key, null)

    override fun write(key: String, raw: String) {
        prefs.edit().putString(key, raw).apply()
    }

    private companion object {
        const val PREFS = "server_mode_prefs"
    }
}
