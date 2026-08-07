plugins { alias(libs.plugins.android.library) }

android {
    namespace = "ua.polodarb.gmsflags.core.root"
    compileSdk = 37
    defaultConfig { minSdk = 29 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_11; targetCompatibility = JavaVersion.VERSION_11 }
}
dependencies {
    implementation(libs.libsu.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.koin.android)
    testImplementation(libs.junit)
}
