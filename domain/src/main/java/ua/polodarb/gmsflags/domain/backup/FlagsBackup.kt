package ua.polodarb.gmsflags.domain.backup

import ua.polodarb.gmsflags.domain.flags.FlagOverride

data class FlagsBackup(val packages: List<BackupPackage>) {
    val flagCount: Int
        get() = packages.sumOf { it.flags.size }
}

data class BackupPackage(
    val androidPackageName: String,
    val phenotypePackageName: String,
    val flags: List<FlagOverride>,
)

data class BackupRestoreResult(
    val restoredFlags: Int,
    val skippedPackages: List<BackupPackage>,
    val failedPackages: List<BackupPackage>,
    val restartFailedApps: List<String>,
    val refreshFailed: Boolean = false,
)

interface FlagsBackupCodec {
    fun isBackup(xml: String): Boolean

    fun encode(backup: FlagsBackup): String

    fun decode(xml: String): FlagsBackup
}

interface FlagsBackupService {
    suspend fun availableApps(): Set<String>

    suspend fun export(): FlagsBackup

    suspend fun restore(backup: FlagsBackup): BackupRestoreResult
}
