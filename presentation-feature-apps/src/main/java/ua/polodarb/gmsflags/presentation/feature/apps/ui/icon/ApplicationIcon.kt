package ua.polodarb.gmsflags.presentation.feature.apps.ui.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import ua.polodarb.gmsflags.presentation.core.ui.application.ApplicationIconProvider
import ua.polodarb.gmsflags.presentation.core.ui.application.GmsApplicationIcon

@Composable
internal fun ApplicationIcon(
    packageName: String,
    applicationName: String,
    modifier: Modifier = Modifier,
) {
    val iconProvider: ApplicationIconProvider = koinInject()
    GmsApplicationIcon(
        packageName = packageName,
        applicationName = applicationName,
        iconProvider = iconProvider,
        modifier = modifier,
    )
}
