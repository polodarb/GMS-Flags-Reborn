package ua.polodarb.gmsflags.presentation.core.ui.state

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.theme.GMSFlags20Theme

@Preview(name = "Empty value badge", showBackground = true, widthDp = 120)
@Composable
private fun GmsEmptyValueBadgePreview() {
    GMSFlags20Theme(dynamicColor = false) {
        GmsEmptyValueBadge(modifier = Modifier.padding(16.dp))
    }
}
