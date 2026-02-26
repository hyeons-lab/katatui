# AGENTS.md

This file provides guidance to AI agents when working with code in this repository.

## Project Overview

Katatui is a Kotlin Multiplatform Native TUI library — idiomatic Kotlin bindings for
[ratatui](https://ratatui.rs), the Rust terminal-UI framework.

- **Group ID**: com.hyeons-lab
- **Package**: com.hyeonslab.katatui
- **Version**: 0.1.0-SNAPSHOT
- **Targets**: macOS ARM64/x64, Linux x64/ARM64, Windows x64 (desktop TUI only — no Android/iOS)

## Module Structure

```
katatui-ffi/    Rust staticlib — ratatui C API (cargo workspace; NOT a Gradle subproject)
katatui/        KMP library — cinterop + hand-written + generated Kotlin wrappers
codegen/        JVM tool — parses cbindgen headers, emits KotlinPoet wrappers
sample-app/     Counter TUI demo (Kotlin/Native executable)
build-logic/    Gradle convention plugins (katatui-quality: ktfmt + detekt)
```

## Build Commands

```bash
# 1. Build Rust FFI (generates katatui.h via cbindgen build.rs)
cd katatui-ffi
cargo build --target aarch64-apple-darwin    # macOS ARM64
cargo build --target x86_64-unknown-linux-gnu  # Linux x64
cd ..

# 2. Codegen: parse katatui.h → generated Kotlin wrappers
./gradlew :codegen:jar
./gradlew :katatui:generateKotlinWrappers

# 3. Compile KMP library
./gradlew :katatui:compileKotlinMacosArm64   # or linuxX64

# 4. Run sample app
./gradlew :sample-app:runDebugExecutableMacosArm64

# 5. Quality checks
./gradlew ktfmtCheck detektJvmMain
./gradlew ktfmtFormat                        # auto-format
```

## Architecture

### FFI Layer

Rust (`katatui-ffi/`) exposes a C ABI:
- `#[no_mangle] extern "C"` functions
- `#[repr(C)]` value types (structs/enums)
- Opaque pointer types (`KatatuiTerminal*`, `KatatuiFrame*`, etc.)

`build.rs` runs cbindgen on every `cargo build`, writing `katatui/src/nativeInterop/cinterop/katatui.h`.

### Cinterop

`katatui/src/nativeInterop/cinterop/katatui.def` points cinterop at `katatui.h`. The generated cinterop klib is in package `com.hyeonslab.katatui.cinterop`.

### Kotlin Wrappers

Two layers:
1. **Hand-written** (`nativeMain/kotlin/…/katatui/`): `Terminal`, `Frame`, `Layout`, common sealed classes (`Constraint`, `Direction`), value types (`Color`, `Modifier`, `Borders`)
2. **Generated** (`nativeMain/kotlin/…/katatui/generated/`): emitted by `codegen` from `katatui.h` — one Kotlin class per opaque widget type. Gitignored; regenerated via `./gradlew :katatui:generateKotlinWrappers`.

### Draw Session

ratatui's `Frame<'_>` is lifetime-bound. Katatui splits the draw call into:
- `katatui_terminal_begin_draw()` → returns `KatatuiFrame*`
- `katatui_terminal_end_draw()`

`KatatuiTerminal` has a `drawing: bool` guard. `KatatuiFrame` holds a raw pointer to the terminal and a snapshotted `area`.

## Code Quality

All warnings are errors (`allWarningsAsErrors.set(true)`).

- ktfmt Google style, 100-char line width
- detekt with `detekt.yml` at repo root
- No star imports
- Testing: `kotlin.test` + Kotest assertions

## Rust Triple → Kotlin Target Mapping

| Kotlin target | Rust triple                 |
|---------------|-----------------------------|
| macosArm64    | aarch64-apple-darwin        |
| macosX64      | x86_64-apple-darwin         |
| linuxX64      | x86_64-unknown-linux-gnu    |
| linuxArm64    | aarch64-unknown-linux-gnu   |
| mingwX64      | x86_64-pc-windows-gnu       |

## Development Workflow

Follows the same git worktree + devlog workflow as prism. See `devlog/CONVENTIONS.md`.

- Never commit to `main` directly
- All features use worktrees (`worktrees/<branch-name>`)
- Create devlog + plan before writing code
- Conventional commits: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `build`
