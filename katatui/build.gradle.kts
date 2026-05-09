import org.gradle.api.attributes.Category
import org.gradle.api.attributes.Usage
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
  id("katatui-quality")
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.skie)
  alias(libs.plugins.maven.publish)
}

group = providers.gradleProperty("GROUP").get()

version = providers.gradleProperty("VERSION_NAME").get()

mavenPublishing {
  pom {
    name.set("Katatui")
    description.set("Kotlin Multiplatform bindings for Ratatui")
    inceptionYear.set("2026")
    url.set("https://github.com/hyeons-lab/katatui")
    licenses {
      license {
        name.set("MIT")
        url.set("https://opensource.org/licenses/MIT")
      }
    }
    developers {
      developer {
        id.set("hyeonslab")
        name.set("Hyeons Lab")
        url.set("https://github.com/hyeons-lab")
      }
    }
    scm {
      url.set("https://github.com/hyeons-lab/katatui")
      connection.set("scm:git:github.com/hyeons-lab/katatui.git")
      developerConnection.set("scm:git:ssh://github.com/hyeons-lab/katatui.git")
    }
  }

  publishToMavenCentral()
  signAllPublications()
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

// Set -PcrossCompile=true to enable building Rust static libs for *all* targets on the current
// host (the Mac publish runner uses this — see .github/workflows/publish.yml). Without it, only
// the host's native target builds, matching the typical local-dev workflow.
val crossCompile = providers.gradleProperty("crossCompile").orElse("false").get().toBoolean()

// One cargo build task per target (release static lib).
//
// isEnabled is a simple Boolean property (not a lambda), which is configuration-cache safe.
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
    isEnabled = isNativeTarget || crossCompile
  }
}

// Header-generation build (host triple only; build.rs writes katatui.h)
val buildKatatuiFfiHeader by
  tasks.registering(Exec::class) {
    group = "rust"
    description = "Build Rust FFI (debug, host only) to regenerate katatui.h via cbindgen"
    workingDir = file("../katatui-ffi")
    val hostTriple =
      when {
        isMac -> "aarch64-apple-darwin"
        isWindows -> "x86_64-pc-windows-gnu"
        else -> "x86_64-unknown-linux-gnu"
      }
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

// Targets are declared based on what this host can build. With -PcrossCompile=true, all five
// are declared so a single Mac runner can publish a complete root Gradle metadata (.module)
// listing every variant. Without it, only the host's native targets are declared — keeps
// per-platform CI builds and local dev fast and free of unbuildable foreign-target tasks.
kotlin {
  if (isMac || crossCompile) {
    macosArm64()
    macosX64()
  }
  if (!isMac && !isWindows || crossCompile) {
    linuxX64()
    linuxArm64()
  }
  if (isWindows || crossCompile) {
    mingwX64()
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
        extraOpts("-libraryPath", "${rootDir}/katatui-ffi/target/$triple/release")
      }
    }
    binaries.all {
      if (name.contains("mingw", ignoreCase = true)) {
        linkerOpts("-lws2_32", "-lbcrypt", "-lntdll", "-luserenv")
      }
    }
    if (this.name.startsWith("macos")) {
      binaries.framework {
        baseName = "Katatui"
        isStatic = true
        xcf.add(this)
      }
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
