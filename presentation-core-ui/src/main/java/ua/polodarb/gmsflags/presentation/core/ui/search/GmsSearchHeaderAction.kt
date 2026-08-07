package ua.polodarb.gmsflags.presentation.core.ui.search

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.R
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsHeaderActionButton

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun GmsSearchHeaderAction(
    searchVisible: Boolean,
    onClick: () -> Unit,
) {
    val contentDescription = stringResource(
        if (searchVisible) R.string.action_close_search else R.string.action_search,
    )
    val searchToClose = AnimatedImageVector.animatedVectorResource(
        R.drawable.avd_searchtoclose_24dp,
    )

    GmsHeaderActionButton(
        contentDescription = contentDescription,
        onClick = onClick,
        selected = searchVisible,
    ) {
        Icon(
            painter = rememberAnimatedVectorPainter(
                animatedImageVector = searchToClose,
                atEnd = searchVisible,
            ),
            contentDescription = contentDescription,
        )
    }
}
