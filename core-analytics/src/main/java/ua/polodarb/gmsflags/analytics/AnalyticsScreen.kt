package ua.polodarb.gmsflags.analytics

/** Canonical screen names for screen_view events. */
enum class AnalyticsScreen(val id: String) {
    Suggestions("suggestions"),
    RecommendationDetails("recommendation_details"),
    Apps("apps"),
    FlagDetails("flag_details"),
    HookStatus("hook_status"),
    HookStatusDetails("hook_status_details"),
    Insight("insight"),
    Settings("settings"),
    Overrides("overrides"),
}
