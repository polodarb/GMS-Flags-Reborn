plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}
android {
    namespace = "ua.polodarb.gmsflags.data.network"; compileSdk = 37
    defaultConfig { minSdk = 29 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_11; targetCompatibility = JavaVersion.VERSION_11 }
}

dependencies {
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
}
