package ua.polodarb.gmsflags.presentation.core.ui.application

import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun GmsApplicationIcon(
    packageName: String,
    applicationName: String,
    iconProvider: ApplicationIconProvider,
    modifier: Modifier = Modifier,
    foregroundOnly: Boolean = false,
    tint: Color? = null,
) {
    val icon by produceState(initialValue = iconProvider.peek(packageName), key1 = packageName) {
        if (value == null) value = iconProvider.load(packageName)
    }
    val displayedIcon = remember(icon, foregroundOnly, tint) {
        icon?.displayedLayer(foregroundOnly = foregroundOnly, tint = tint)
    }

    if (displayedIcon == null) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.small,
            color = if (foregroundOnly) Color.Transparent else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
            contentColor = tint ?: MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = applicationName.firstOrNull()?.uppercase().orEmpty(),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    } else {
        AndroidView(
            factory = { context ->
                ImageView(context).apply { scaleType = ImageView.ScaleType.FIT_CENTER }
            },
            update = { imageView -> imageView.setImageDrawable(displayedIcon) },
            modifier = modifier,
        )
    }
}

private fun Drawable.displayedLayer(
    foregroundOnly: Boolean,
    tint: Color?,
): Drawable {
    val source = if (foregroundOnly && this is AdaptiveIconDrawable) foreground else this
    return (source.constantState?.newDrawable()?.mutate() ?: source.mutate()).apply {
        tint?.let { setTint(it.toArgb()) }
    }
}
