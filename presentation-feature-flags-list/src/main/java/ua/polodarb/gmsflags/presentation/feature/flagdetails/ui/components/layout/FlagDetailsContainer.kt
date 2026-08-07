package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.layout

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsContentContainer

@Composable
internal fun FlagDetailsContainer(
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceBright),
    ) {
        GmsContentContainer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            content = content,
        )
        bottomBar()
    }
}
