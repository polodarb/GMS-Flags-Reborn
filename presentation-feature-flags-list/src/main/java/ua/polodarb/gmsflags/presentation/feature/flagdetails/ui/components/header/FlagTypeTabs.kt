package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.header

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabIndicatorScope
import androidx.compose.material3.Text
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.sp
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.model.labelRes

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun FlagTypeTabs(
    selectedType: FlagType,
    enabled: Boolean,
    onTypeSelected: (FlagType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val types = FlagType.entries
    val selectedIndex = types.indexOf(selectedType)
    val haptic = LocalHapticFeedback.current
    val indicator: @Composable TabIndicatorScope.() -> Unit = {
        Box(
            modifier = Modifier
                .tabIndicatorOffset(selectedIndex, matchContentSize = false)
                .padding(vertical = GmsSpacing.Indicator)
                .fillMaxSize()
                .padding(horizontal = GmsSpacing.ExtraSmall)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
        )
    }

    PrimaryTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = GmsSpacing.Large)
            .clip(CircleShape),
        indicator = indicator,
        divider = {},
    ) {
        types.forEachIndexed { index, type ->
            val selected = index == selectedIndex
            Tab(
                selected = selected,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTypeSelected(type)
                },
                enabled = enabled,
                text = {
                    Text(
                        text = stringResource(type.labelRes),
                        modifier = Modifier.padding(vertical = GmsSpacing.Micro),
                        style = MaterialTheme.typography.titleMediumEmphasized.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = GmsSpacing.Micro)
                    .padding(horizontal = GmsSpacing.ExtraSmall)
                    .clip(RoundedCornerShape(50))
                    .zIndex(2f),
            )
        }
    }
}
