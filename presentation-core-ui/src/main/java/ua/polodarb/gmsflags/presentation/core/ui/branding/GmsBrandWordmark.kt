package ua.polodarb.gmsflags.presentation.core.ui.branding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GmsBrandWordmark(
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    generationStyle: TextStyle = MaterialTheme.typography.labelSmall,
    color: Color = MaterialTheme.colorScheme.onSurface,
    generationColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    generationPlacement: GmsBrandGenerationPlacement = GmsBrandGenerationPlacement.Superscript,
) {
    val brandName = stringResource(R.string.gms_app_name)
    val generation = stringResource(R.string.gms_version_label)

    if (generationPlacement == GmsBrandGenerationPlacement.ExpressiveBadge) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = brandName, color = color, style = style)
            Surface(
                modifier = Modifier.size(30.dp),
                shape = MaterialShapes.Cookie6Sided.toShape(),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = generation,
                        style = generationStyle,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        return
    }

    Text(
        modifier = modifier,
        text = buildAnnotatedString {
            append(brandName)
            when (generationPlacement) {
                GmsBrandGenerationPlacement.Hidden -> Unit
                GmsBrandGenerationPlacement.ExpressiveBadge -> Unit
                GmsBrandGenerationPlacement.Inline -> {
                    append(" ")
                    withStyle(
                        SpanStyle(
                            color = generationColor,
                            fontWeight = FontWeight.Bold,
                        )
                    ) {
                        append(generation)
                    }
                }
                GmsBrandGenerationPlacement.Superscript -> {
                    append("\u2009")
                    withStyle(
                        SpanStyle(
                            color = generationColor,
                            fontSize = generationStyle.fontSize,
                            fontWeight = FontWeight.Bold,
                            baselineShift = BaselineShift.Superscript,
                        )
                    ) {
                        append(generation)
                    }
                }
            }
        },
        color = color,
        style = style,
    )
}

enum class GmsBrandGenerationPlacement {
    Hidden,
    Inline,
    Superscript,
    ExpressiveBadge,
}
