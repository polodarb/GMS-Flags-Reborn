package ua.polodarb.gmsflags.data.repository.impl.apps.repository

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypePackageBinding
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypePackageReader
import ua.polodarb.gmsflags.data.repository.apps.datasource.XposedScopeDataSource
import ua.polodarb.gmsflags.data.repository.apps.datasource.XposedScopeSnapshot
import ua.polodarb.gmsflags.data.repository.impl.apps.reader.InstalledApplicationMetadata
import ua.polodarb.gmsflags.data.repository.impl.apps.reader.InstalledApplicationReader
import ua.polodarb.gmsflags.domain.apps.FlagPackageCategory
import ua.polodarb.gmsflags.domain.apps.XposedModuleStatus
import ua.polodarb.xposed.info.XposedTargetRegistry

class SupportedApplicationsRepositoryImplTest {
    @Test
    fun `reports disabled module from the same LSPosed scope snapshot`() = runBlocking {
        val repository = SupportedApplicationsRepositoryImpl(
            packageReader = PhenotypePackageReader { Result.success(emptyList()) },
            installedApplicationReader = InstalledApplicationReader { emptyMap() },
            xposedTargetRegistry = XposedTargetRegistry { emptySet() },
            xposedScopeDataSource = XposedScopeDataSource { _, _ ->
                Result.success(
                    XposedScopeSnapshot(
                        moduleFound = true,
                        moduleEnabled = false,
                        scopedPackageNames = emptySet(),
                    )
                )
            },
            modulePackageName = "module.app",
            userId = 0,
        )

        val snapshot = repository.getSnapshot().getOrThrow()

        assertEquals(XposedModuleStatus.Disabled, snapshot.moduleStatus)
    }

    @Test
    fun `keeps only installed applications supported by the runtime hook`() = runBlocking {
        var metadataReads = 0
        val packageReader = PhenotypePackageReader {
            Result.success(
                listOf(
                    PhenotypePackageBinding("installed.app", "installed.app"),
                    PhenotypePackageBinding("installed.app", "namespace.b"),
                    PhenotypePackageBinding("installed.app", "namespace.a"),
                    PhenotypePackageBinding("unsupported.app", "namespace.unsupported"),
                    PhenotypePackageBinding("missing.app", "namespace.missing"),
                    PhenotypePackageBinding("installed.app", "namespace.a"),
                )
            )
        }
        val installedReader = InstalledApplicationReader { requestedPackages ->
            metadataReads += 1
            assertEquals(setOf("installed.app", "missing.app"), requestedPackages)
            mapOf(
                "installed.app" to InstalledApplicationMetadata(
                    packageName = "installed.app",
                    name = "Installed",
                    versionName = "1.0",
                    versionCode = 1,
                    lastUpdateTime = 10,
                )
            )
        }

        val applications = SupportedApplicationsRepositoryImpl(
            packageReader = packageReader,
            installedApplicationReader = installedReader,
            xposedTargetRegistry = XposedTargetRegistry {
                setOf("installed.app", "missing.app")
            },
            xposedScopeDataSource = scopeDataSource("installed.app"),
            modulePackageName = "module.app",
            userId = 0,
        ).getApplications().getOrThrow()

        assertEquals(1, metadataReads)
        assertEquals(1, applications.size)
        assertEquals("installed.app", applications.single().androidPackageName)
        assertEquals(
            listOf("installed.app", "namespace.a", "namespace.b"),
            applications.single().flagPackages.map { it.packageName },
        )
        assertEquals(
            FlagPackageCategory.Primary,
            applications.single().flagPackages.single { it.packageName == "installed.app" }.category,
        )
        assertTrue(
            applications.single().flagPackages
                .filter { it.packageName.startsWith("namespace") }
                .all { it.category == FlagPackageCategory.Secondary }
        )
    }

    @Test
    fun `keeps a shared phenotype package bound to different applications`() = runBlocking {
        val packageReader = PhenotypePackageReader {
            Result.success(
                listOf(
                    PhenotypePackageBinding("first.app", "shared.namespace"),
                    PhenotypePackageBinding("second.app", "shared.namespace"),
                )
            )
        }
        val installedReader = InstalledApplicationReader { requestedPackages ->
            requestedPackages.associateWith { packageName ->
                InstalledApplicationMetadata(
                    packageName = packageName,
                    name = packageName,
                    versionName = null,
                    versionCode = 1,
                    lastUpdateTime = 0,
                )
            }
        }

        val applications = SupportedApplicationsRepositoryImpl(
            packageReader = packageReader,
            installedApplicationReader = installedReader,
            xposedTargetRegistry = XposedTargetRegistry {
                setOf("first.app", "second.app")
            },
            xposedScopeDataSource = scopeDataSource("first.app", "second.app"),
            modulePackageName = "module.app",
            userId = 0,
        ).getApplications().getOrThrow()

        assertEquals(setOf("first.app", "second.app"), applications.map { it.androidPackageName }.toSet())
        assertTrue(applications.all { app ->
            app.flagPackages.single().packageName == "shared.namespace"
        })
    }

    @Test
    fun `forces a known flag package into the list when the device registry never discovered it`() = runBlocking {
        val packageReader = PhenotypePackageReader {
            Result.success(
                listOf(
                    PhenotypePackageBinding("photos.app", "photos.app"),
                    PhenotypePackageBinding("photos.app", "photos_android_auto"),
                )
            )
        }
        val installedReader = InstalledApplicationReader { requestedPackages ->
            requestedPackages.associateWith { packageName ->
                InstalledApplicationMetadata(
                    packageName = packageName,
                    name = packageName,
                    versionName = null,
                    versionCode = 1,
                    lastUpdateTime = 0,
                )
            }
        }

        val applications = SupportedApplicationsRepositoryImpl(
            packageReader = packageReader,
            installedApplicationReader = installedReader,
            xposedTargetRegistry = object : XposedTargetRegistry {
                override fun supportedApplicationPackageNames() = setOf("photos.app")
                override fun preferredFlagPackageName(androidPackageName: String) = "photos.app"
                override fun knownFlagPackageNames(androidPackageName: String) =
                    setOf("photos.app.phenotype")
            },
            xposedScopeDataSource = scopeDataSource("photos.app"),
            modulePackageName = "module.app",
            userId = 0,
        ).getApplications().getOrThrow()

        val application = applications.single()
        assertEquals(
            listOf("photos.app", "photos.app.phenotype", "photos_android_auto"),
            application.flagPackages.map { it.packageName },
        )
        assertEquals(
            FlagPackageCategory.Primary,
            application.flagPackages.single { it.packageName == "photos.app" }.category,
        )
        assertEquals(
            FlagPackageCategory.Secondary,
            application.flagPackages.single { it.packageName == "photos.app.phenotype" }.category,
        )
        assertEquals("photos.app", application.mainFlagPackage?.packageName)
    }

    private fun scopeDataSource(vararg packageNames: String) = XposedScopeDataSource { _, _ ->
        Result.success(
            XposedScopeSnapshot(
                moduleFound = true,
                moduleEnabled = true,
                scopedPackageNames = packageNames.toSet(),
            )
        )
    }
}
