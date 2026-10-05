package ua.polodarb.gmsflags

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ua.polodarb.gmsflags.analytics.CrashReporter
import ua.polodarb.gmsflags.domain.apps.GetApplicationXposedScopeStatus
import ua.polodarb.gmsflags.domain.apps.XposedScopeStatus
import ua.polodarb.gmsflags.domain.hookstatus.GetHookStatus
import ua.polodarb.gmsflags.domain.hookstatus.HookHealth
import ua.polodarb.gmsflags.domain.hookstatus.RestartHookTarget

/**
 * Restarts every target app stuck on a stale hook session, once per boot.
 *
 * Runs through WorkManager rather than a plain background coroutine or a directly-started
 * foreground service: [BootCompletedReceiver] has no foreground presence of its own to keep this
 * app's process alive while it connects to the root service and force-stops targets - a bare
 * `startForegroundService()` call from a broadcast receiver is denied outside a narrow exemption
 * window, and a plain background coroutine can be frozen mid-connection by the app freezer.
 * WorkManager's own executor already handles both.
 *
 * Only [HookHealth.RestartRequired] is acted on here, not every health the status screen flags as
 * needing attention: a restart only ever fixes a stale session (the current override count no
 * longer matching what that session loaded) - it does nothing for [HookHealth.Error],
 * [HookHealth.Partial], or a compatibility warning, which are structural problems restarting can't
 * touch. Restarting a target that a restart can't help would just force-stop it again on every
 * single boot for no benefit.
 */
class RestartAttentionHooksWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {
    private val getHookStatus: GetHookStatus by inject()
    private val getApplicationXposedScopeStatus: GetApplicationXposedScopeStatus by inject()
    private val restartHookTarget: RestartHookTarget by inject()
    private val crashReporter: CrashReporter by inject()

    override suspend fun doWork(): Result {
        val overview = getHookStatus().getOrElse { error ->
            logFailure(error)
            return Result.success()
        }

        overview.applications
            .filter { application -> application.health == HookHealth.RestartRequired }
            .forEach { application ->
                // A target the module isn't scoped to would never pick up the restart anyway.
                if (isInScope(application.androidPackageName)) {
                    restartHookTarget(application.androidPackageName).onFailure(::logFailure)
                }
            }

        return Result.success()
    }

    private suspend fun isInScope(androidPackageName: String): Boolean =
        getApplicationXposedScopeStatus(androidPackageName).getOrNull() == XposedScopeStatus.Included

    private fun logFailure(error: Throwable) {
        Log.e(TAG, "Failed to restart a hook needing attention after boot", error)
        crashReporter.recordException(error)
    }

    private companion object {
        const val TAG = "RestartAttentionHooks"
    }
}
