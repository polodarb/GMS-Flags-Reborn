plugins { alias(libs.plugins.android.library); alias(libs.plugins.kotlin.compose) }
android {
    namespace = "ua.polodarb.gmsflags.presentation.core"; compileSdk = 37
    defaultConfig { minSdk = 29 }; buildFeatures { compose = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_11; targetCompatibility = JavaVersion.VERSION_11 }
}
dependencies {
    implementation(project(":domain"))
    api(project(":core-analytics"))
    implementation(platform(libs.androidx.compose.bom)); implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.lifecycle.viewmodel.compose); implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
