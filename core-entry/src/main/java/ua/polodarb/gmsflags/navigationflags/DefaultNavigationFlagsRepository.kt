package ua.polodarb.gmsflags.navigationflags

import kotlinx.coroutines.flow.StateFlow
import ua.polodarb.gmsflags.data.repository.navigation.NavigationFlagsRepository
import ua.polodarb.gmsflags.remoteconfig.StickyRemoteValue

class DefaultNavigationFlagsRepository(
    private val sticky: StickyRemoteValue<Boolean>,
) : NavigationFlagsRepository {
    override val gmsInsightHidden: StateFlow<Boolean> = sticky.value

    override suspend fun refresh() = sticky.refresh()
}

internal fun parseGmsInsightHidden(raw: String?): Boolean = raw?.toBooleanStrictOrNull() ?: false
