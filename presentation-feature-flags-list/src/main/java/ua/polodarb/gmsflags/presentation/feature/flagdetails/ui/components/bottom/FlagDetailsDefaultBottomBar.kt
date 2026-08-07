package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.bottom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsEvent

@Composable
internal fun FlagDetailsDefaultBottomBar(
    phenotypePackageName: String,
    primaryPhenotypePackageName: String,
    availablePhenotypePackageNames: List<String>,
    enabled: Boolean,
    onEvent: (FlagDetailsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = GmsSpacing.Large, vertical = GmsSpacing.Medium),
        horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDetailsPackageMenu(
            currentPackageName = phenotypePackageName,
            primaryPackageName = primaryPhenotypePackageName,
            packageNames = availablePhenotypePackageNames,
            enabled = enabled,
            onEvent = onEvent,
            modifier = Modifier
                .weight(1f),
        )

        FlagDetailsTransformMenuButton(
            enabled = enabled,
            onEvent = onEvent,
        )
    }
}
