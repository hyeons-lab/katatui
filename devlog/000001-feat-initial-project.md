## Agent

Claude Code (claude-sonnet-4-6) @ katatui branch feat/initial-project

## Intent

Create the initial Katatui project: a Kotlin Multiplatform Native library that wraps Rust's ratatui TUI library. Includes build-logic convention plugins, Rust FFI crate, KMP cinterop wrappers, KotlinPoet codegen, and a counter sample app.

## Progress

- [x] Git repository initialized
- [x] Worktree created: `feat/initial-project`
- [x] Devlog + plan files created
- [ ] Root project scaffolding (Gradle, .gitignore, README, AGENTS.md)
- [ ] Build-logic convention plugin
- [ ] Rust FFI crate (katatui-ffi)
- [ ] KMP library module (katatui)
- [ ] Codegen module
- [ ] Sample app

## What Changed

2026-02-25T20:58-0800 devlog/CONVENTIONS.md — devlog conventions (copied from prism pattern)
2026-02-25T20:58-0800 devlog/plans/000001-01-initial-project.md — initial project plan

## Decisions

2026-02-25T20:58-0800 Mirrored prism build conventions — same build-logic pattern, version catalog, ktfmt+detekt quality plugin; ensures consistency and familiarity

2026-02-25T20:58-0800 begin/end draw pattern for Frame FFI — ratatui's `Frame<'_>` is lifetime-bound; split into `katatui_terminal_begin_draw` / `katatui_terminal_end_draw` with a `drawing: bool` guard on the terminal struct; avoids unsafe lifetime crossing the FFI boundary

2026-02-25T20:58-0800 Gradle 9.3.1 (vs prism's 9.2.0) — plan specifies 9.3.1; build-logic mirrors prism pattern exactly otherwise

2026-02-25T20:58-0800 katatui-ffi is Cargo workspace, NOT Gradle subproject — FFI crate is purely Rust; Gradle invokes cargo via Exec tasks, not as a Gradle subproject

2026-02-25T20:58-0800 codegen as JVM module — KotlinPoet is a JVM library; codegen runs on JVM, outputs Kotlin source that gets compiled by nativeMain

## Issues

(none yet)

## Commits

(none yet)

## Next Steps

- Complete all file scaffolding
- Run `cargo build` to verify Rust FFI compiles and katatui.h is generated
- Run `./gradlew :codegen:jar :katatui:generateKotlinWrappers`
- Run `./gradlew :katatui:compileKotlinMacosArm64`
- Run sample app
