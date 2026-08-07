package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class HomeInfoBlockUiModel(
    val id: Long,
    val message: String,
    val type: HomeInfoBlockTypeUiModel,
    val externalLink: String? = null,
)

enum class HomeInfoBlockTypeUiModel {
    Info,
    Warning,
    Success,
    Promo,
    Danger,
    Unknown,
}
