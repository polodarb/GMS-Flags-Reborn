package ua.polodarb.gmsflags.presentation.core.ui.loading

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.theme.GMSFlags20Theme

@Preview(name = "Loading indicator", showBackground = true, widthDp = 120, heightDp = 120)
@Composable
private fun GmsLoadingIndicatorPreview() {
    GMSFlags20Theme(dynamicColor = false) {
        GmsLoadingIndicator(modifier = Modifier.size(120.dp))
    }
}
