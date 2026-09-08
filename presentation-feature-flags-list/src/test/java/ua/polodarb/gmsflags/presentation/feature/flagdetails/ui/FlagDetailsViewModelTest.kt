package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ua.polodarb.gmsflags.analytics.NoOpPerformanceTracer
import ua.polodarb.gmsflags.presentation.feature.flagdetails.NoOpAnalyticsTracker
import ua.polodarb.gmsflags.domain.flags.ApplyFlagOverrides
import ua.polodarb.gmsflags.domain.flags.DeleteFlagOverride
import ua.polodarb.gmsflags.domain.flags.DeleteFlagOverrides
import ua.polodarb.gmsflags.domain.flags.DeletePackageOverrides
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.domain.flags.GetPhenotypeFlags
import ua.polodarb.gmsflags.domain.flags.PhenotypeFlag
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagOverridesChange
import ua.polodarb.gmsflags.domain.flags.ObserveFlagOverrideChanges
import ua.polodarb.gmsflags.domain.apps.GetApplicationXposedScopeStatus
import ua.polodarb.gmsflags.domain.apps.XposedScopeStatus
import ua.polodarb.gmsflags.domain.error.AppError
import ua.polodarb.gmsflags.domain.hookstatus.GetPairipIncompatiblePackages
import ua.polodarb.gmsflags.domain.server.content.GetApplicationRecommendations
import ua.polodarb.gmsflags.domain.server.content.GetServerApplication
import ua.polodarb.gmsflags.domain.server.content.ServerApplication
import ua.polodarb.gmsflags.domain.server.content.ServerApplicationDetails
import ua.polodarb.gmsflags.domain.servermode.ObserveServerMode
import ua.polodarb.gmsflags.domain.servermode.ServerMode
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.AppRemoteContentState
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsEffect
import ua.polodarb.gmsflags.presentation.core.error.DefaultErrorResolver
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.core.message.UiMessageType

@OptIn(ExperimentalCoroutinesApi::class)
class FlagDetailsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selecting package reloads flags in the same state holder`() = runTest(dispatcher) {
        val requestedPackages = mutableListOf<String>()
        val viewModel = flagDetailsViewModel { packageName ->
            requestedPackages += packageName
            Result.success(listOf(flag(packageName)))
        }
        advanceUntilIdle()

        viewModel.setEvent(FlagDetailsEvent.PackageSelected(STABLE_PACKAGE))
        advanceUntilIdle()

        assertEquals(listOf(REGULAR_PACKAGE, STABLE_PACKAGE), requestedPackages)
        assertEquals(STABLE_PACKAGE, viewModel.viewState.value.phenotypePackageName)
        assertEquals(REGULAR_PACKAGE, viewModel.viewState.value.primaryPhenotypePackageName)
        assertEquals(listOf(flag(STABLE_PACKAGE)), viewModel.viewState.value.flags)
    }

    @Test
    fun `unknown package is ignored`() = runTest(dispatcher) {
        val requestedPackages = mutableListOf<String>()
        val viewModel = flagDetailsViewModel { packageName ->
            requestedPackages += packageName
            Result.success(emptyList())
        }
        advanceUntilIdle()

        viewModel.setEvent(FlagDetailsEvent.PackageSelected("unknown.package"))
        advanceUntilIdle()

        assertEquals(listOf(REGULAR_PACKAGE), requestedPackages)
        assertEquals(REGULAR_PACKAGE, viewModel.viewState.value.phenotypePackageName)
    }

    @Test
    fun `search is applied after three hundred milliseconds of inactivity`() = runTest(dispatcher) {
        val viewModel = flagDetailsViewModel { Result.success(emptyList()) }
        advanceUntilIdle()

        viewModel.setEvent(FlagDetailsEvent.QueryChanged("first"))
        runCurrent()
        advanceTimeBy(200L)
        viewModel.setEvent(FlagDetailsEvent.QueryChanged("second"))
        runCurrent()
        advanceTimeBy(299L)
        runCurrent()

        assertEquals("second", viewModel.viewState.value.query)
        assertEquals("", viewModel.viewState.value.effectiveQuery)

        advanceTimeBy(1L)
        runCurrent()
        assertEquals("second", viewModel.viewState.value.effectiveQuery)
    }

    @Test
    fun `bulk boolean change emits a long warning`() = runTest(dispatcher) {
        val booleanFlag = flag("bulk_flag")
        val viewModel = flagDetailsViewModel { Result.success(listOf(booleanFlag)) }
        advanceUntilIdle()
        viewModel.setEvent(FlagDetailsEvent.FlagLongClicked(booleanFlag.name))
        runCurrent()
        val emittedEffect = async { viewModel.effect.first() }

        viewModel.setEvent(FlagDetailsEvent.SetSelectedBooleans(true))
        advanceUntilIdle()

        assertEquals(
            FlagDetailsEffect.ShowMessage(
                messageRes = R.string.message_bulk_override_warning,
                type = UiMessageType.Warning,
                longDuration = true,
            ),
            emittedEffect.await(),
        )
    }

    @Test
    fun `applied flags from another screen update the current list`() = runTest(dispatcher) {
        val changes = MutableSharedFlow<FlagOverridesChange>()
        val viewModel = flagDetailsViewModel(
            loader = { Result.success(emptyList()) },
            observeChanges = ObserveFlagOverrideChanges { changes },
        )
        advanceUntilIdle()

        changes.emit(
            FlagOverridesChange.Applied(
                androidPackageName = "com.android.vending",
                phenotypePackageName = REGULAR_PACKAGE,
                overrides = listOf(
                    FlagOverride("new_flag", FlagType.String, "new_value")
                ),
            )
        )
        runCurrent()

        assertEquals(
            listOf(
                PhenotypeFlag(
                    name = "new_flag",
                    type = FlagType.String,
                    originalValue = null,
                    value = "new_value",
                    overridden = true,
                )
            ),
            viewModel.viewState.value.flags,
        )
    }

    @Test
    fun `offline mode reports remote content as unavailable without calling the server`() =
        runTest(dispatcher) {
            var serverCalls = 0
            val viewModel = flagDetailsViewModel(
                offline = true,
                getServerApplication = GetServerApplication {
                    serverCalls++
                    Result.failure(AppError.NetworkUnavailable)
                },
            )
            advanceUntilIdle()

            assertEquals(AppRemoteContentState.Unavailable, viewModel.viewState.value.remoteContent)
            assertEquals(0, serverCalls)
        }

    private fun flagDetailsViewModel(
        observeChanges: ObserveFlagOverrideChanges = ObserveFlagOverrideChanges { emptyFlow() },
        offline: Boolean = false,
        getServerApplication: GetServerApplication = GetServerApplication { packageName ->
            Result.success(
                ServerApplicationDetails(
                    application = ServerApplication(
                        id = 1L,
                        packageName = packageName,
                        displayName = "Google Play Store",
                        iconUrl = null,
                    ),
                    infoBlocks = emptyList(),
                    highlightedFlags = emptyList(),
                )
            )
        },
        loader: suspend (String) -> Result<List<PhenotypeFlag>> = { Result.success(emptyList()) },
    ) = FlagDetailsViewModel(
        androidPackageName = "com.android.vending",
        applicationName = "Google Play Store",
        initialPhenotypePackageName = REGULAR_PACKAGE,
        availablePhenotypePackageNames = listOf(REGULAR_PACKAGE, STABLE_PACKAGE),
        getFlags = GetPhenotypeFlags { _, packageName -> loader(packageName) },
        applyOverrides = ApplyFlagOverrides { _, _, _ -> Result.success(Unit) },
        deleteOverride = DeleteFlagOverride { _, _, _ -> Result.success(Unit) },
        deleteOverrides = DeleteFlagOverrides { _, _, _ -> Result.success(Unit) },
        deletePackageOverrides = DeletePackageOverrides { _, _ -> Result.success(Unit) },
        observeFlagOverrideChanges = observeChanges,
        getApplicationScopeStatus = GetApplicationXposedScopeStatus {
            Result.success(XposedScopeStatus.Included)
        },
        getPairipIncompatiblePackages = GetPairipIncompatiblePackages { Result.success(emptySet()) },
        getServerApplication = getServerApplication,
        getApplicationRecommendations = GetApplicationRecommendations {
            Result.success(emptyList())
        },
        errorResolver = DefaultErrorResolver(),
        analytics = NoOpAnalyticsTracker,
        performanceTracer = NoOpPerformanceTracer,
        observeServerMode = ObserveServerMode {
            MutableStateFlow(
                if (offline) ServerMode(offline = true, notice = null) else ServerMode.Online,
            )
        },
        backgroundDispatcher = dispatcher,
    )

    private fun flag(packageName: String) = PhenotypeFlag(
        name = packageName,
        type = FlagType.Boolean,
        originalValue = "0",
        value = "0",
        overridden = false,
    )

    private companion object {
        const val REGULAR_PACKAGE = "com.google.android.finsky.regular"
        const val STABLE_PACKAGE = "com.google.android.finsky.stable"
    }
}
