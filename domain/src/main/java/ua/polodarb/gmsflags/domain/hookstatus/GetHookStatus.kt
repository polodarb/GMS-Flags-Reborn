package ua.polodarb.gmsflags.domain.hookstatus

fun interface GetHookStatus {
    suspend operator fun invoke(): Result<HookStatusOverview>
}

fun interface RestartHookTarget {
    suspend operator fun invoke(androidPackageName: String): Result<Unit>
}
