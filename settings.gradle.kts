pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven("https://jitpack.io")
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        // Xposed API (IXposedHookLoadPackage, XposedBridge, XC_MethodReplacement)
        maven("https://api.xposed.info/")
    }
}

rootProject.name = "GMS Flags 2.0"
include(":core-entry")
include(":core-analytics")
include(":core-root")
include(":core-xposed-info")
include(":xposed")
include(":domain")
include(":domain-impl")
include(":data-phenotype-impl")
include(":data-repository")
include(":data-repository-impl")
include(":data-network")
include(":data-network-impl")
include(":presentation-core")
include(":presentation-core-ui")
include(":presentation-core-navigation")
include(":presentation-feature-suggestions")
include(":presentation-feature-community")
include(":presentation-feature-apps")

include(":presentation-feature-flags-list")
include(":presentation-feature-insight")
include(":presentation-feature-hook-status")
include(":presentation-feature-settings")
include(":presentation-feature-onboarding")
