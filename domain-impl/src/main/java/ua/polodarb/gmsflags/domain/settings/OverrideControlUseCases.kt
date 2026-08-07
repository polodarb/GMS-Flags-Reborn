package ua.polodarb.gmsflags.domain.settings

import ua.polodarb.gmsflags.data.repository.settings.repository.OverrideControlRepository

class ObserveOverrideControlUseCase(
    private val repository: OverrideControlRepository,
) : ObserveOverrideControl {
    override fun invoke() = repository.state
}

class RefreshOverrideControlUseCase(
    private val repository: OverrideControlRepository,
) : RefreshOverrideControl {
    override suspend fun invoke() = repository.refresh()
}

class SetOverridesPausedUseCase(
    private val repository: OverrideControlRepository,
) : SetOverridesPaused {
    override suspend fun invoke(paused: Boolean) = repository.setPaused(paused)
}

class DeleteAllOverridesUseCase(
    private val repository: OverrideControlRepository,
) : DeleteAllOverrides {
    override suspend fun invoke() = repository.deleteAll()
}

class DeleteApplicationOverridesUseCase(
    private val repository: OverrideControlRepository,
) : DeleteApplicationOverrides {
    override suspend fun invoke(androidPackageName: String) = repository.deleteAll(androidPackageName)
}
