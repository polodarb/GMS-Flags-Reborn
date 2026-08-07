plugins { alias(libs.plugins.android.library) }
android {
    namespace = "ua.polodarb.xposed"; compileSdk = 37
    defaultConfig { minSdk = 29 }
    buildFeatures { buildConfig = true }
    buildTypes {
        debug {
            buildConfigField("String", "MAIN_APPLICATION_ID", "\"ua.polodarb.gmsflags\"")
        }

        release {
            buildConfigField("String", "MAIN_APPLICATION_ID", "\"ua.polodarb.gmsflags\"")
        }
    }
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
    compileOnly(libs.xposed.api)
    implementation(libs.dexkit)
    implementation(project(":core-xposed-info"))
    testImplementation(libs.junit)
}
