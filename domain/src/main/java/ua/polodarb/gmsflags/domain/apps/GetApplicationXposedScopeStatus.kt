package ua.polodarb.gmsflags.domain.apps

fun interface GetApplicationXposedScopeStatus {
    suspend operator fun invoke(androidPackageName: String): Result<XposedScopeStatus>
}
