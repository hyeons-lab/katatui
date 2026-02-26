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

// One cargo build task per target (release static lib)
rustTriples.forEach { (kotlinTarget, triple) ->
    tasks.register<Exec>("buildKatatuiFfi_$kotlinTarget") {
        group = "rust"
        description = "Build Rust FFI staticlib for $kotlinTarget ($triple)"
        workingDir = file("../katatui-ffi")
        commandLine("cargo", "build", "--release", "--target", triple)
        outputs.file("../katatui-ffi/target/$triple/release/libkatatui_ffi.a")
        inputs.dir("../katatui-ffi/src")
        inputs.file("../katatui-ffi/Cargo.toml")
    }
}

// Header-generation build (host triple only; build.rs writes katatui.h)
val buildKatatuiFfiHeader by
    tasks.registering(Exec::class) {
        group = "rust"
        description = "Build Rust FFI (debug, host only) to regenerate katatui.h via cbindgen"
        workingDir = file("../katatui-ffi")
        val hostTriple =
            if (isMac) "aarch64-apple-darwin" else "x86_64-unknown-linux-gnu"
        commandLine("cargo", "build", "--target", hostTriple)
        outputs.file("src/nativeInterop/cinterop/katatui.h")
        inputs.dir("../katatui-ffi/src")
        inputs.file("../katatui-ffi/cbindgen.toml")
    }

// Codegen task: parse katatui.h → emit Kotlin wrapper sources
val generateKotlinWrappers by
    tasks.registering(JavaExec::class) {
        group = "codegen"
        description = "Parse katatui.h and emit generated Kotlin wrapper classes"
        dependsOn(buildKatatuiFfiHeader, project(":codegen").tasks.named("jar"))
        classpath = project(":codegen").configurations["runtimeClasspath"]
        mainClass.set("com.hyeonslab.katatui.codegen.MainKt")
        args(
            file("src/nativeInterop/cinterop/katatui.h").absolutePath,
            file("src/nativeMain/kotlin/com/hyeonslab/katatui/generated").absolutePath,
        )
        inputs.file("src/nativeInterop/cinterop/katatui.h")
        outputs.dir("src/nativeMain/kotlin/com/hyeonslab/katatui/generated")
    }

kotlin {
    if (isMac) {
        macosArm64()
        macosX64()
    }
    linuxX64()
    linuxArm64()
    mingwX64()
    applyDefaultHierarchyTemplate()

    targets.withType<KotlinNativeTarget>().configureEach {
        val triple = rustTriples[name] ?: return@configureEach
        compilations.getByName("main") {
            cinterops.create("katatui") {
                definitionFile.set(project.file("src/nativeInterop/cinterop/katatui.def"))
                includeDirs(project.file("src/nativeInterop/cinterop"))
            }
        }
        binaries.all {
            linkerOpts("-L${rootDir}/katatui-ffi/target/$triple/release", "-lkatatui_ffi")
            if (name.contains("mingw", ignoreCase = true)) {
                linkerOpts("-lws2_32", "-lbcrypt", "-lntdll", "-luserenv")
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

// Wire cargo build → before cinterop task for each target
rustTriples.keys.forEach { target ->
    val cap = target.replaceFirstChar { it.uppercase() }
    tasks.matching { it.name == "cinterop${cap}Katatui" }.configureEach {
        dependsOn("buildKatatuiFfi_$target")
    }
}
