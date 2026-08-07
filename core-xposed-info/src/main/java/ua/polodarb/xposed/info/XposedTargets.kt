package ua.polodarb.xposed.info

object XposedTargets : XposedTargetRegistry {
    const val VENDING_PACKAGE_NAME = "com.android.vending"
    const val GOOGLE_APP_PACKAGE_NAME = "com.google.android.googlequicksearchbox"
    const val CALENDAR_PACKAGE_NAME = "com.google.android.calendar"
    const val DIALER_PACKAGE_NAME = "com.google.android.dialer"
    const val FITBIT_PACKAGE_NAME = "com.fitbit.FitbitMobile"
    const val TRANSLATE_PACKAGE_NAME = "com.google.android.apps.translate"
    const val KEEP_PACKAGE_NAME = "com.google.android.keep"
    const val YOUTUBE_PACKAGE_NAME = "com.google.android.youtube"
    const val YOUTUBE_MUSIC_PACKAGE_NAME = "com.google.android.apps.youtube.music"
    const val MESSAGES_PACKAGE_NAME = "com.google.android.apps.messaging"
    const val MAPS_PACKAGE_NAME = "com.google.android.apps.maps"
    const val DESKCLOCK_PACKAGE_NAME = "com.google.android.deskclock"
    const val TASKS_PACKAGE_NAME = "com.google.android.apps.tasks"
    const val CONTACTS_PACKAGE_NAME = "com.google.android.contacts"
    const val RECORDER_PACKAGE_NAME = "com.google.android.apps.recorder"
    const val PIXEL_LIVE_WALLPAPER_PACKAGE_NAME = "com.google.pixel.livewallpaper"
    const val GBOARD_PACKAGE_NAME = "com.google.android.inputmethod.latin"
    const val TIPS_PACKAGE_NAME = "com.google.android.apps.tips"
    const val PHOTOS_PACKAGE_NAME = "com.google.android.apps.photos"
    const val WEATHER_PACKAGE_NAME = "com.google.android.apps.weather"
    const val GOOGLE_TV_PACKAGE_NAME = "com.google.android.videos"
    const val WEAR_COMPANION_PACKAGE_NAME = "com.google.android.apps.wear.companion"
    const val CREDENTIAL_MANAGER_PACKAGE_NAME = "com.google.android.apps.credentialmanager"
    const val DIGITAL_WELLBEING_PACKAGE_NAME = "com.google.android.apps.wellbeing"
    const val SAFETY_HUB_PACKAGE_NAME = "com.google.android.apps.safetyhub"
    const val HEALTH_CONNECT_PACKAGE_NAME = "com.google.android.apps.healthdata"
    const val NOW_PLAYING_PACKAGE_NAME = "com.google.android.apps.pixel.nowplaying"
    const val PIXEL_CUSTOMIZATION_PACKAGE_NAME =
        "com.google.android.apps.pixel.customizationbundle"
    const val AICORE_PACKAGE_NAME = "com.google.android.aicore"
    const val FIND_HUB_PACKAGE_NAME = "com.google.android.apps.adm"
    const val BARD_PACKAGE_NAME = "com.google.android.apps.bard"
    const val ANDROID_SYSTEM_INTELLIGENCE_PACKAGE_NAME = "com.google.android.as"
    const val DIAGNOSTICS_TOOL_PACKAGE_NAME = "com.google.android.apps.diagnosticstool"
    const val GOOGLE_MEET_PACKAGE_NAME = "com.google.android.apps.tachyon"
    const val GOOGLE_HOME_PACKAGE_NAME = "com.google.android.apps.chromecast.app"
    const val GOOGLE_ONE_PACKAGE_NAME = "com.google.android.apps.subscriptions.red"
    const val GMAIL_PACKAGE_NAME = "com.google.android.gm"
    const val GOOGLE_WALLET_PACKAGE_NAME = "com.google.android.apps.walletnfcrel"
    const val FILES_BY_GOOGLE_PACKAGE_NAME = "com.google.android.apps.nbu.files"
    const val GOOGLE_CAMERA_PACKAGE_NAME = "com.google.android.GoogleCamera"
    const val PIXEL_CREATIVE_ASSISTANT_PACKAGE_NAME = "com.google.android.apps.pixel.creativeassistant"
    const val PIXEL_AGENT_PACKAGE_NAME = "com.google.android.apps.pixel.agent"
    const val GOOGLE_DRIVE_PACKAGE_NAME = "com.google.android.apps.docs"
    const val TAILWIND_PACKAGE_NAME = "com.google.android.apps.labs.language.tailwind"
    const val PIXEL_AURELIUS_PACKAGE_NAME = "com.google.android.apps.pixel.aurelius"
    const val WHISK_PACKAGE_NAME = "com.google.android.apps.labs.whisk"
    const val GIANT_PACKAGE_NAME = "com.google.android.apps.giant"
    const val FAMILY_LINK_PACKAGE_NAME = "com.google.android.apps.kids.familylink"

    const val VENDING_REGULAR_FLAGS_PACKAGE_NAME = "com.google.android.finsky.regular"
    const val VENDING_STABLE_FLAGS_PACKAGE_NAME = "com.google.android.finsky.stable"
    const val VENDING_STABLE_NEW_FLAGS_PACKAGE_NAME = "com.google.android.finsky.stable_new"
    const val GOOGLE_APP_FLAGS_PACKAGE_NAME = "com.google.android.libraries.search.googleapp.user#com.google.android.googlequicksearchbox"
    const val CALENDAR_FLAGS_PACKAGE_NAME = CALENDAR_PACKAGE_NAME
    const val DIALER_FLAGS_PACKAGE_NAME = DIALER_PACKAGE_NAME
    const val FITBIT_FLAGS_PACKAGE_NAME = "fitbit_android#com.fitbit.FitbitMobile"
    const val KEEP_FLAGS_PACKAGE_NAME = "com.google.android.keep#com.google.android.keep"
    const val MESSAGES_FLAGS_PACKAGE_NAME = "$MESSAGES_PACKAGE_NAME#$MESSAGES_PACKAGE_NAME"
    const val MAPS_FLAGS_PACKAGE_NAME = "com.google.geo.apps#$MAPS_PACKAGE_NAME"
    const val DESKCLOCK_FLAGS_PACKAGE_NAME = "$DESKCLOCK_PACKAGE_NAME#$DESKCLOCK_PACKAGE_NAME"
    const val TASKS_FLAGS_PACKAGE_NAME = "$TASKS_PACKAGE_NAME#$TASKS_PACKAGE_NAME"
    const val CONTACTS_FLAGS_PACKAGE_NAME = "$CONTACTS_PACKAGE_NAME#$CONTACTS_PACKAGE_NAME"
    const val RECORDER_FLAGS_PACKAGE_NAME = "$RECORDER_PACKAGE_NAME#$RECORDER_PACKAGE_NAME"
    const val PIXEL_LIVE_WALLPAPER_FLAGS_PACKAGE_NAME = PIXEL_LIVE_WALLPAPER_PACKAGE_NAME
    const val GBOARD_FLAGS_PACKAGE_NAME = "$GBOARD_PACKAGE_NAME#$GBOARD_PACKAGE_NAME"
    const val TIPS_FLAGS_PACKAGE_NAME = "$TIPS_PACKAGE_NAME#$TIPS_PACKAGE_NAME"
    const val PHOTOS_FLAGS_PACKAGE_NAME = PHOTOS_PACKAGE_NAME
    const val PHOTOS_MAIN_FLAGS_PACKAGE_NAME = "$PHOTOS_PACKAGE_NAME.phenotype"
    const val GOOGLE_MEET_FLAGS_PACKAGE_NAME =
        "com.google.android.libraries.communications.conference.device#$GOOGLE_MEET_PACKAGE_NAME"
    const val GOOGLE_HOME_FLAGS_PACKAGE_NAME =
        "com.google.android.libraries.home.phenotype#$GOOGLE_HOME_PACKAGE_NAME"
    const val GOOGLE_ONE_FLAGS_PACKAGE_NAME =
        "$GOOGLE_ONE_PACKAGE_NAME.device#$GOOGLE_ONE_PACKAGE_NAME"
    const val GMAIL_FLAGS_PACKAGE_NAME =
        "com.google.android.libraries.communications.conference.device#$GMAIL_PACKAGE_NAME"
    const val GOOGLE_WALLET_FLAGS_PACKAGE_NAME =
        "$GOOGLE_WALLET_PACKAGE_NAME#$GOOGLE_WALLET_PACKAGE_NAME"
    const val FILES_BY_GOOGLE_FLAGS_PACKAGE_NAME =
        "$FILES_BY_GOOGLE_PACKAGE_NAME.device#$FILES_BY_GOOGLE_PACKAGE_NAME"
    const val GOOGLE_CAMERA_FLAGS_PACKAGE_NAME =
        "com.google.android.apps.camera#$GOOGLE_CAMERA_PACKAGE_NAME"
    const val GOOGLE_CAMERA_ALT_FLAGS_PACKAGE_NAME = "google_camera_app"
    const val PIXEL_CREATIVE_ASSISTANT_FLAGS_PACKAGE_NAME = "pixel_creativeassistant.device#$PIXEL_CREATIVE_ASSISTANT_PACKAGE_NAME"
    const val PIXEL_AGENT_FLAGS_PACKAGE_NAME = "pixel_pearl_android#$PIXEL_AGENT_PACKAGE_NAME"
    const val GOOGLE_DRIVE_FLAGS_PACKAGE_NAME = "com.google.apps.drive.android#$GOOGLE_DRIVE_PACKAGE_NAME"
    const val TAILWIND_FLAGS_PACKAGE_NAME = "com.google.labs.language.tailwind.mobile#$TAILWIND_PACKAGE_NAME"
    const val PIXEL_AURELIUS_FLAGS_PACKAGE_NAME = "$PIXEL_AURELIUS_PACKAGE_NAME#$PIXEL_AURELIUS_PACKAGE_NAME"
    const val WHISK_FLAGS_PACKAGE_NAME = "labs_whisk#$WHISK_PACKAGE_NAME"
    const val GIANT_FLAGS_PACKAGE_NAME = GIANT_PACKAGE_NAME
    const val FAMILY_LINK_FLAGS_PACKAGE_NAME = "com.google.kids.familylink.flutter#$FAMILY_LINK_PACKAGE_NAME"
    const val AICORE_FLAGS_PACKAGE_NAME = "com.google.android.platform.aicore"
    const val FIND_HUB_FLAGS_PACKAGE_NAME = "com.google.android.apps.fmd#$FIND_HUB_PACKAGE_NAME"
    const val BARD_FLAGS_PACKAGE_NAME = "${BARD_PACKAGE_NAME}.device#$BARD_PACKAGE_NAME"

    private val supportedApplications = setOf(
        VENDING_PACKAGE_NAME,
        GOOGLE_APP_PACKAGE_NAME,
        CALENDAR_PACKAGE_NAME,
        DIALER_PACKAGE_NAME,
        FITBIT_PACKAGE_NAME,
        TRANSLATE_PACKAGE_NAME,
        KEEP_PACKAGE_NAME,
        MESSAGES_PACKAGE_NAME,
        MAPS_PACKAGE_NAME,
        DESKCLOCK_PACKAGE_NAME,
        TASKS_PACKAGE_NAME,
        CONTACTS_PACKAGE_NAME,
        RECORDER_PACKAGE_NAME,
        PIXEL_LIVE_WALLPAPER_PACKAGE_NAME,
        GBOARD_PACKAGE_NAME,
        TIPS_PACKAGE_NAME,
        PHOTOS_PACKAGE_NAME,
        WEATHER_PACKAGE_NAME,
        GOOGLE_TV_PACKAGE_NAME,
        WEAR_COMPANION_PACKAGE_NAME,
        CREDENTIAL_MANAGER_PACKAGE_NAME,
        DIGITAL_WELLBEING_PACKAGE_NAME,
        SAFETY_HUB_PACKAGE_NAME,
        HEALTH_CONNECT_PACKAGE_NAME,
        NOW_PLAYING_PACKAGE_NAME,
        PIXEL_CUSTOMIZATION_PACKAGE_NAME,
        AICORE_PACKAGE_NAME,
        FIND_HUB_PACKAGE_NAME,
        BARD_PACKAGE_NAME,
        ANDROID_SYSTEM_INTELLIGENCE_PACKAGE_NAME,
        DIAGNOSTICS_TOOL_PACKAGE_NAME,
        GOOGLE_MEET_PACKAGE_NAME,
        GOOGLE_HOME_PACKAGE_NAME,
        GOOGLE_ONE_PACKAGE_NAME,
        GMAIL_PACKAGE_NAME,
        GOOGLE_WALLET_PACKAGE_NAME,
        FILES_BY_GOOGLE_PACKAGE_NAME,
        GOOGLE_CAMERA_PACKAGE_NAME,
        PIXEL_CREATIVE_ASSISTANT_PACKAGE_NAME,
        PIXEL_AGENT_PACKAGE_NAME,
        GOOGLE_DRIVE_PACKAGE_NAME,
        TAILWIND_PACKAGE_NAME,
        PIXEL_AURELIUS_PACKAGE_NAME,
        WHISK_PACKAGE_NAME,
        GIANT_PACKAGE_NAME,
        FAMILY_LINK_PACKAGE_NAME,
    )

    private val runtimeTargetOverrides = mapOf(
        VENDING_REGULAR_FLAGS_PACKAGE_NAME to VENDING_PACKAGE_NAME,
        VENDING_STABLE_FLAGS_PACKAGE_NAME to VENDING_PACKAGE_NAME,
        VENDING_STABLE_NEW_FLAGS_PACKAGE_NAME to VENDING_PACKAGE_NAME,
        AICORE_FLAGS_PACKAGE_NAME to AICORE_PACKAGE_NAME,
    )

    override fun supportedApplicationPackageNames(): Set<String> = supportedApplications

    override fun preferredFlagPackageName(androidPackageName: String): String? = when (
        androidPackageName
    ) {
        VENDING_PACKAGE_NAME -> VENDING_STABLE_FLAGS_PACKAGE_NAME
        GOOGLE_APP_PACKAGE_NAME -> GOOGLE_APP_FLAGS_PACKAGE_NAME
        CALENDAR_PACKAGE_NAME -> CALENDAR_FLAGS_PACKAGE_NAME
        DIALER_PACKAGE_NAME -> DIALER_FLAGS_PACKAGE_NAME
        FITBIT_PACKAGE_NAME -> FITBIT_FLAGS_PACKAGE_NAME
        TRANSLATE_PACKAGE_NAME -> TRANSLATE_PACKAGE_NAME
        KEEP_PACKAGE_NAME -> KEEP_FLAGS_PACKAGE_NAME
        MESSAGES_PACKAGE_NAME -> MESSAGES_FLAGS_PACKAGE_NAME
        MAPS_PACKAGE_NAME -> MAPS_FLAGS_PACKAGE_NAME
        DESKCLOCK_PACKAGE_NAME -> DESKCLOCK_FLAGS_PACKAGE_NAME
        TASKS_PACKAGE_NAME -> TASKS_FLAGS_PACKAGE_NAME
        CONTACTS_PACKAGE_NAME -> CONTACTS_FLAGS_PACKAGE_NAME
        RECORDER_PACKAGE_NAME -> RECORDER_FLAGS_PACKAGE_NAME
        PIXEL_LIVE_WALLPAPER_PACKAGE_NAME -> PIXEL_LIVE_WALLPAPER_FLAGS_PACKAGE_NAME
        GBOARD_PACKAGE_NAME -> GBOARD_FLAGS_PACKAGE_NAME
        TIPS_PACKAGE_NAME -> TIPS_FLAGS_PACKAGE_NAME
        PHOTOS_PACKAGE_NAME -> PHOTOS_FLAGS_PACKAGE_NAME
        WEATHER_PACKAGE_NAME,
        GOOGLE_TV_PACKAGE_NAME,
        WEAR_COMPANION_PACKAGE_NAME,
        CREDENTIAL_MANAGER_PACKAGE_NAME,
        DIGITAL_WELLBEING_PACKAGE_NAME,
        SAFETY_HUB_PACKAGE_NAME,
        HEALTH_CONNECT_PACKAGE_NAME,
        NOW_PLAYING_PACKAGE_NAME,
        PIXEL_CUSTOMIZATION_PACKAGE_NAME,
        ANDROID_SYSTEM_INTELLIGENCE_PACKAGE_NAME,
        DIAGNOSTICS_TOOL_PACKAGE_NAME -> androidPackageName
        AICORE_PACKAGE_NAME -> AICORE_FLAGS_PACKAGE_NAME
        FIND_HUB_PACKAGE_NAME -> FIND_HUB_FLAGS_PACKAGE_NAME
        BARD_PACKAGE_NAME -> BARD_FLAGS_PACKAGE_NAME
        GOOGLE_MEET_PACKAGE_NAME -> GOOGLE_MEET_FLAGS_PACKAGE_NAME
        GOOGLE_HOME_PACKAGE_NAME -> GOOGLE_HOME_FLAGS_PACKAGE_NAME
        GOOGLE_ONE_PACKAGE_NAME -> GOOGLE_ONE_FLAGS_PACKAGE_NAME
        GMAIL_PACKAGE_NAME -> GMAIL_FLAGS_PACKAGE_NAME
        GOOGLE_WALLET_PACKAGE_NAME -> GOOGLE_WALLET_FLAGS_PACKAGE_NAME
        FILES_BY_GOOGLE_PACKAGE_NAME -> FILES_BY_GOOGLE_FLAGS_PACKAGE_NAME
        GOOGLE_CAMERA_PACKAGE_NAME -> GOOGLE_CAMERA_FLAGS_PACKAGE_NAME
        PIXEL_CREATIVE_ASSISTANT_PACKAGE_NAME -> PIXEL_CREATIVE_ASSISTANT_FLAGS_PACKAGE_NAME
        PIXEL_AGENT_PACKAGE_NAME -> PIXEL_AGENT_FLAGS_PACKAGE_NAME
        GOOGLE_DRIVE_PACKAGE_NAME -> GOOGLE_DRIVE_FLAGS_PACKAGE_NAME
        TAILWIND_PACKAGE_NAME -> TAILWIND_FLAGS_PACKAGE_NAME
        PIXEL_AURELIUS_PACKAGE_NAME -> PIXEL_AURELIUS_FLAGS_PACKAGE_NAME
        WHISK_PACKAGE_NAME -> WHISK_FLAGS_PACKAGE_NAME
        GIANT_PACKAGE_NAME -> GIANT_FLAGS_PACKAGE_NAME
        FAMILY_LINK_PACKAGE_NAME -> FAMILY_LINK_FLAGS_PACKAGE_NAME
        else -> null
    }

    override fun knownFlagPackageNames(androidPackageName: String): Set<String> = when (
        androidPackageName
    ) {
        PHOTOS_PACKAGE_NAME -> setOf(PHOTOS_MAIN_FLAGS_PACKAGE_NAME)
        GOOGLE_CAMERA_PACKAGE_NAME -> setOf(GOOGLE_CAMERA_ALT_FLAGS_PACKAGE_NAME)
        else -> emptySet()
    }

    fun isSupportedApplication(packageName: String): Boolean = packageName in supportedApplications

    fun isSupportedProcess(packageName: String, processName: String?): Boolean {
        if (!isSupportedApplication(packageName)) return false
        return packageName != VENDING_PACKAGE_NAME || processName == VENDING_PACKAGE_NAME
    }

    fun runtimeTargetPackageForFlagPackage(flagPackageName: String): String {
        if ('#' in flagPackageName) return flagPackageName.substringAfterLast('#')

        return runtimeTargetOverrides[flagPackageName] ?: flagPackageName
    }
}
