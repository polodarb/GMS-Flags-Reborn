package ua.polodarb.gmsflags.domain.navigation

import kotlinx.coroutines.flow.StateFlow

fun interface ObserveGmsInsightHidden {
    operator fun invoke(): StateFlow<Boolean>
}

fun interface RefreshNavigationFlags {
    suspend operator fun invoke()
}
