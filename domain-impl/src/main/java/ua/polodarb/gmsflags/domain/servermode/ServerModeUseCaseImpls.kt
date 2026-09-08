package ua.polodarb.gmsflags.domain.servermode

import ua.polodarb.gmsflags.data.repository.servermode.ServerModeRepository

class ObserveServerModeUseCase(
    private val repository: ServerModeRepository,
) : ObserveServerMode {
    override fun invoke() = repository.mode
}

class RefreshServerModeUseCase(
    private val repository: ServerModeRepository,
) : RefreshServerMode {
    override suspend fun invoke() = repository.refresh()
}
