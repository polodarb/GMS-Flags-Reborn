package ua.polodarb.gmsflags.presentation.core.ui.branding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.theme.GMSFlags20Theme

@Preview(name = "All placements", showBackground = true, widthDp = 300)
@Composable
private fun GmsBrandWordmarkPreview() {
    GMSFlags20Theme(dynamicColor = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
        ) {
            GmsBrandWordmark(generationPlacement = GmsBrandGenerationPlacement.Superscript)
            GmsBrandWordmark(generationPlacement = GmsBrandGenerationPlacement.Inline)
            GmsBrandWordmark(generationPlacement = GmsBrandGenerationPlacement.ExpressiveBadge)
            GmsBrandWordmark(generationPlacement = GmsBrandGenerationPlacement.Hidden)
        }
    }
}
