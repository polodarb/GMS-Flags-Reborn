package ua.polodarb.gmsflags.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ua.polodarb.gmsflags.domain.update.AppUpdatePolicy
import ua.polodarb.gmsflags.domain.update.AppUpdateType

/**
 * Shows the force/soft update sheet on top of everything. FORCE cannot be dismissed (no gestures,
 * no scrim tap, no secondary button); SOFT can be snoozed via the secondary button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdateGate() {
    val viewModel: AppUpdateViewModel = koinViewModel()
    val policy by viewModel.sheetPolicy.collectAsStateWithLifecycle()

    policy?.let { current ->
        val isForce = current.type == AppUpdateType.FORCE
        val uriHandler = LocalUriHandler.current
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { target -> if (isForce) target != SheetValue.Hidden else true },
        )
        ModalBottomSheet(
            onDismissRequest = { if (!isForce) viewModel.dismiss() },
            sheetState = sheetState,
            properties = ModalBottomSheetProperties(
                shouldDismissOnBackPress = !isForce,
            ),
        ) {
            UpdateSheetContent(
                policy = current,
                isForce = isForce,
                onUpdate = { uriHandler.openUri(current.updateUrl) },
                onLater = { viewModel.dismiss() },
            )
        }
    }
}

@Composable
private fun UpdateSheetContent(
    policy: AppUpdatePolicy,
    isForce: Boolean,
    onUpdate: () -> Unit,
    onLater: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = policy.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = policy.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onUpdate,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(policy.primaryButtonTitle)
        }
        if (!isForce) {
            TextButton(
                onClick = onLater,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(policy.secondaryButtonTitle)
            }
        }
    }
}
