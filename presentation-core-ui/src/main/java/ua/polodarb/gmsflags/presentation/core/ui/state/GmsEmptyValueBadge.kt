package ua.polodarb.gmsflags.presentation.core.ui.state

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.R
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing

@Composable
fun GmsEmptyValueBadge(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
            text = stringResource(R.string.flag_value_empty),
            modifier = Modifier.padding(
                horizontal = GmsSpacing.Small,
                vertical = GmsSpacing.ExtraSmall,
            ),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
