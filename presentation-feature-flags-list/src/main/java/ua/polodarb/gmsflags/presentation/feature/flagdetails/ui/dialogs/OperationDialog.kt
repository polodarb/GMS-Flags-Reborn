package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.loading.GmsLoadingIndicator
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun OperationDialog(showBulkNotice: Boolean = false) {
    BasicAlertDialog(onDismissRequest = {}) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = GmsDimensions.ModalElevation,
        ) {
            if (showBulkNotice) {
                Column(
                    modifier = Modifier
                        .widthIn(max = GmsDimensions.DetailsMenuMaxWidth)
                        .padding(GmsSpacing.ExtraLarge),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LoadingIndicator(modifier = Modifier.size(GmsDimensions.BulkDialogIndicatorSize))
                    Text(
                        text = stringResource(R.string.flag_details_bulk_operation_title),
                        modifier = Modifier.padding(top = GmsSpacing.Large),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.flag_details_bulk_operation_description),
                        modifier = Modifier.padding(top = GmsSpacing.Small),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(GmsDimensions.DialogLoadingSize)
                        .padding(GmsSpacing.ExtraLarge),
                ) {
                    GmsLoadingIndicator()
                }
            }
        }
    }
}
