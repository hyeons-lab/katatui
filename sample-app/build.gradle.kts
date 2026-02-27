import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

// ---- Embedded resources ----
// Reads binary resource files at build time and emits a generated Kotlin source file containing
// each file as a ByteArray constant. The binary is then fully self-contained.
//
// Abstract task class — required for configuration-cache compatibility; lambdas/closures
// defined directly in the build script cannot be serialized by the configuration cache.

abstract class EmbedResources : DefaultTask() {
  @get:InputFile
  @get:PathSensitive(PathSensitivity.NONE)
  abstract val leapDarkPng: RegularFileProperty

  @get:OutputDirectory abstract val outputDir: DirectoryProperty

  @TaskAction
  fun generate() {
    val sb = StringBuilder()
    sb.appendLine("// Generated — do not edit")
    sb.appendLine("package com.hyeonslab.katatui.sample")
    sb.appendLine()
    embed(sb, "LEAP_DARK_PNG", leapDarkPng.get().asFile)
    val out = outputDir.get().file("com/hyeonslab/katatui/sample/Resources.kt").asFile
    out.parentFile.mkdirs()
    out.writeText(sb.toString())
  }

  private fun embed(sb: StringBuilder, name: String, file: java.io.File) {
    val values = file.readBytes().joinToString(", ") { it.toString() }
    sb.appendLine("internal val $name: ByteArray = byteArrayOf($values)")
  }
}

val resourcesDir = layout.buildDirectory.dir("generated-sources/resources/nativeMain/kotlin")

val generateResources by
  tasks.registering(EmbedResources::class) {
    leapDarkPng.set(layout.projectDirectory.file("leap-dark.png"))
    outputDir.set(resourcesDir)
  }

plugins {
  id("katatui-quality")
  alias(libs.plugins.kotlin.multiplatform)
}

val isMac = System.getProperty("os.name").startsWith("Mac")
val isWindows = System.getProperty("os.name").startsWith("Windows")

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
  } else if (isWindows) {
    mingwX64 { binaries.executable { entryPoint = "com.hyeonslab.katatui.sample.main" } }
  } else {
    linuxX64 { binaries.executable { entryPoint = "com.hyeonslab.katatui.sample.main" } }
    linuxArm64 { binaries.executable { entryPoint = "com.hyeonslab.katatui.sample.main" } }
  }
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

  sourceSets {
    nativeMain {
      kotlin.srcDir(generateResources.map { resourcesDir.get().asFile })
      dependencies { implementation(project(":katatui")) }
    }
  }

  compilerOptions { allWarningsAsErrors.set(true) }
}
