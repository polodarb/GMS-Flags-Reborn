package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.external.ui

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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import ua.polodarb.gmsflags.domain.apps.FlagPackage
import ua.polodarb.gmsflags.domain.apps.FlagPackageCategory
import ua.polodarb.gmsflags.domain.apps.GetSupportedApplications
import ua.polodarb.gmsflags.domain.apps.SupportedApplication
import ua.polodarb.gmsflags.domain.backup.FlagsBackup
import ua.polodarb.gmsflags.domain.backup.FlagsBackupCodec
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.document.FlagImportDocumentSource
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.external.mvi.ExternalImportEffect
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.model.FlagImportDocument
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.parser.GmsFlagsFileParser

@OptIn(ExperimentalCoroutinesApi::class)
class ExternalImportViewModelTest {
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
    fun `single matching application is opened automatically`() = runTest(dispatcher) {
        val viewModel = viewModel(listOf(application(SUPPORTED_PACKAGE)))
        val effect = async { viewModel.effect.first() }

        advanceUntilIdle()

        val openImport = effect.await() as ExternalImportEffect.OpenImport
        assertEquals(ANDROID_PACKAGE, openImport.target.androidPackageName)
        assertEquals(SUPPORTED_PACKAGE, openImport.target.phenotypePackageName)
        assertNull(viewModel.viewState.value.unsupportedPackageName)
    }

    @Test
    fun `package without matching application is reported as unsupported`() = runTest(dispatcher) {
        val viewModel = viewModel(listOf(application("another.package")))

        advanceUntilIdle()

        assertEquals(
            SUPPORTED_PACKAGE,
            viewModel.viewState.value.unsupportedPackageName,
        )
        assertEquals(emptyList<Any>(), viewModel.viewState.value.targets)
    }

    @Test
    fun `backup file routes to restore preview without legacy target resolution`() = runTest(dispatcher) {
        val viewModel = ExternalImportViewModel(
            documentUri = "content://test/backup.xml",
            documentSource = { FlagImportDocument("backup.xml", "<gms-flags-backup version=\"1\"/>") },
            parser = GmsFlagsFileParser(),
            getSupportedApplications = { error("Backup must not resolve legacy targets") },
            backgroundDispatcher = dispatcher,
            backupCodec = object : FlagsBackupCodec {
                override fun isBackup(xml: String) = true
                override fun encode(backup: FlagsBackup): String = error("unused")
                override fun decode(xml: String): FlagsBackup = error("Preview handles validation")
            },
        )
        val effect = async { viewModel.effect.first() }
        advanceUntilIdle()
        assertEquals(ExternalImportEffect.OpenBackup, effect.await())
    }

    private fun viewModel(applications: List<SupportedApplication>) = ExternalImportViewModel(
        documentUri = "content://test/flags.gmsflags",
        documentSource = {
            FlagImportDocument("flags.gmsflags", XML)
        },
        parser = GmsFlagsFileParser(),
        getSupportedApplications = { Result.success(applications) },
        backgroundDispatcher = dispatcher,
    )

    private fun application(packageName: String) = SupportedApplication(
        androidPackageName = ANDROID_PACKAGE,
        flagPackages = listOf(FlagPackage(packageName, FlagPackageCategory.Primary)),
        name = "Google Play Store",
        versionName = null,
        versionCode = 1,
        lastUpdateTime = 1,
    )

    private companion object {
        const val ANDROID_PACKAGE = "com.android.vending"
        const val SUPPORTED_PACKAGE = "com.google.android.finsky.stable"
        const val XML = """
            <package name="$SUPPORTED_PACKAGE">
                <flags>
                    <flag name="enabled" type="boolean" value="true" />
                </flags>
            </package>
        """
    }
}
