## Thinking

The goal is to create a new Kotlin Multiplatform Native project named Katatui that wraps Rust's ratatui TUI library. The key insight is that Kotlin/Native's cinterop can consume C headers, and Rust exposes a C ABI via `#[no_mangle] extern "C"` functions and `#[repr(C)]` types. cbindgen generates C headers from Rust source during `cargo build`.

The project mirrors the prism project structure: build-logic convention plugins, version catalog, devlog, quality tooling.

Target platforms: macOS ARM64 + x64, Linux x64 + ARM64, Windows x64. No Android or iOS.

The architecture is:
1. `katatui-ffi/` — Rust staticlib that wraps ratatui and exposes a C API
2. `katatui/` — KMP library: cinterop reads the C headers, Kotlin wrappers on top
3. `codegen/` — JVM tool that parses cbindgen headers and emits KotlinPoet wrappers
4. `sample-app/` — Counter TUI demo

For the FFI layer, the key challenge is that `Frame<'_>` is lifetime-bound in ratatui. The solution is to use a begin/end draw pattern where `KatatuiFrame` stores a raw pointer to the terminal plus a snapshotted area. A `drawing: bool` guard prevents misuse.

The KotlinPoet codegen parses cbindgen headers line-by-line (no external parser needed since cbindgen output is highly structured) and emits Kotlin wrapper classes with companion object `invoke` constructors for DSL use.

## Plan

1. Initialize repository + worktree on `feat/initial-project`
2. Create devlog directory + CONVENTIONS.md, plans/000001-01-initial-project.md, 000001-feat-initial-project.md
3. Scaffold root project files:
   - `.gitignore`, `README.md`, `AGENTS.md`
   - `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`
   - `gradle/libs.versions.toml`
   - `gradle/wrapper/gradle-wrapper.properties` (Gradle 9.3.1)
   - `detekt.yml` (copy from prism)
4. Build-logic convention plugin:
   - `build-logic/settings.gradle.kts`
   - `build-logic/build.gradle.kts`
   - `build-logic/src/main/kotlin/katatui-quality.gradle.kts`
5. Rust FFI crate (`katatui-ffi/`):
   - `Cargo.toml`, `cbindgen.toml`, `build.rs`
   - `src/lib.rs`, `src/terminal.rs`, `src/types.rs`
   - `src/widgets/mod.rs`, `block.rs`, `paragraph.rs`, `list.rs`, `layout.rs`
6. KMP library module (`katatui/`):
   - `build.gradle.kts` with cinterop + cargo task wiring
   - `src/nativeInterop/cinterop/katatui.def`
   - `src/nativeInterop/cinterop/katatui.h` (initial placeholder; replaced by cargo build)
   - `src/commonMain/kotlin/` — Constraint.kt, Direction.kt, widgets/Color.kt, Modifier.kt, Borders.kt
   - `src/nativeMain/kotlin/` — Terminal.kt, Frame.kt, Layout.kt
7. Codegen module (`codegen/`):
   - `build.gradle.kts`
   - `src/main/kotlin/…/codegen/model/` — CFunction.kt, CType.kt, WidgetGroup.kt
   - `src/main/kotlin/…/codegen/HeaderParser.kt`, `WrapperEmitter.kt`, `Main.kt`
8. Sample app (`sample-app/`):
   - `build.gradle.kts`
   - `src/nativeMain/kotlin/…/sample/main.kt`
9. Commit devlog scaffolding, then commit all project files
