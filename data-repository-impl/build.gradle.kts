plugins { alias(libs.plugins.android.library) }
android {
    namespace = "ua.polodarb.gmsflags.data.repository.impl"; compileSdk = 37
    defaultConfig { minSdk = 29 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_11; targetCompatibility = JavaVersion.VERSION_11 }
}
dependencies {
    implementation(project(":core-root")); implementation(project(":core-xposed-info")); implementation(project(":domain")); implementation(project(":data-network")); implementation(project(":data-repository")); implementation(libs.koin.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
