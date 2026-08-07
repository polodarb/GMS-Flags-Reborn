package ua.polodarb.gmsflags.data.phenotype.runtime

import android.content.Context

internal data class RuntimeFlagOverride(
    val packageName: String,
    val name: String,
    val type: Int,
    val value: String,
)

internal data class RuntimeMicroHookOverride(
    val recipeId: Long,
    val payloadBase64: String,
    val payloadSha256: String,
    val signatureBase64: String,
    val required: Boolean,
)

internal class RuntimeFlagOverrideStore(
    private val database: RuntimeOverrideDatabase,
    private val locator: RuntimeOverrideDatabaseLocator,
    private val fileAccess: RuntimeOverrideFileAccess,
) {
    private val preparedTargets = mutableSetOf<String>()

    constructor(context: Context) : this(
        database = SqliteRuntimeOverrideDatabase(),
        locator = AndroidRuntimeOverrideDatabaseLocator(context.packageManager),
        fileAccess = AndroidRuntimeOverrideFileAccess(context.packageManager),
    )

    fun read(
        androidPackageName: String,
        phenotypePackageName: String,
    ): List<RuntimeFlagOverride> = database.read(
        file = locator.locate(androidPackageName),
        phenotypePackageName = phenotypePackageName,
    )

    fun write(
        androidPackageName: String,
        phenotypePackageName: String,
        overrides: List<RuntimeFlagOverride>,
    ) {
        if (overrides.isEmpty()) return
        val file = locator.locate(androidPackageName)
        database.write(file, phenotypePackageName, overrides)
        fileAccess.prepare(
            androidPackageName = androidPackageName,
            databaseFile = file,
            restoreContext = preparedTargets.add(androidPackageName),
        )
    }

    fun writeMicroHooks(
        androidPackageName: String,
        hooks: List<RuntimeMicroHookOverride>,
    ) {
        if (hooks.isEmpty()) return
        val file = locator.locate(androidPackageName)
        database.writeMicroHooks(file, androidPackageName, hooks)
        fileAccess.prepare(
            androidPackageName = androidPackageName,
            databaseFile = file,
            restoreContext = preparedTargets.add(androidPackageName),
        )
    }

    fun delete(
        androidPackageName: String,
        phenotypePackageName: String,
        flagName: String,
    ) {
        val file = locator.locate(androidPackageName)
        if (!database.delete(file, phenotypePackageName, flagName)) return
        fileAccess.prepare(androidPackageName, file, restoreContext = false)
    }

    fun delete(
        androidPackageName: String,
        phenotypePackageName: String,
        flagNames: List<String>,
    ) {
        if (flagNames.isEmpty()) return
        val file = locator.locate(androidPackageName)
        if (!database.delete(file, phenotypePackageName, flagNames)) return
        fileAccess.prepare(androidPackageName, file, restoreContext = false)
    }

    fun deletePackage(
        androidPackageName: String,
        phenotypePackageName: String,
    ) {
        val file = locator.locate(androidPackageName)
        if (!database.deletePackage(file, phenotypePackageName)) return
        fileAccess.prepare(androidPackageName, file, restoreContext = false)
    }

    fun deleteMicroHooks(
        androidPackageName: String,
        recipeIds: List<Long>,
    ) {
        if (recipeIds.isEmpty()) return
        val file = locator.locate(androidPackageName)
        if (!database.deleteMicroHooks(file, androidPackageName, recipeIds)) return
        fileAccess.prepare(androidPackageName, file, restoreContext = false)
    }

    fun deleteAll(androidPackageName: String) {
        val file = locator.locate(androidPackageName)
        if (!database.deleteAll(file)) return
        fileAccess.prepare(androidPackageName, file, restoreContext = false)
    }
}
