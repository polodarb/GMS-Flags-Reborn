plugins { alias(libs.plugins.android.library); alias(libs.plugins.kotlin.compose) }
android {
    namespace = "ua.polodarb.gmsflags.presentation.feature.apps"; compileSdk = 37
    defaultConfig { minSdk = 29 }; buildFeatures { compose = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_11; targetCompatibility = JavaVersion.VERSION_11 }
}
androidComponents {
    onVariants(selector().withBuildType("debug")) { variant ->
        val internalDir = layout.projectDirectory.dir("src/debug-internal/java")
        val publicDir = layout.projectDirectory.dir("src/debug-public/java")
        variant.sources.java?.addStaticSourceDirectory(
            (if (internalDir.asFile.exists()) internalDir else publicDir).asFile.absolutePath,
        )
    }
}
dependencies {
    implementation(project(":domain")); implementation(project(":presentation-core")); implementation(project(":presentation-core-ui")); implementation(project(":presentation-core-navigation"))
    implementation(platform(libs.androidx.compose.bom)); implementation(libs.androidx.compose.ui); implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose); implementation(libs.koin.compose.viewmodel); implementation(libs.koin.android)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
