package ua.polodarb.gmsflags.data.repository.navigation

import kotlinx.coroutines.flow.StateFlow

interface NavigationFlagsRepository {
    val gmsInsightHidden: StateFlow<Boolean>

    suspend fun refresh()
}
