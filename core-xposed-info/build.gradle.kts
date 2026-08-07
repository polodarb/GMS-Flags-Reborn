plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.secrets.gradle.plugin)
}

secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"
}

android {
    namespace = "ua.polodarb.xposed.info"; compileSdk = 37
    defaultConfig {
        minSdk = 29
    }
    buildFeatures { buildConfig = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_11; targetCompatibility = JavaVersion.VERSION_11 }
}
dependencies {
    api(libs.kotlinx.serialization.core)
    api(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
}
