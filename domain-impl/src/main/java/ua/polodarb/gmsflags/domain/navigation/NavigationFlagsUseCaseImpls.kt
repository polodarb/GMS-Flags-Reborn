package ua.polodarb.gmsflags.domain.navigation

import ua.polodarb.gmsflags.data.repository.navigation.NavigationFlagsRepository

class ObserveGmsInsightHiddenUseCase(
    private val repository: NavigationFlagsRepository,
) : ObserveGmsInsightHidden {
    override fun invoke() = repository.gmsInsightHidden
}

class RefreshNavigationFlagsUseCase(
    private val repository: NavigationFlagsRepository,
) : RefreshNavigationFlags {
    override suspend fun invoke() = repository.refresh()
}
