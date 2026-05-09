plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ktfmt) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.maven.publish) apply false
}

val assembleKatatuiReleaseXCFramework by tasks.registering {
    dependsOn(":katatui:assembleKatatuiReleaseXCFramework")
}

val buildSwiftSample by tasks.registering(Exec::class) {
    dependsOn(assembleKatatuiReleaseXCFramework)
    workingDir("sample-app-swift")
    commandLine("swift", "build", "-c", "release")
}

val runSwiftSample by tasks.registering(Exec::class) {
    dependsOn(buildSwiftSample)
    workingDir("sample-app-swift")
    commandLine("swift", "run", "-c", "release")
}
