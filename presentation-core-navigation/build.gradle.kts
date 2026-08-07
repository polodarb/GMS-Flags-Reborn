plugins { alias(libs.plugins.android.library); alias(libs.plugins.kotlin.serialization) }
android {
    namespace = "ua.polodarb.gmsflags.presentation.core.navigation"; compileSdk = 37
    defaultConfig { minSdk = 29 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_11; targetCompatibility = JavaVersion.VERSION_11 }
}
dependencies {
    api(libs.androidx.navigation3.runtime)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.animation)
    implementation(libs.kotlinx.serialization.core)
    testImplementation(libs.junit)
}
