package ua.polodarb.gmsflags.presentation.feature.settings.overview.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.settings.R
import ua.polodarb.gmsflags.presentation.feature.settings.overview.icon.LogoTelegram
import ua.polodarb.gmsflags.presentation.feature.settings.overview.icon.LogoX

@Composable
internal fun DevelopersSection(
    versionName: String,
    onDanyilClick: () -> Unit,
    onTransaeroClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(GmsSpacing.Small))
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(0.38f)
                .align(Alignment.CenterHorizontally),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
        Spacer(Modifier.height(GmsSpacing.ExtraLarge))
        DeveloperCard(
            name = stringResource(R.string.settings_developer_danyil),
            role = stringResource(R.string.settings_developer_role_lead),
            avatarUrl = "https://github.com/polodarb.png?size=100",
            onClick = onDanyilClick,
        )
        Spacer(Modifier.height(GmsSpacing.Medium))
        DeveloperCard(
            name = stringResource(R.string.settings_developer_transaero),
            role = stringResource(R.string.settings_developer_role_xposed),
            avatarUrl = "https://github.com/transaero21.png?size=100",
            onClick = onTransaeroClick,
        )
        Spacer(Modifier.height(GmsSpacing.Medium))
        Text(
            text = stringResource(R.string.settings_version_short, versionName),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DeveloperCard(
    name: String,
    role: String,
    avatarUrl: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(GmsSpacing.Large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = null,
                error = rememberVectorPainter(Icons.Rounded.Person),
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape),
            )
            Spacer(Modifier.width(GmsSpacing.Medium))
            Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = role,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SettingsSocialBar(
    onTelegramClick: () -> Unit,
    onXClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = GmsSpacing.Large, vertical = GmsSpacing.Medium),
        horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onTelegramClick,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface,
            ),
            shapes = ButtonDefaults.shapes(),
        ) {
            Icon(LogoTelegram, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(GmsSpacing.Small))
            Text(stringResource(R.string.settings_join_telegram))
        }
        IconButton(
            onClick = onXClick,
            modifier = Modifier.size(56.dp),
            shapes = IconButtonDefaults.shapes(),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Icon(
                LogoX,
                contentDescription = stringResource(R.string.settings_follow_x),
                modifier = Modifier.size(19.dp),
            )
        }
    }
}
