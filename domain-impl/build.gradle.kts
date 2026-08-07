plugins { alias(libs.plugins.android.library) }
android {
    namespace = "ua.polodarb.gmsflags.domain.impl"; compileSdk = 37
    defaultConfig { minSdk = 29 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_11; targetCompatibility = JavaVersion.VERSION_11 }
}
dependencies {
    implementation(project(":domain")); implementation(project(":data-repository")); implementation(project(":core-root"))
    implementation(project(":core-xposed-info"))
    implementation(libs.koin.android); implementation(libs.kotlinx.coroutines.core); testImplementation(libs.junit)
}
