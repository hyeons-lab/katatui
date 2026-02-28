import org.gradle.api.attributes.Category
import org.gradle.api.attributes.Usage
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
  id("katatui-quality")
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.skie)
}

// Proper configuration for accessing :codegen's runtime classpath.
// Declared here (not inline in the task) so Gradle's configuration cache can serialize it.
val codegenClasspath: Configuration by
  configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
      attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
      attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
    }
  }

dependencies { codegenClasspath(project(":codegen")) }

val isMac = System.getProperty("os.name").startsWith("Mac")

val rustTriples =
  mapOf(
    "macosArm64" to "aarch64-apple-darwin",
    "macosX64" to "x86_64-apple-darwin",
    "linuxX64" to "x86_64-unknown-linux-gnu",
    "linuxArm64" to "aarch64-unknown-linux-gnu",
    "mingwX64" to "x86_64-pc-windows-gnu",
  )

val isWindows = System.getProperty("os.name").startsWith("Windows")

// One cargo build task per target (release static lib).

// isEnabled is a simple Boolean property (not a lambda), which is configuration-cache safe.
// Cross-compilation requires toolchains not present in a typical dev setup, so we skip
// foreign targets locally; CI can enable them by providing the right toolchain.
rustTriples.forEach { (kotlinTarget, triple) ->
  val isNativeTarget =
    when {
      isMac -> kotlinTarget.startsWith("macos")
      isWindows -> kotlinTarget.startsWith("mingw")
      else -> kotlinTarget.startsWith("linux")
    }
  tasks.register<Exec>("buildKatatuiFfi_$kotlinTarget") {
    group = "rust"
    description = "Build Rust FFI staticlib for $kotlinTarget ($triple)"
    workingDir = file("../katatui-ffi")
    commandLine("cargo", "build", "--release", "--target", triple)
    outputs.file("../katatui-ffi/target/$triple/release/libkatatui_ffi.a")
    inputs.dir("../katatui-ffi/src")
    inputs.file("../katatui-ffi/Cargo.toml")
    isEnabled = isNativeTarget
  }
}

// Header-generation build (host triple only; build.rs writes katatui.h)
val buildKatatuiFfiHeader by
  tasks.registering(Exec::class) {
    group = "rust"
    description = "Build Rust FFI (debug, host only) to regenerate katatui.h via cbindgen"
    workingDir = file("../katatui-ffi")
    val hostTriple = if (isMac) "aarch64-apple-darwin" else "x86_64-unknown-linux-gnu"
    commandLine("cargo", "build", "--target", hostTriple)
    outputs.file("src/nativeInterop/cinterop/katatui.h")
    inputs.dir("../katatui-ffi/src")
    inputs.file("../katatui-ffi/cbindgen.toml")
  }

// Codegen task: parse katatui.h → emit Kotlin wrapper sources into build/ (gitignored, not
// ktfmt-checked)
val generatedSourcesDir = layout.buildDirectory.dir("generated-sources/katatui/nativeMain/kotlin")

val generateKotlinWrappers by
  tasks.registering(JavaExec::class) {
    group = "codegen"
    description = "Parse katatui.h and emit generated Kotlin wrapper classes"
    dependsOn(buildKatatuiFfiHeader)
    classpath = codegenClasspath
    mainClass.set("com.hyeonslab.katatui.codegen.MainKt")
    args(
      file("src/nativeInterop/cinterop/katatui.h").absolutePath,
      generatedSourcesDir.get().asFile.absolutePath,
    )
    inputs.file("src/nativeInterop/cinterop/katatui.h")
    outputs.dir(generatedSourcesDir)
  }

kotlin {
  if (isMac) {
    macosArm64()
    macosX64()
  } else if (isWindows) {
    mingwX64()
  } else {
    linuxX64()
    linuxArm64()
  }
  applyDefaultHierarchyTemplate()

  skie {}

  val xcf = XCFramework("Katatui")

  targets.withType<KotlinNativeTarget>().configureEach {
    val triple = rustTriples[name] ?: return@configureEach
    compilations.getByName("main") {
      cinterops.create("katatui") {
        definitionFile.set(project.file("src/nativeInterop/cinterop/katatui.def"))
        includeDirs(project.file("src/nativeInterop/cinterop"))
      }
    }
    binaries.all {
      // Always links against release: the Rust build task only produces a release static lib.
      // Run `./gradlew buildKatatuiFfi_<target>` (cargo --release) to satisfy this path.
      linkerOpts("-L${rootDir}/katatui-ffi/target/$triple/release", "-lkatatui_ffi")
      if (name.contains("mingw", ignoreCase = true)) {
        linkerOpts("-lws2_32", "-lbcrypt", "-lntdll", "-luserenv")
      }
    }
    binaries.framework {
      baseName = "Katatui"
      isStatic = true
      xcf.add(this)
    }
  }

  sourceSets {
    nativeMain { kotlin.srcDir(generateKotlinWrappers.map { it.outputs.files.singleFile }) }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotest.assertions.core)
    }
  }

  compilerOptions { allWarningsAsErrors.set(true) }
}

skie {}

// Wire cargo build + header → before cinterop task for each target.
// Cinterop task name format: cinterop<InteropName><TargetName> e.g. cinteropKatatuiMacosArm64
rustTriples.keys.forEach { target ->
  val cap = target.replaceFirstChar { it.uppercase() }
  tasks
    .matching { it.name == "cinteropKatatui$cap" }
    .configureEach {
      // Release static lib must exist before cinterop links
      dependsOn("buildKatatuiFfi_$target")
      // Header must be regenerated before cinterop reads katatui.h
      dependsOn(buildKatatuiFfiHeader)
    }
}
