package ua.polodarb.gmsflags.update

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import ua.polodarb.gmsflags.data.repository.update.UpdatePolicyRepository
import ua.polodarb.gmsflags.domain.update.AppUpdatePolicy
import ua.polodarb.gmsflags.domain.update.AppUpdateType
import kotlin.coroutines.resume

/**
 * Force/soft update policy from Firebase Remote Config. Config schema and keys mirror GMS Insight
 * exactly (android_app_update_info with min_version + config). Soft-update snooze is stored in
 * SharedPreferences instead of DataStore.
 */
class FirebaseUpdatePolicyRepository(
    private val remoteConfig: FirebaseRemoteConfig,
    private val context: Context,
) : UpdatePolicyRepository {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override suspend fun getUpdatePolicy(): AppUpdatePolicy? {
        remoteConfig.fetchAndActivateAwait()

        val rawJson = remoteConfig.getString(KEY_UPDATE_INFO).trim()
        if (rawJson.isBlank()) return null

        val root = runCatching { JSONObject(rawJson) }.getOrNull() ?: return null
        val minVersion = root.optJSONObject(KEY_MIN_VERSION) ?: return null
        val config = root.optJSONObject(KEY_CONFIG) ?: return null

        val forceUpdateVersion = minVersion.optLong(KEY_FORCE_UPDATE, 0L)
        val softUpdateVersion = minVersion.optLong(KEY_SOFT_UPDATE, 0L)
        val currentVersion = currentVersionCode()

        val type = when {
            forceUpdateVersion > 0 && currentVersion < forceUpdateVersion -> AppUpdateType.FORCE
            softUpdateVersion > 0 && currentVersion < softUpdateVersion -> AppUpdateType.SOFT
            else -> return null
        }

        val title = config.optString(KEY_TITLE, DEFAULT_TITLE).trim().ifBlank { DEFAULT_TITLE }
        val description = config.optString(KEY_SUBTITLE, DEFAULT_SUBTITLE).trim().ifBlank { DEFAULT_SUBTITLE }
        val primaryButtonTitle = config.optString(KEY_PRIMARY_BUTTON_TITLE, DEFAULT_PRIMARY_BUTTON_TITLE)
            .trim().ifBlank { DEFAULT_PRIMARY_BUTTON_TITLE }
        val secondaryButtonTitle = config.optString(KEY_SECONDARY_BUTTON_TITLE, DEFAULT_SECONDARY_BUTTON_TITLE)
            .trim().ifBlank { DEFAULT_SECONDARY_BUTTON_TITLE }

        val configuredUrl = config.optString(KEY_UPDATE_URL, "").trim()
        val updateUrl = configuredUrl.ifBlank {
            "https://play.google.com/store/apps/details?id=${context.packageName}"
        }

        val softCooldownHours = config.optInt(KEY_SOFT_COOLDOWN_HOURS, DEFAULT_SOFT_COOLDOWN_HOURS)
            .coerceAtLeast(1)

        val policyId = config.optString(KEY_POLICY_ID, "").trim().ifBlank {
            listOf(
                forceUpdateVersion.toString(),
                softUpdateVersion.toString(),
                title,
                description,
                updateUrl,
                primaryButtonTitle,
                secondaryButtonTitle,
            ).joinToString("|")
        }

        if (type == AppUpdateType.SOFT) {
            val snoozedUntil = prefs.getLong(softDismissKey(policyId), 0L)
            if (System.currentTimeMillis() < snoozedUntil) return null
        }

        return AppUpdatePolicy(
            policyId = policyId,
            title = title,
            description = description,
            updateUrl = updateUrl,
            type = type,
            softCooldownHours = softCooldownHours,
            primaryButtonTitle = primaryButtonTitle,
            secondaryButtonTitle = secondaryButtonTitle,
        )
    }

    override suspend fun dismissSoftUpdate(policyId: String, cooldownHours: Int) {
        if (policyId.isBlank()) return
        val safeCooldown = cooldownHours.coerceAtLeast(1)
        val untilMillis = System.currentTimeMillis() + safeCooldown * ONE_HOUR_MILLIS
        prefs.edit().putLong(softDismissKey(policyId), untilMillis).apply()
    }

    private fun currentVersionCode(): Long {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return PackageInfoCompat.getLongVersionCode(packageInfo)
    }

    private suspend fun FirebaseRemoteConfig.fetchAndActivateAwait() {
        suspendCancellableCoroutine { cont ->
            fetchAndActivate().addOnCompleteListener {
                if (cont.isActive) cont.resume(Unit)
            }
        }
    }

    private companion object {
        const val PREFS = "update_policy_prefs"
        const val ONE_HOUR_MILLIS = 60 * 60 * 1000L

        const val KEY_UPDATE_INFO = "android_app_update_info"
        const val KEY_MIN_VERSION = "min_version"
        const val KEY_FORCE_UPDATE = "force_update"
        const val KEY_SOFT_UPDATE = "soft_update"
        const val KEY_CONFIG = "config"
        const val KEY_TITLE = "title"
        const val KEY_SUBTITLE = "subtitle"
        const val KEY_PRIMARY_BUTTON_TITLE = "primary_button_title"
        const val KEY_SECONDARY_BUTTON_TITLE = "secondary_button_title"
        const val KEY_UPDATE_URL = "update_url"
        const val KEY_POLICY_ID = "policy_id"
        const val KEY_SOFT_COOLDOWN_HOURS = "soft_cooldown_hours"

        const val DEFAULT_TITLE = "Update available"
        const val DEFAULT_SUBTITLE = "A newer version is available. Please update the app."
        const val DEFAULT_PRIMARY_BUTTON_TITLE = "Update"
        const val DEFAULT_SECONDARY_BUTTON_TITLE = "Later"
        const val DEFAULT_SOFT_COOLDOWN_HOURS = 24

        fun softDismissKey(policyId: String) = "soft_dismiss_until_$policyId"
    }
}
