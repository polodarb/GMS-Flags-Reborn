package ua.polodarb.gmsflags.presentation.core.ui.application

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.LruCache
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CachedApplicationIconProvider(
    private val packageManager: PackageManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ApplicationIconProvider {
    private val cache = LruCache<String, Drawable.ConstantState>(CACHE_SIZE)

    override fun peek(packageName: String): Drawable? =
        synchronized(cache) { cache.get(packageName) }?.newDrawable()

    override suspend fun load(packageName: String): Drawable? = withContext(dispatcher) {
        synchronized(cache) {
            cache.get(packageName)?.newDrawable()
        } ?: runCatching {
            packageManager.getApplicationIcon(packageName)
        }.getOrNull()?.also { drawable ->
            drawable.constantState?.let { constantState ->
                synchronized(cache) { cache.put(packageName, constantState) }
            }
        }
    }

    private companion object {
        const val CACHE_SIZE = 256
    }
}
