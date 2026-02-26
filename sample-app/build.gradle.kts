import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  id("katatui-quality")
  alias(libs.plugins.kotlin.multiplatform)
}

val isMac = System.getProperty("os.name").startsWith("Mac")

val rustTriples =
  mapOf(
    "macosArm64" to "aarch64-apple-darwin",
    "macosX64" to "x86_64-apple-darwin",
    "linuxX64" to "x86_64-unknown-linux-gnu",
    "linuxArm64" to "aarch64-unknown-linux-gnu",
    "mingwX64" to "x86_64-pc-windows-gnu",
  )

kotlin {
  if (isMac) {
    macosArm64 { binaries.executable { entryPoint = "com.hyeonslab.katatui.sample.main" } }
    macosX64 { binaries.executable { entryPoint = "com.hyeonslab.katatui.sample.main" } }
  }
  linuxX64 { binaries.executable { entryPoint = "com.hyeonslab.katatui.sample.main" } }
  linuxArm64 { binaries.executable { entryPoint = "com.hyeonslab.katatui.sample.main" } }
  mingwX64 { binaries.executable { entryPoint = "com.hyeonslab.katatui.sample.main" } }
  applyDefaultHierarchyTemplate()

  targets.withType<KotlinNativeTarget>().configureEach {
    val triple = rustTriples[name] ?: return@configureEach
    binaries.all {
      linkerOpts("-L${rootDir}/katatui-ffi/target/$triple/release", "-lkatatui_ffi")
      if (name.contains("mingw", ignoreCase = true)) {
        linkerOpts("-lws2_32", "-lbcrypt", "-lntdll", "-luserenv")
      }
    }
  }

  sourceSets { nativeMain.dependencies { implementation(project(":katatui")) } }

  compilerOptions { allWarningsAsErrors.set(true) }
}
