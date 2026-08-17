package ua.polodarb.xposed.info

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class XposedTargetsTest {
    @Test
    fun `supports configured runtime targets`() {
        assertEquals(
            setOf(
                "com.android.vending",
                "com.google.android.googlequicksearchbox",
                "com.google.android.calendar",
                "com.google.android.dialer",
                "com.fitbit.FitbitMobile",
                "com.google.android.apps.translate",
                "com.google.android.keep",
                "com.google.android.apps.messaging",
                "com.google.android.apps.maps",
                "com.google.android.deskclock",
                "com.google.android.apps.tasks",
                "com.google.android.contacts",
                "com.google.android.apps.recorder",
                "com.google.pixel.livewallpaper",
                "com.google.android.inputmethod.latin",
                "com.google.android.apps.tips",
                "com.google.android.apps.photos",
                "com.google.android.apps.weather",
                "com.google.android.videos",
                "com.google.android.apps.wear.companion",
                "com.google.android.apps.credentialmanager",
                "com.google.android.apps.wellbeing",
                "com.google.android.apps.safetyhub",
                "com.google.android.apps.healthdata",
                "com.google.android.apps.pixel.nowplaying",
                "com.google.android.apps.pixel.customizationbundle",
                "com.google.android.aicore",
                "com.google.android.apps.adm",
                "com.google.android.apps.bard",
                "com.google.android.as",
                "com.google.android.apps.diagnosticstool",
                "com.google.android.apps.tachyon",
                "com.google.android.apps.chromecast.app",
                "com.google.android.apps.subscriptions.red",
                "com.google.android.gm",
                "com.google.android.apps.walletnfcrel",
                "com.google.android.apps.nbu.files",
                "com.google.android.GoogleCamera",
                "com.google.android.apps.docs",
                "com.google.android.apps.giant",
                "com.google.android.apps.kids.familylink",
                "com.google.android.apps.labs.language.tailwind",
                "com.google.android.apps.labs.whisk",
                "com.google.android.apps.pixel.agent",
                "com.google.android.apps.pixel.aurelius",
                "com.google.android.apps.pixel.creativeassistant",
                "com.google.android.youtube",
                "com.google.android.apps.youtube.music",
            ),
            XposedTargets.supportedApplicationPackageNames(),
        )
        assertTrue(XposedTargets.isSupportedApplication("com.android.vending"))
        assertTrue(XposedTargets.isSupportedApplication("com.google.android.googlequicksearchbox"))
        assertTrue(XposedTargets.isSupportedApplication("com.google.android.calendar"))
        assertTrue(XposedTargets.isSupportedApplication("com.google.android.dialer"))
        assertTrue(XposedTargets.isSupportedApplication("com.fitbit.FitbitMobile"))
        assertTrue(XposedTargets.isSupportedApplication("com.google.android.apps.translate"))
        assertTrue(XposedTargets.isSupportedApplication("com.google.android.keep"))
        assertTrue(XposedTargets.isSupportedApplication("com.google.android.inputmethod.latin"))
        assertTrue(XposedTargets.isSupportedApplication("com.google.android.apps.weather"))
        assertTrue(XposedTargets.isSupportedApplication("com.google.android.as"))
        assertTrue(
            XposedTargets.isSupportedApplication("com.google.android.apps.diagnosticstool")
        )
        assertTrue(XposedTargets.isSupportedApplication("com.google.android.apps.tachyon"))
        assertTrue(XposedTargets.isSupportedApplication("com.google.android.gm"))
        assertTrue(
            XposedTargets.isSupportedApplication("com.google.android.apps.walletnfcrel")
        )
        assertTrue(XposedTargets.isSupportedApplication("com.google.android.GoogleCamera"))
        assertFalse(XposedTargets.isSupportedApplication("com.google.android.apps.wallpaper"))
        assertFalse(XposedTargets.isSupportedApplication("com.android.chrome"))
    }

    @Test
    fun `hooks only the main Vending process`() {
        assertTrue(
            XposedTargets.isSupportedProcess("com.android.vending", "com.android.vending")
        )
        assertFalse(
            XposedTargets.isSupportedProcess(
                "com.android.vending",
                "com.android.vending:background",
            )
        )
        assertTrue(
            XposedTargets.isSupportedProcess(
                "com.google.android.apps.translate",
                "com.google.android.apps.translate:background",
            )
        )
    }

    @Test
    fun `hooks only the main process of a Mendel application`() {
        assertTrue(
            XposedTargets.isSupportedProcess(
                "com.google.android.youtube",
                "com.google.android.youtube",
            )
        )
        assertFalse(
            XposedTargets.isSupportedProcess(
                "com.google.android.youtube",
                "com.google.android.youtube:player",
            )
        )
    }

    @Test
    fun `routes Mendel applications to their own override identity`() {
        assertEquals(
            setOf(
                "com.google.android.youtube",
                "com.google.android.apps.youtube.music",
            ),
            XposedTargets.mendelApplicationPackageNames(),
        )
        assertTrue(XposedTargets.isMendelApplication(XposedTargets.YOUTUBE_PACKAGE_NAME))
        assertFalse(XposedTargets.isMendelApplication(XposedTargets.MAPS_PACKAGE_NAME))
        assertEquals(
            "mendel#com.google.android.youtube",
            XposedTargets.preferredFlagPackageName(XposedTargets.YOUTUBE_PACKAGE_NAME),
        )
        assertEquals(
            "mendel#com.google.android.apps.youtube.music",
            XposedTargets.preferredFlagPackageName(XposedTargets.YOUTUBE_MUSIC_PACKAGE_NAME),
        )
        assertEquals(
            XposedTargets.YOUTUBE_PACKAGE_NAME,
            XposedTargets.runtimeTargetPackageForFlagPackage(
                XposedTargets.YOUTUBE_FLAGS_PACKAGE_NAME,
            ),
        )
    }

    @Test
    fun `uses stable Finsky flags as the initial Vending package`() {
        assertEquals(
            "com.google.android.finsky.stable",
            XposedTargets.preferredFlagPackageName("com.android.vending"),
        )
    }

    @Test
    fun `uses application-specific primary flag packages`() {
        assertEquals(
            XposedTargets.GOOGLE_APP_FLAGS_PACKAGE_NAME,
            XposedTargets.preferredFlagPackageName(XposedTargets.GOOGLE_APP_PACKAGE_NAME),
        )
        assertEquals(
            XposedTargets.CALENDAR_FLAGS_PACKAGE_NAME,
            XposedTargets.preferredFlagPackageName(XposedTargets.CALENDAR_PACKAGE_NAME),
        )
        assertEquals(
            XposedTargets.DIALER_FLAGS_PACKAGE_NAME,
            XposedTargets.preferredFlagPackageName(XposedTargets.DIALER_PACKAGE_NAME),
        )
        assertEquals(
            XposedTargets.FITBIT_FLAGS_PACKAGE_NAME,
            XposedTargets.preferredFlagPackageName(XposedTargets.FITBIT_PACKAGE_NAME),
        )
        assertEquals(
            XposedTargets.GBOARD_FLAGS_PACKAGE_NAME,
            XposedTargets.preferredFlagPackageName(XposedTargets.GBOARD_PACKAGE_NAME),
        )
        assertEquals(
            XposedTargets.CREDENTIAL_MANAGER_PACKAGE_NAME,
            XposedTargets.preferredFlagPackageName(
                XposedTargets.CREDENTIAL_MANAGER_PACKAGE_NAME,
            ),
        )
        assertEquals(
            XposedTargets.GOOGLE_MEET_FLAGS_PACKAGE_NAME,
            XposedTargets.preferredFlagPackageName(XposedTargets.GOOGLE_MEET_PACKAGE_NAME),
        )
        assertEquals(
            XposedTargets.GOOGLE_HOME_FLAGS_PACKAGE_NAME,
            XposedTargets.preferredFlagPackageName(XposedTargets.GOOGLE_HOME_PACKAGE_NAME),
        )
        assertEquals(
            XposedTargets.GOOGLE_ONE_FLAGS_PACKAGE_NAME,
            XposedTargets.preferredFlagPackageName(XposedTargets.GOOGLE_ONE_PACKAGE_NAME),
        )
        assertEquals(
            XposedTargets.GMAIL_FLAGS_PACKAGE_NAME,
            XposedTargets.preferredFlagPackageName(XposedTargets.GMAIL_PACKAGE_NAME),
        )
        assertEquals(
            XposedTargets.GOOGLE_WALLET_FLAGS_PACKAGE_NAME,
            XposedTargets.preferredFlagPackageName(
                XposedTargets.GOOGLE_WALLET_PACKAGE_NAME,
            ),
        )
        assertEquals(
            XposedTargets.FILES_BY_GOOGLE_FLAGS_PACKAGE_NAME,
            XposedTargets.preferredFlagPackageName(
                XposedTargets.FILES_BY_GOOGLE_PACKAGE_NAME,
            ),
        )
        assertEquals(
            XposedTargets.GOOGLE_CAMERA_FLAGS_PACKAGE_NAME,
            XposedTargets.preferredFlagPackageName(
                XposedTargets.GOOGLE_CAMERA_PACKAGE_NAME,
            ),
        )
    }

    @Test
    fun `prefers the app own config package, not an auxiliary one`() {
        assertEquals(
            "com.google.android.apps.photos",
            XposedTargets.preferredFlagPackageName(XposedTargets.PHOTOS_PACKAGE_NAME),
        )
        assertEquals(
            "com.google.pixel.livewallpaper",
            XposedTargets.preferredFlagPackageName(
                XposedTargets.PIXEL_LIVE_WALLPAPER_PACKAGE_NAME,
            ),
        )
    }

    @Test
    fun `also lists Photos' real flag package even though the device registry never discovers it`() {
        assertEquals(
            setOf("com.google.android.apps.photos.phenotype"),
            XposedTargets.knownFlagPackageNames(XposedTargets.PHOTOS_PACKAGE_NAME),
        )
        assertEquals(
            emptySet<String>(),
            XposedTargets.knownFlagPackageNames(XposedTargets.PIXEL_LIVE_WALLPAPER_PACKAGE_NAME),
        )
    }

    @Test
    fun `also lists Google Camera's renamed flag package from newer builds`() {
        assertEquals(
            setOf("google_camera_app"),
            XposedTargets.knownFlagPackageNames(XposedTargets.GOOGLE_CAMERA_PACKAGE_NAME),
        )
    }

    @Test
    fun `maps only known flag packages to supported applications`() {
        assertEquals(
            "com.android.vending",
            XposedTargets.runtimeTargetPackageForFlagPackage(
                "com.google.android.finsky.regular"
            ),
        )
        assertEquals(
            "com.android.vending",
            XposedTargets.runtimeTargetPackageForFlagPackage(
                "com.google.android.libraries.onegoogle.consent#com.android.vending"
            ),
        )
        assertEquals(
            "com.google.android.keep",
            XposedTargets.runtimeTargetPackageForFlagPackage(
                "com.google.android.keep#com.google.android.keep"
            ),
        )
        assertEquals(
            "com.google.android.apps.translate",
            XposedTargets.runtimeTargetPackageForFlagPackage("com.google.android.apps.translate"),
        )
        assertEquals(
            "unsupported.package",
            XposedTargets.runtimeTargetPackageForFlagPackage("unsupported.package"),
        )
    }
}
