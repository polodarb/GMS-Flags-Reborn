plugins { alias(libs.plugins.android.library) }
android {
    namespace = "ua.polodarb.gmsflags.data.network.impl"; compileSdk = 37
    defaultConfig { minSdk = 29 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_11; targetCompatibility = JavaVersion.VERSION_11 }
}
dependencies {
    implementation(project(":data-network"))
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.koin.android)

    // In-app HTTP inspector. Real implementation in debug; no-op stub in release keeps the same API.
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.no.op)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.ktor.client.mock)
}
