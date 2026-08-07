package ua.polodarb.gmsflags.data.phenotype.runtime

import java.io.File

internal interface RuntimeOverrideDatabase {
    fun read(file: File, phenotypePackageName: String): List<RuntimeFlagOverride>
    fun write(
        file: File,
        phenotypePackageName: String,
        overrides: List<RuntimeFlagOverride>,
    )

    fun writeMicroHooks(
        file: File,
        androidPackageName: String,
        hooks: List<RuntimeMicroHookOverride>,
    )

    fun delete(file: File, phenotypePackageName: String, flagName: String): Boolean
    fun delete(file: File, phenotypePackageName: String, flagNames: List<String>): Boolean
    fun deletePackage(file: File, phenotypePackageName: String): Boolean

    fun deleteMicroHooks(file: File, androidPackageName: String, recipeIds: List<Long>): Boolean

    fun deleteAll(file: File): Boolean
}
