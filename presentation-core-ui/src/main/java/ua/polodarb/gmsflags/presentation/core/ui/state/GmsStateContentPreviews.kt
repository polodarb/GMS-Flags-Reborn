package ua.polodarb.gmsflags.presentation.core.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ua.polodarb.gmsflags.presentation.core.error.UiError
import ua.polodarb.gmsflags.presentation.core.ui.theme.GMSFlags20Theme

@Preview(name = "Empty", showBackground = true, widthDp = 390, heightDp = 500)
@Composable
private fun GmsEmptyContentPreview() {
    GMSFlags20Theme(dynamicColor = false) {
        GmsEmptyContent(
            title = "No flags found",
            description = "Try a different search or filter.",
        )
    }
}

@Preview(name = "Error", showBackground = true, widthDp = 390, heightDp = 500)
@Composable
private fun GmsErrorContentPreview() {
    GMSFlags20Theme(dynamicColor = false) {
        GmsErrorContent(error = UiError.NetworkUnavailable, onRetry = {})
    }
}
