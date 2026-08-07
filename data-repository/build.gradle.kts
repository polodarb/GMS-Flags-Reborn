plugins { alias(libs.plugins.android.library) }
android {
    namespace = "ua.polodarb.gmsflags.data.repository"; compileSdk = 37
    defaultConfig { minSdk = 29 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_11; targetCompatibility = JavaVersion.VERSION_11 }
}
dependencies {
    api(project(":domain"))
    implementation(libs.kotlinx.coroutines.core)
}
