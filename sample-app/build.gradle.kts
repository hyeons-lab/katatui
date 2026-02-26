import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("katatui-quality")
    alias(libs.plugins.kotlin.multiplatform)
}

val isMac = System.getProperty("os.name").startsWith("Mac")

kotlin {
    if (isMac) {
        macosArm64 { binaries.executable { entryPoint = "com.hyeonslab.katatui.sample.main" } }
        macosX64 { binaries.executable { entryPoint = "com.hyeonslab.katatui.sample.main" } }
    }
    linuxX64 { binaries.executable { entryPoint = "com.hyeonslab.katatui.sample.main" } }
    linuxArm64 { binaries.executable { entryPoint = "com.hyeonslab.katatui.sample.main" } }
    mingwX64 { binaries.executable { entryPoint = "com.hyeonslab.katatui.sample.main" } }
    applyDefaultHierarchyTemplate()

    sourceSets {
        nativeMain.dependencies {
            implementation(project(":katatui"))
        }
    }

    compilerOptions { allWarningsAsErrors.set(true) }
}
