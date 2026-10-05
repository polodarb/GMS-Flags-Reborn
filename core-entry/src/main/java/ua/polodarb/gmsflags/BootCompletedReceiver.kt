package ua.polodarb.gmsflags

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager

/**
 * Enqueues [RestartAttentionHooksWorker], which restarts every target app whose hook needs
 * attention (see [ua.polodarb.gmsflags.domain.hookstatus.needsAttention]).
 *
 * This only fires after the device's first unlock: [GmsFlagsApplication] is not direct-boot aware,
 * so Android defers [Intent.ACTION_BOOT_COMPLETED] for it until credential-protected storage - and
 * therefore the saved overrides - are actually readable. A target that started earlier in the boot
 * sequence (Gboard above all) already has whatever flags it read at its own startup baked in, so it
 * needs an actual restart - the same one the "restart" action already does after every manual edit -
 * to pick up overrides that were configured before this reboot.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // Expedited so the system runs it ahead of every other app's own boot-time work queue,
        // instead of waiting in line behind them for a regular JobScheduler slot.
        WorkManager.getInstance(context).enqueueUniqueWork(
            RESTART_ATTENTION_HOOKS_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<RestartAttentionHooksWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build(),
        )
    }

    private companion object {
        const val RESTART_ATTENTION_HOOKS_WORK_NAME = "restart_attention_hooks_after_boot"
    }
}
