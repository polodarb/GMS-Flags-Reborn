plugins { alias(libs.plugins.android.library) }

android {
    namespace = "ua.polodarb.gmsflags.data.phenotype.impl"
    compileSdk = 37
    defaultConfig { minSdk = 29 }
    buildFeatures { aidl = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_11; targetCompatibility = JavaVersion.VERSION_11 }
}

dependencies {
    implementation(project(":core-root"))
    implementation(project(":data-repository"))
    implementation(project(":core-xposed-info"))
    implementation(libs.libsu.core)
    implementation(libs.libsu.service)
    implementation(libs.requery.sqlite)
    implementation(libs.protobuf.javalite)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.android)
    testImplementation(libs.junit)
}
