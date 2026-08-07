package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationDetailsUiModel
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.cards.RecommendationLogo
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.sheets.RecommendationSupportInfoSheet
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.badges.RecommendationStatusRow
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.sheets.RecommendationApplicationInfoSheet
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationApplicationUiStatus

@Composable
internal fun RecommendationHeroCard(
    recommendation: RecommendationDetailsUiModel,
    applicationStatus: RecommendationApplicationUiStatus,
    onExternalLinkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var supportInfoVisible by remember(recommendation.id) { mutableStateOf(false) }
    var applicationInfoVisible by remember(recommendation.id) { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(GmsSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RecommendationLogo(logoUrl = recommendation.logoUrl)
                Text(
                    text = recommendation.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            RecommendationStatusRow(
                applicationStatus = applicationStatus,
                supportStatus = recommendation.supportStatus,
                onApplicationClick = { applicationInfoVisible = true },
                onSupportClick = { supportInfoVisible = true },
            )
            recommendation.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            recommendation.applicationName?.let { applicationName ->
                Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall)) {
                    Text(
                        text = applicationName,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    recommendation.packageName?.let { packageName ->
                        Text(
                            text = packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    recommendation.installedVersionName?.let { versionName ->
                        Text(
                            text = stringResource(
                                R.string.suggestions_details_installed_version,
                                versionName,
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (recommendation.externalLink != null) {
                OutlinedButton(onClick = onExternalLinkClick) {
                    Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
                    Text(
                        text = stringResource(R.string.suggestions_details_learn_more),
                        modifier = Modifier.padding(start = GmsSpacing.Small),
                    )
                }
            }
            recommendation.source?.let { source ->
                Text(
                    text = stringResource(R.string.suggestions_details_source, source),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (supportInfoVisible) {
        RecommendationSupportInfoSheet(
            currentStatus = recommendation.supportStatus,
            onDismiss = { supportInfoVisible = false },
        )
    }
    if (applicationInfoVisible) {
        RecommendationApplicationInfoSheet(
            currentStatus = applicationStatus,
            onDismiss = { applicationInfoVisible = false },
        )
    }
}
