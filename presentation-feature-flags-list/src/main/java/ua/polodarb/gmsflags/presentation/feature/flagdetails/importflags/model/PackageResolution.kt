package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.model

internal fun resolvePackageName(candidate: String, supportedPackages: Set<String>): String? =
    candidate.takeIf { it in supportedPackages }
        ?: supportedPackages.firstOrNull { it.substringAfterLast('#') == candidate }

internal fun ImportedFlag.resolvedPackageName(
    primaryPackageName: String,
    supportedPackages: Set<String>,
): String? {
    val ownPackage = packageName ?: return primaryPackageName
    return resolvePackageName(ownPackage, supportedPackages) ?: ownPackage
}
