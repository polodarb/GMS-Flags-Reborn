package ua.polodarb.gmsflags.presentation.feature.apps.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.core.ui.search.GmsSearchField
import ua.polodarb.gmsflags.presentation.feature.apps.R

@Composable
internal fun AppsSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = adaptiveLayout.contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        GmsSearchField(
            query = query,
            onQueryChange = onQueryChanged,
            placeholder = stringResource(R.string.apps_search_placeholder),
            requestFocus = true,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = adaptiveLayout.listMaxWidth),
        )
    }
}
