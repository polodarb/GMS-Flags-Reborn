package ua.polodarb.gmsflags.data.phenotype.root.flags

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.data.phenotype.runtime.RuntimeFlagOverride
import ua.polodarb.gmsflags.data.phenotype.runtime.RuntimeFlagOverrideStore
import ua.polodarb.gmsflags.data.phenotype.runtime.RuntimeMicroHookOverride
import ua.polodarb.gmsflags.data.phenotype.runtime.RuntimeOverrideDatabase
import ua.polodarb.gmsflags.data.phenotype.runtime.RuntimeOverrideDatabaseLocator
import ua.polodarb.gmsflags.data.phenotype.runtime.RuntimeOverrideFileAccess

class PhenotypeFlagStoreTest {
    private val androidPackageName = "com.google.android.googlequicksearchbox"
    private val bareNamespace = "com.google.android.apps.search.assistant.mobile.user"
    private val compositeIdentity = "$bareNamespace#$androidPackageName"

    private val file = File("runtime-overrides.db")
    private val database = FakeDatabase()
    private val overrideStore = RuntimeFlagOverrideStore(
        database = database,
        locator = RuntimeOverrideDatabaseLocator { file },
        fileAccess = object : RuntimeOverrideFileAccess {
            override fun prepare(
                androidPackageName: String,
                databaseFile: File,
                restoreContext: Boolean,
            ) = Unit
        },
    )
    private val store = PhenotypeFlagStore(
        flagReader = { _, _ -> emptyList() },
        overrideStore = overrideStore,
    )

    @Test
    fun `finds an override stored under the bare namespace while browsing the composite identity`() {
        database.overridesByPackage[bareNamespace] =
            listOf(RuntimeFlagOverride(bareNamespace, "some_flag", 0, "1"))

        val page = store.readFlagsPage(androidPackageName, compositeIdentity, offset = 0, limit = 10)

        assertEquals(1, page.flags.size)
        assertEquals("some_flag", page.flags.single().name)
        assertTrue(page.flags.single().overridden)
    }

    @Test
    fun `an override written under the composite identity is still found there directly`() {
        database.overridesByPackage[compositeIdentity] =
            listOf(RuntimeFlagOverride(compositeIdentity, "some_flag", 0, "1"))

        val page = store.readFlagsPage(androidPackageName, compositeIdentity, offset = 0, limit = 10)

        assertEquals(1, page.flags.size)
        assertTrue(page.flags.single().overridden)
    }

    @Test
    fun `deletes an override regardless of which candidate package it was actually stored under`() {
        store.deleteOverride(androidPackageName, compositeIdentity, "some_flag")

        assertEquals(
            setOf(compositeIdentity, bareNamespace, androidPackageName),
            database.deletedFromPackages,
        )
    }

    @Test
    fun `backup pages preserve custom package identities and exclude base flags`() {
        database.overridesByPackage["undiscovered"] = List(700) {
            RuntimeFlagOverride("undiscovered", "flag_$it", 3, "value_$it")
        }
        val all = buildList {
            var offset = 0
            while (true) {
                val page = store.readSavedOverridesPage(androidPackageName, offset)
                if (page.isEmpty()) {
                    break
                }
                addAll(page)
                offset += page.size
            }
        }
        assertEquals(700, all.size)
        assertEquals(700, all.map { it.flag.name }.distinct().size)
        assertTrue(all.all { it.packageName == "undiscovered" && it.flag.overridden && it.flag.originalValue == null })
    }

    private class FakeDatabase : RuntimeOverrideDatabase {
        val overridesByPackage = mutableMapOf<String, List<RuntimeFlagOverride>>()
        val deletedFromPackages = mutableSetOf<String>()

        override fun readPage(file: File, offset: Int): List<RuntimeFlagOverride> =
            overridesByPackage.values.flatten().sortedWith(compareBy(RuntimeFlagOverride::packageName).thenBy(RuntimeFlagOverride::name)).drop(offset).take(256)

        override fun read(file: File, phenotypePackageName: String) =
            overridesByPackage[phenotypePackageName].orEmpty()

        override fun write(
            file: File,
            phenotypePackageName: String,
            overrides: List<RuntimeFlagOverride>,
        ) = Unit

        override fun writeMicroHooks(
            file: File,
            androidPackageName: String,
            hooks: List<RuntimeMicroHookOverride>,
        ) = Unit

        override fun delete(file: File, phenotypePackageName: String, flagName: String): Boolean {
            deletedFromPackages += phenotypePackageName
            return true
        }

        override fun delete(
            file: File,
            phenotypePackageName: String,
            flagNames: List<String>,
        ): Boolean {
            deletedFromPackages += phenotypePackageName
            return true
        }

        override fun deletePackage(file: File, phenotypePackageName: String): Boolean {
            deletedFromPackages += phenotypePackageName
            return true
        }

        override fun deleteMicroHooks(
            file: File,
            androidPackageName: String,
            recipeIds: List<Long>,
        ) = true

        override fun deleteAll(file: File) = true
    }
}
