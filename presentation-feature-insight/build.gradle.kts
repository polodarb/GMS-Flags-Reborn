plugins { alias(libs.plugins.android.library); alias(libs.plugins.kotlin.compose) }
android {
    namespace = "ua.polodarb.gmsflags.presentation.feature.insight"; compileSdk = 37
    defaultConfig { minSdk = 29 }; buildFeatures { compose = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_11; targetCompatibility = JavaVersion.VERSION_11 }
}
dependencies {
    implementation(project(":presentation-core")); implementation(project(":presentation-core-ui")); implementation(project(":presentation-core-navigation"))
    implementation(platform(libs.androidx.compose.bom)); implementation(libs.androidx.compose.ui); implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.koin.compose.viewmodel); implementation(libs.koin.android)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
