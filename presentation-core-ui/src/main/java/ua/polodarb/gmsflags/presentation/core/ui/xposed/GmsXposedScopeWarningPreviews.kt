package ua.polodarb.gmsflags.presentation.core.ui.xposed

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.theme.GMSFlags20Theme

@Preview(name = "Badge", showBackground = true, widthDp = 320)
@Composable
private fun GmsXposedScopeWarningBadgePreview() {
    GMSFlags20Theme(dynamicColor = false) {
        GmsXposedScopeWarningBadge(
            label = "Not in scope",
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Action chip", showBackground = true, widthDp = 360)
@Composable
private fun GmsXposedScopeActionChipPreview() {
    GMSFlags20Theme(dynamicColor = false) {
        GmsXposedScopeActionChip(
            label = "Enable in LSPosed to apply flag overrides",
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
