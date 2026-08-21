plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.secrets.gradle.plugin)
}

secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"
}

android {
    namespace = "ua.polodarb.gmsflags"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "ua.polodarb.gmsflags.reborn"
        minSdk = 29
        targetSdk = 37
        versionCode = 107
        versionName = "1.1.1"
        val serverBaseUrl = providers.gradleProperty("GMS_FLAGS_API_BASE_URL")
            .getOrElse("https://api.polodarb.com/gmsflags/v1")
        buildConfigField("String", "SERVER_BASE_URL", "\"$serverBaseUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
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
    implementation(project(":core-xposed-info"))
    implementation(project(":presentation-core-ui"))
    implementation(project(":core-root"))
    implementation(project(":xposed"))
    implementation(project(":data-phenotype-impl"))
    implementation(project(":data-repository"))
    implementation(project(":data-repository-impl"))
    implementation(project(":data-network-impl"))
    implementation(project(":data-network"))
    implementation(project(":domain"))
    implementation(project(":domain-impl"))
    implementation(project(":presentation-core"))
    implementation(project(":presentation-core-navigation"))
    implementation(project(":presentation-feature-suggestions"))
    implementation(project(":presentation-feature-apps"))
    implementation(project(":presentation-feature-flags-list"))
    implementation(project(":presentation-feature-insight"))
    implementation(project(":presentation-feature-hook-status"))
    implementation(project(":presentation-feature-settings"))
    implementation(project(":presentation-feature-onboarding"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose.viewmodel)

    implementation(project(":core-analytics"))
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
    implementation(libs.firebase.config)
    configurations.all {
        exclude(group = "com.google.firebase", module = "protolite-well-known-types")
    }

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
