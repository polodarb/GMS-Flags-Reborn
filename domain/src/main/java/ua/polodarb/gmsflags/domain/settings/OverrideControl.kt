package ua.polodarb.gmsflags.domain.settings

import kotlinx.coroutines.flow.StateFlow

data class OverrideControlState(
    val paused: Boolean = false,
    val overrideCount: Int = 0,
)

fun interface ObserveOverrideControl {
    operator fun invoke(): StateFlow<OverrideControlState>
}

fun interface RefreshOverrideControl {
    suspend operator fun invoke(): Result<Unit>
}

fun interface SetOverridesPaused {
    suspend operator fun invoke(paused: Boolean): Result<Unit>
}

fun interface DeleteAllOverrides {
    suspend operator fun invoke(): Result<Unit>
}

fun interface DeleteApplicationOverrides {
    suspend operator fun invoke(androidPackageName: String): Result<Unit>
}
