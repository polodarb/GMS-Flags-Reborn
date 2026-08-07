plugins { alias(libs.plugins.android.library); alias(libs.plugins.kotlin.compose) }
android {
    namespace = "ua.polodarb.gmsflags.presentation.core.ui"; compileSdk = 37
    defaultConfig { minSdk = 29 }; buildFeatures { compose = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_11; targetCompatibility = JavaVersion.VERSION_11 }
}
dependencies {
    implementation(project(":presentation-core"))
    api(platform(libs.androidx.compose.bom)); api(libs.androidx.compose.ui); api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.animation.graphics)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
