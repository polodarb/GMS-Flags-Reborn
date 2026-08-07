package ua.polodarb.gmsflags.presentation.core.ui.search

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.theme.GMSFlags20Theme

@Preview(name = "Empty", showBackground = true, widthDp = 360)
@Composable
private fun GmsSearchFieldEmptyPreview() {
    GMSFlags20Theme(dynamicColor = false) {
        GmsSearchField(
            query = "",
            onQueryChange = {},
            placeholder = "Search flags",
            requestFocus = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "With query", showBackground = true, widthDp = 360)
@Composable
private fun GmsSearchFieldWithQueryPreview() {
    GMSFlags20Theme(dynamicColor = false) {
        GmsSearchField(
            query = "gemini",
            onQueryChange = {},
            placeholder = "Search flags",
            requestFocus = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}
