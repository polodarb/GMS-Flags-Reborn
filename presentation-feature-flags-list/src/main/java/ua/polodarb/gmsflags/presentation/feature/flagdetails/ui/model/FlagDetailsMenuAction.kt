package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.ui.graphics.vector.ImageVector
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsEvent

internal data class FlagDetailsMenuAction(
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
    val event: FlagDetailsEvent,
    val enabled: Boolean = true,
)

internal data class FlagDetailsMenuSection(
    val actions: List<FlagDetailsMenuAction>,
)

internal val flagDetailsMenuSections = listOf(
    FlagDetailsMenuSection(
        actions = listOf(
            FlagDetailsMenuAction(
                R.string.flag_action_add,
                Icons.Rounded.Add,
                FlagDetailsEvent.AddFlagClicked,
            ),
            FlagDetailsMenuAction(
                R.string.flag_action_add_multiple,
                Icons.AutoMirrored.Rounded.PlaylistAdd,
                FlagDetailsEvent.AddMultipleClicked,
            ),
        ),
    ),
    FlagDetailsMenuSection(
        actions = listOf(
            FlagDetailsMenuAction(
                R.string.flag_action_import,
                Icons.Rounded.UploadFile,
                FlagDetailsEvent.ImportFlagsClicked,
            ),
            FlagDetailsMenuAction(
                R.string.flag_action_delete_all,
                Icons.Rounded.DeleteSweep,
                FlagDetailsEvent.DeleteAllOverridesClicked,
            ),
        ),
    ),
    FlagDetailsMenuSection(
        actions = listOf(
            FlagDetailsMenuAction(
                R.string.flag_action_launch_app,
                Icons.Rounded.PlayArrow,
                FlagDetailsEvent.LaunchApplicationClicked,
            ),
            FlagDetailsMenuAction(
                R.string.flag_action_app_info,
                Icons.Rounded.Info,
                FlagDetailsEvent.OpenAppSettingsClicked,
            ),
        ),
    ),
)
