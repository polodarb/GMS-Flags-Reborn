package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ua.polodarb.gmsflags.domain.flags.ApplyFlagOverrides
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.presentation.core.error.DefaultErrorResolver
import ua.polodarb.gmsflags.presentation.feature.flagdetails.NoOpAnalyticsTracker
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.document.FlagImportDocumentSource
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.model.FlagImportDocument
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.model.ImportedFlagKey
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.mvi.ImportFlagsEffect
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.mvi.ImportFlagsEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.mvi.PackageOverrideTarget
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.parser.GmsFlagsFileParser

@OptIn(ExperimentalCoroutinesApi::class)
class ImportFlagsViewModelTest {
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
    fun `unsupported phenotype package is blocked before apply`() = runTest(dispatcher) {
        val applied = mutableListOf<List<FlagOverride>>()
        val viewModel = viewModel(UNSUPPORTED_XML) { overrides -> applied += overrides }

        advanceUntilIdle()

        assertNotNull(viewModel.viewState.value.unsupportedPackage)
        assertNull(viewModel.viewState.value.batch)
        assertEquals(emptyList<List<FlagOverride>>(), applied)
    }

    @Test
    fun `selected file flags are applied to the file package`() = runTest(dispatcher) {
        val applied = mutableListOf<Triple<String, String, List<FlagOverride>>>()
        val viewModel = viewModel(SUPPORTED_XML) { overrides ->
            applied += Triple(ANDROID_PACKAGE, SUPPORTED_PACKAGE, overrides)
        }
        advanceUntilIdle()
        val completion = async { viewModel.effect.first() }

        viewModel.setEvent(ImportFlagsEvent.ApplyClicked)
        advanceUntilIdle()

        assertEquals(1, applied.size)
        assertEquals(listOf("enabled", "label"), applied.single().third.map { it.name })
        assertEquals(ImportFlagsEffect.ImportCompleted(SUPPORTED_PACKAGE), completion.await())
    }

    @Test
    fun `overriding the whole batch's package changes every flag without its own package`() = runTest(dispatcher) {
        val viewModel = viewModel(SUPPORTED_XML) { }
        advanceUntilIdle()

        viewModel.setEvent(ImportFlagsEvent.PackageOverrideRequested(PackageOverrideTarget.WholeBatch))
        viewModel.setEvent(ImportFlagsEvent.PackageOverrideSelected(OTHER_PACKAGE))
        advanceUntilIdle()

        assertEquals(OTHER_PACKAGE, viewModel.viewState.value.batch?.phenotypePackageName)
        assertEquals(null, viewModel.viewState.value.packageOverrideTarget)
    }

    @Test
    fun `overriding a single flag's package only changes that flag`() = runTest(dispatcher) {
        val viewModel = viewModel(SUPPORTED_XML) { }
        advanceUntilIdle()
        val key = ImportedFlagKey(FlagType.Boolean, "enabled")

        viewModel.setEvent(ImportFlagsEvent.PackageOverrideRequested(PackageOverrideTarget.SingleFlag(key)))
        viewModel.setEvent(ImportFlagsEvent.PackageOverrideSelected(OTHER_PACKAGE))
        advanceUntilIdle()

        val flags = viewModel.viewState.value.batch?.flags.orEmpty()
        assertEquals(OTHER_PACKAGE, flags.first { it.name == "enabled" }.packageName)
        assertEquals(null, flags.first { it.name == "label" }.packageName)
    }

    @Test
    fun `applying selected flags across two packages issues one apply call per package`() = runTest(dispatcher) {
        val calls = mutableListOf<Pair<String, List<FlagOverride>>>()
        val viewModel = viewModelWithApply(SUPPORTED_XML) { _, phenotypePackageName, overrides ->
            calls += phenotypePackageName to overrides
            Result.success(Unit)
        }
        advanceUntilIdle()
        val key = ImportedFlagKey(FlagType.Boolean, "enabled")
        viewModel.setEvent(ImportFlagsEvent.PackageOverrideRequested(PackageOverrideTarget.SingleFlag(key)))
        viewModel.setEvent(ImportFlagsEvent.PackageOverrideSelected(OTHER_PACKAGE))
        advanceUntilIdle()

        val completion = async { viewModel.effect.first() }
        viewModel.setEvent(ImportFlagsEvent.ApplyClicked)
        advanceUntilIdle()

        assertEquals(
            ImportFlagsEffect.ImportCompleted(SUPPORTED_PACKAGE),
            completion.await(),
        )
        assertEquals(2, calls.size)
        val callsByPackage = calls.associate { (packageName, overrides) ->
            packageName to overrides.map { it.name }
        }
        assertEquals(
            mapOf(
                OTHER_PACKAGE to listOf("enabled"),
                SUPPORTED_PACKAGE to listOf("label"),
            ),
            callsByPackage,
        )
    }

    @Test
    fun `a failure in one package's apply call surfaces an error even if the other package succeeded`() =
        runTest(dispatcher) {
            val calls = mutableListOf<String>()
            val viewModel = viewModelWithApply(SUPPORTED_XML) { _, phenotypePackageName, _ ->
                calls += phenotypePackageName
                if (phenotypePackageName == OTHER_PACKAGE) {
                    Result.failure(RuntimeException("boom"))
                } else {
                    Result.success(Unit)
                }
            }
            advanceUntilIdle()
            val key = ImportedFlagKey(FlagType.Boolean, "enabled")
            viewModel.setEvent(ImportFlagsEvent.PackageOverrideRequested(PackageOverrideTarget.SingleFlag(key)))
            viewModel.setEvent(ImportFlagsEvent.PackageOverrideSelected(OTHER_PACKAGE))
            advanceUntilIdle()

            val completion = async { viewModel.effect.first() }
            viewModel.setEvent(ImportFlagsEvent.ApplyClicked)
            advanceUntilIdle()

            val effect = completion.await()
            assertEquals(setOf(SUPPORTED_PACKAGE, OTHER_PACKAGE), calls.toSet())
            assertTrue(effect is ImportFlagsEffect.ShowError)
            assertFalse(viewModel.viewState.value.applying)
        }

    private fun viewModel(
        xml: String,
        supportedPackages: List<String> = listOf(SUPPORTED_PACKAGE, OTHER_PACKAGE),
        onApply: (List<FlagOverride>) -> Unit,
    ) = ImportFlagsViewModel(
        androidPackageName = ANDROID_PACKAGE,
        currentPhenotypePackageName = SUPPORTED_PACKAGE,
        supportedPhenotypePackageNames = supportedPackages,
        initialDocumentUri = "content://test/flags.gmsflags",
        documentSource = FlagImportDocumentSource {
            FlagImportDocument("flags.gmsflags", xml)
        },
        parser = GmsFlagsFileParser(),
        applyOverrides = ApplyFlagOverrides { _, _, overrides ->
            onApply(overrides)
            Result.success(Unit)
        },
        errorResolver = DefaultErrorResolver(),
        analytics = NoOpAnalyticsTracker,
        backgroundDispatcher = dispatcher,
    )

    private fun viewModelWithApply(
        xml: String,
        supportedPackages: List<String> = listOf(SUPPORTED_PACKAGE, OTHER_PACKAGE),
        applyOverrides: ApplyFlagOverrides,
    ) = ImportFlagsViewModel(
        androidPackageName = ANDROID_PACKAGE,
        currentPhenotypePackageName = SUPPORTED_PACKAGE,
        supportedPhenotypePackageNames = supportedPackages,
        initialDocumentUri = "content://test/flags.gmsflags",
        documentSource = FlagImportDocumentSource {
            FlagImportDocument("flags.gmsflags", xml)
        },
        parser = GmsFlagsFileParser(),
        applyOverrides = applyOverrides,
        errorResolver = DefaultErrorResolver(),
        analytics = NoOpAnalyticsTracker,
        backgroundDispatcher = dispatcher,
    )

    private companion object {
        const val ANDROID_PACKAGE = "com.android.vending"
        const val SUPPORTED_PACKAGE = "com.google.android.finsky.regular"
        const val OTHER_PACKAGE = "com.google.android.finsky.other"
        const val SUPPORTED_XML = """
            <package name="$SUPPORTED_PACKAGE">
                <flags>
                    <flag name="enabled" type="boolean" value="true" />
                    <flag name="label" type="string" value="test" />
                </flags>
            </package>
        """
        const val UNSUPPORTED_XML = """
            <package name="com.google.unsupported">
                <flags>
                    <flag name="enabled" type="boolean" value="true" />
                </flags>
            </package>
        """
    }
}
