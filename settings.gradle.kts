pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "katatui"

include(":katatui")
include(":codegen")
include(":sample-app")
// katatui-ffi is a Cargo workspace — NOT a Gradle subproject
