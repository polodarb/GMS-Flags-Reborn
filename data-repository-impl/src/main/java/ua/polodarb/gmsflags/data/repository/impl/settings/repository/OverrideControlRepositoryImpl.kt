package ua.polodarb.gmsflags.data.repository.impl.settings.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ua.polodarb.gmsflags.data.repository.apps.repository.SupportedApplicationsRepository
import ua.polodarb.gmsflags.data.repository.impl.flags.TargetProcessRestarter
import ua.polodarb.gmsflags.data.repository.impl.settings.datasource.OverrideControlPreferences
import ua.polodarb.gmsflags.data.repository.settings.datasource.OverrideControlDataSource
import ua.polodarb.gmsflags.data.repository.settings.repository.OverrideControlRepository
import ua.polodarb.gmsflags.domain.server.content.AppliedRecommendationSetupStore
import ua.polodarb.gmsflags.domain.settings.OverrideControlState

internal class OverrideControlRepositoryImpl(
    private val applicationsRepository: SupportedApplicationsRepository,
    private val dataSource: OverrideControlDataSource,
    private val targetRestarter: TargetProcessRestarter,
    private val preferences: OverrideControlPreferences,
    private val appliedSetupStore: AppliedRecommendationSetupStore,
) : OverrideControlRepository {
    private val mutableState = MutableStateFlow(
        OverrideControlState(paused = preferences.paused)
    )
    override val state: StateFlow<OverrideControlState> = mutableState.asStateFlow()

    override suspend fun refresh(): Result<Unit> = applicationPackageNames().fold(
        onSuccess = { packageNames ->
            dataSource.readOverrideCount(packageNames).map { count ->
                val paused = preferences.paused
                if (paused) dataSource.setPaused(packageNames, true).getOrThrow()
                mutableState.value = OverrideControlState(paused = paused, overrideCount = count)
            }
        },
        onFailure = { Result.failure(it) },
    )

    override suspend fun setPaused(paused: Boolean): Result<Unit> =
        applicationPackageNames().fold(
            onSuccess = { packageNames ->
                dataSource.setPaused(packageNames, paused).fold(
                    onSuccess = {
                        preferences.paused = paused
                        mutableState.value = mutableState.value.copy(paused = paused)
                        restartTargets(packageNames)
                    },
                    onFailure = { Result.failure(it) },
                )
            },
            onFailure = { Result.failure(it) },
        )

    override suspend fun deleteAll(): Result<Unit> = applicationPackageNames().fold(
        onSuccess = { packageNames ->
            dataSource.deleteAll(packageNames).fold(
                onSuccess = {
                    mutableState.value = mutableState.value.copy(overrideCount = 0)
                    restartTargets(packageNames).fold(
                        onSuccess = { appliedSetupStore.clearAll() },
                        onFailure = { Result.failure(it) },
                    )
                },
                onFailure = { Result.failure(it) },
            )
        },
        onFailure = { Result.failure(it) },
    )

    override suspend fun deleteAll(androidPackageName: String): Result<Unit> =
        dataSource.deleteAll(listOf(androidPackageName)).fold(
            onSuccess = {
                restartTargets(listOf(androidPackageName)).fold(
                    onSuccess = { refresh() },
                    onFailure = { Result.failure(it) },
                )
            },
            onFailure = { Result.failure(it) },
        )

    private suspend fun applicationPackageNames(): Result<List<String>> = applicationsRepository
        .getApplications()
        .map { applications -> applications.map { it.androidPackageName }.distinct() }

    private suspend fun restartTargets(packageNames: List<String>): Result<Unit> {
        packageNames.forEach { packageName ->
            targetRestarter.restart(packageName).getOrElse { return Result.failure(it) }
        }
        return Result.success(Unit)
    }
}
