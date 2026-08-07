package ua.polodarb.gmsflags.presentation.core.ui.state

import android.content.res.Resources
import ua.polodarb.gmsflags.presentation.core.error.UiError
import ua.polodarb.gmsflags.presentation.core.ui.R

fun UiError.localizedDescription(resources: Resources): String =
    resources.getString(toCopy().descriptionRes)

internal fun UiError.toCopy(): ErrorCopy = when (this) {
    UiError.NetworkUnavailable -> ErrorCopy(
        R.string.error_network_title,
        R.string.error_network_description,
    )
    UiError.Timeout -> ErrorCopy(
        R.string.error_timeout_title,
        R.string.error_timeout_description,
    )
    UiError.Unauthorized -> ErrorCopy(
        R.string.error_unauthorized_title,
        R.string.error_unauthorized_description,
    )
    UiError.AccessDenied -> ErrorCopy(
        R.string.error_access_denied_title,
        R.string.error_access_denied_description,
    )
    UiError.NotFound -> ErrorCopy(
        R.string.error_not_found_title,
        R.string.error_not_found_description,
    )
    UiError.TooManyRequests -> ErrorCopy(
        R.string.error_rate_limit_title,
        R.string.error_rate_limit_description,
    )
    UiError.Server -> ErrorCopy(
        R.string.error_server_title,
        R.string.error_server_description,
    )
    UiError.InvalidResponse -> ErrorCopy(
        R.string.error_invalid_response_title,
        R.string.error_invalid_response_description,
    )
    UiError.RootUnavailable -> ErrorCopy(
        R.string.error_root_unavailable_title,
        R.string.error_root_unavailable_description,
    )
    UiError.RootServiceUnavailable -> ErrorCopy(
        R.string.error_root_service_title,
        R.string.error_root_service_description,
    )
    UiError.SystemDataUnavailable -> ErrorCopy(
        R.string.error_system_data_title,
        R.string.error_system_data_description,
    )
    UiError.Generic -> ErrorCopy(
        R.string.error_generic_title,
        R.string.error_generic_description,
    )
}

internal data class ErrorCopy(
    val titleRes: Int,
    val descriptionRes: Int,
)
