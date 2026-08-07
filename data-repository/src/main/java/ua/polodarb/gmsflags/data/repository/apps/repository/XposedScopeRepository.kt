package ua.polodarb.gmsflags.data.repository.apps.repository

import ua.polodarb.gmsflags.domain.apps.XposedScopeStatus

fun interface XposedScopeRepository {
    suspend fun getApplicationStatus(androidPackageName: String): Result<XposedScopeStatus>
}
