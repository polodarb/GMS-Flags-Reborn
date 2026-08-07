package ua.polodarb.gmsflags.presentation.core.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ua.polodarb.gmsflags.presentation.core.ui.theme.GMSFlags20Theme

@Preview(name = "Back header", showBackground = true, widthDp = 400)
@Composable
private fun GmsBackHeaderPreview() {
    GMSFlags20Theme(dynamicColor = false) {
        GmsBackHeader(title = "Flag Details", onBack = {})
    }
}
