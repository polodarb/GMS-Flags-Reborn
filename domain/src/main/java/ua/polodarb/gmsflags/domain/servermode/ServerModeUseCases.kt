package ua.polodarb.gmsflags.domain.servermode

import kotlinx.coroutines.flow.StateFlow

fun interface ObserveServerMode {
    operator fun invoke(): StateFlow<ServerMode>
}

fun interface RefreshServerMode {
    suspend operator fun invoke()
}
