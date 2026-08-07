package ua.polodarb.gmsflags.analytics

/**
 * A single analytics event: a snake_case [name] plus string/number/bool [params].
 */
class AnalyticsEvent private constructor(
    val name: String,
    val params: Map<String, Any>,
) {
    companion object {
        fun screenView(screen: AnalyticsScreen): AnalyticsEvent =
            AnalyticsEvent("screen_view", mapOf("screen_name" to screen.id))

        fun recommendationViewed(id: Long, title: String): AnalyticsEvent =
            AnalyticsEvent(
                "recommendation_viewed",
                mapOf("recommendation_id" to id, "title" to title.take(MAX_STRING)),
            )

        fun recommendationVariantApplied(
            id: Long,
            variantLabel: String?,
            flagsCount: Int,
        ): AnalyticsEvent = AnalyticsEvent(
            "recommendation_variant_applied",
            buildMap {
                put("recommendation_id", id)
                put("flags_count", flagsCount)
                variantLabel?.let { put("variant", it.take(MAX_STRING)) }
            },
        )

        fun recommendationDisabled(id: Long, flagsCount: Int): AnalyticsEvent =
            AnalyticsEvent(
                "recommendation_disabled",
                mapOf("recommendation_id" to id, "flags_count" to flagsCount),
            )

        fun recommendationLaunchApp(id: Long, packageName: String): AnalyticsEvent =
            AnalyticsEvent(
                "recommendation_launch_app",
                mapOf("recommendation_id" to id, "package" to packageName),
            )

        fun flagApplied(
            packageName: String,
            flagName: String,
            type: String,
            value: String,
        ): AnalyticsEvent = AnalyticsEvent(
            "flag_applied",
            mapOf(
                "package" to packageName,
                "flag_name" to flagName.take(MAX_STRING),
                "flag_type" to type,
                "flag_value" to value.take(MAX_STRING),
            ),
        )

        fun flagReset(packageName: String, flagName: String): AnalyticsEvent =
            AnalyticsEvent(
                "flag_reset",
                mapOf("package" to packageName, "flag_name" to flagName.take(MAX_STRING)),
            )

        fun flagsBatchApplied(packageName: String, count: Int): AnalyticsEvent =
            AnalyticsEvent(
                "flags_batch_applied",
                mapOf("package" to packageName, "flags_count" to count),
            )

        fun flagSearch(queryLength: Int): AnalyticsEvent =
            AnalyticsEvent("flag_search", mapOf("query_length" to queryLength))

        fun packageSwitched(packageName: String): AnalyticsEvent =
            AnalyticsEvent("package_switched", mapOf("package" to packageName))

        fun flagsImported(packageName: String): AnalyticsEvent =
            AnalyticsEvent("flags_imported", mapOf("package" to packageName))

        fun appDetailsOpened(packageName: String): AnalyticsEvent =
            AnalyticsEvent("app_details_opened", mapOf("package" to packageName))

        fun hookDetailsOpened(packageName: String, health: String): AnalyticsEvent =
            AnalyticsEvent(
                "hook_details_opened",
                mapOf("package" to packageName, "health" to health),
            )

        fun hookRestartCheck(packageName: String): AnalyticsEvent =
            AnalyticsEvent("hook_restart_check", mapOf("package" to packageName))

        fun hookOverridesDeleted(packageName: String): AnalyticsEvent =
            AnalyticsEvent("hook_overrides_deleted", mapOf("package" to packageName))

        fun overridesPaused(paused: Boolean): AnalyticsEvent =
            AnalyticsEvent("overrides_pause_toggled", mapOf("paused" to paused))

        fun overridesRemovedAll(count: Int): AnalyticsEvent =
            AnalyticsEvent("overrides_removed_all", mapOf("flags_count" to count))

        fun settingsItemClick(item: String): AnalyticsEvent =
            AnalyticsEvent("settings_item_click", mapOf("item" to item))

        fun insightPlayStoreClick(): AnalyticsEvent =
            AnalyticsEvent("insight_play_store_click", emptyMap())

        fun insightOpenClick(): AnalyticsEvent =
            AnalyticsEvent("insight_open_click", emptyMap())

        private const val MAX_STRING = 100
    }
}
