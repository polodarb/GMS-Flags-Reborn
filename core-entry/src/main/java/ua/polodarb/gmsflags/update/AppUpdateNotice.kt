package ua.polodarb.gmsflags.update

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowCircleDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ua.polodarb.gmsflags.domain.update.AppUpdatePolicy
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing

/**
 * Persistent update banner shown above the main tab, next to the other top-level notices. Unlike
 * the sheet it never blocks the app, so it survives a dismissed soft-update sheet.
 */
@Composable
fun AppUpdateNotice() {
    val viewModel: AppUpdateViewModel = koinViewModel()
    val policy by viewModel.noticePolicy.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    AnimatedVisibility(
        visible = policy != null,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
    ) {
        policy?.let { current ->
            AppUpdateNoticeContent(
                policy = current,
                onUpdate = { uriHandler.openUri(current.updateUrl) },
            )
        }
    }
}

@Composable
private fun AppUpdateNoticeContent(
    policy: AppUpdatePolicy,
    onUpdate: () -> Unit,
) {
    Surface(
        onClick = onUpdate,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = GmsSpacing.Small),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(
                start = GmsSpacing.Large,
                end = GmsSpacing.Small,
                top = GmsSpacing.ExtraSmall,
                bottom = GmsSpacing.ExtraSmall,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowCircleDown,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(GmsSpacing.Medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = policy.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = policy.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(GmsSpacing.Small))
            TextButton(
                onClick = onUpdate,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                contentPadding = ButtonDefaults.TextButtonWithIconContentPadding,
            ) {
                Text(
                    text = policy.primaryButtonTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
