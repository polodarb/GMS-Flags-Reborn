package ua.polodarb.gmsflags.presentation.feature.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.compose.viewmodel.koinViewModel
import ua.polodarb.gmsflags.domain.settings.ObserveOverrideControl
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.settings.R

internal class OverrideNoticeViewModel(
    observeOverrideControl: ObserveOverrideControl,
) : ViewModel() {
    val paused = observeOverrideControl()
        .map { it.paused }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
}

@Composable
fun OverridesPausedNotice(onClick: () -> Unit) {
    val viewModel: OverrideNoticeViewModel = koinViewModel()
    val paused by viewModel.paused.collectAsStateWithLifecycle()
    AnimatedVisibility(
        visible = paused,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = GmsSpacing.Small)
                .clickable(onClick = onClick),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = GmsSpacing.Large, vertical = GmsSpacing.Medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.PauseCircle, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(GmsSpacing.Medium))
                Column {
                    Text(
                        stringResource(R.string.settings_paused_notice_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        stringResource(R.string.settings_paused_notice_description),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
