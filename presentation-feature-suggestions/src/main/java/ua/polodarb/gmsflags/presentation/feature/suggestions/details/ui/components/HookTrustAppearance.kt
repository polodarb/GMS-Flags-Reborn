package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GppMaybe
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.rounded.GppBad
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ua.polodarb.gmsflags.domain.server.content.HookTrustStatus
import ua.polodarb.gmsflags.presentation.feature.suggestions.R

@Immutable
internal data class HookTrustAppearance(
    val labelRes: Int,
    val descriptionRes: Int,
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color,
)

@Composable
internal fun HookTrustStatus.appearance(): HookTrustAppearance = when (this) {
    HookTrustStatus.VERIFIED -> HookTrustAppearance(
        labelRes = R.string.suggestions_details_patch_trust_verified,
        descriptionRes = R.string.suggestions_details_patch_trust_verified_desc,
        icon = Icons.Rounded.VerifiedUser,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )
    HookTrustStatus.NOT_SIGNED -> HookTrustAppearance(
        labelRes = R.string.suggestions_details_patch_trust_not_signed,
        descriptionRes = R.string.suggestions_details_patch_trust_not_signed_desc,
        icon = Icons.Outlined.Shield,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    HookTrustStatus.CANNOT_VERIFY_IN_THIS_BUILD -> HookTrustAppearance(
        labelRes = R.string.suggestions_details_patch_trust_unavailable,
        descriptionRes = R.string.suggestions_details_patch_trust_unavailable_desc,
        icon = Icons.Outlined.GppMaybe,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    HookTrustStatus.VERIFICATION_FAILED -> HookTrustAppearance(
        labelRes = R.string.suggestions_details_patch_trust_failed,
        descriptionRes = R.string.suggestions_details_patch_trust_failed_desc,
        icon = Icons.Rounded.GppBad,
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    )
    HookTrustStatus.APP_UPDATE_REQUIRED -> HookTrustAppearance(
        labelRes = R.string.suggestions_details_patch_trust_update_required,
        descriptionRes = R.string.suggestions_details_patch_trust_update_required_desc,
        icon = Icons.Outlined.SystemUpdate,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    )
}
