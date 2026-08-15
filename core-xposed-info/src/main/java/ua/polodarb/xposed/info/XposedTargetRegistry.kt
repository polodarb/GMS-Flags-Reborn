package ua.polodarb.xposed.info

fun interface XposedTargetRegistry {
    fun supportedApplicationPackageNames(): Set<String>

    fun preferredFlagPackageName(androidPackageName: String): String? = null

    fun knownFlagPackageNames(androidPackageName: String): Set<String> = emptySet()

    fun mendelApplicationPackageNames(): Set<String> = emptySet()
}
