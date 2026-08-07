package ua.polodarb.gmsflags.data.repository.impl.flags

import ua.polodarb.gmsflags.core.root.RootCommandExecutor
import ua.polodarb.gmsflags.core.root.RootAccessUnavailableException
import ua.polodarb.gmsflags.domain.error.AppError

internal fun interface TargetProcessRestarter {
    suspend fun restart(androidPackageName: String): Result<Unit>
}

internal class RootTargetProcessRestarter(
    private val commandExecutor: RootCommandExecutor,
) : TargetProcessRestarter {
    override suspend fun restart(androidPackageName: String): Result<Unit> {
        if (!ANDROID_PACKAGE_PATTERN.matches(androidPackageName)) {
            return Result.failure(IllegalArgumentException("Invalid Android package name"))
        }
        return commandExecutor.execute("am force-stop $androidPackageName").fold(
            onSuccess = { result ->
                if (result.code == 0) Result.success(Unit)
                else Result.failure(AppError.SystemDataUnavailable())
            },
            onFailure = { error ->
                Result.failure(
                    if (error is RootAccessUnavailableException) AppError.RootUnavailable
                    else AppError.SystemDataUnavailable(error),
                )
            },
        )
    }

    private companion object {
        val ANDROID_PACKAGE_PATTERN = Regex("[A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)+")
    }
}
