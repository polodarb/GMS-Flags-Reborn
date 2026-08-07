package ua.polodarb.gmsflags.presentation.core.ui.application

import android.graphics.drawable.Drawable

interface ApplicationIconProvider {
    /**
     * Returns an already-cached icon synchronously, or null if it has not been loaded yet. Safe to
     * call on the main thread.
     */
    fun peek(packageName: String): Drawable?

    suspend fun load(packageName: String): Drawable?
}
