## Agent

Claude Code (claude-opus-4-7) @ katatui branch fix/kmp-multiplatform-publish

## Intent

The previous publish workflow used a 3-runner OS matrix where each runner ran `publishAllPublicationsToMavenCentral`. Each runner wrote its own root `katatui-0.1.0-SNAPSHOT.module` listing only its target's variants, and the runners overwrote each other on upload — published snapshot ended up with whichever runner ran last (e.g. only `linuxX64` + `linuxArm64` variants). Switch to a single Mac runner that cross-compiles everything so one root metadata write contains all five variants.

## What Changed

2026-05-08T23:57-0700 katatui/build.gradle.kts — added a `crossCompile` Gradle property gate. In the `kotlin {}` block, targets are declared if the host is native to them *or* `crossCompile=true`. Cargo task `isEnabled = isNativeTarget || crossCompile` so the Mac publish runner builds Rust for every target. Fixed the framework guard: `if (isMac)` → `if (this.name.startsWith("macos"))` — the previous host-check tried to add a framework to Linux/Windows targets when all targets were declared (frameworks are Apple-only).

2026-05-08T23:57-0700 .github/workflows/publish.yml — dropped the 3-OS matrix. One `macos-26` job that installs all five Rust targets, `brew install`s the cross-compile linkers (`messense/macos-cross-toolchains` for Linux GNU triples + `mingw-w64` for Windows), sets `CARGO_TARGET_*_LINKER` env vars to point cargo at the brew binaries, and runs `publishAllPublicationsToMavenCentral -PcrossCompile=true`.

2026-05-09T00:12-0700 katatui-ffi/src/lib.rs + src/terminal.rs — cleaned up two cargo warnings surfaced by the cross-compile run: removed the private `use types::KatatuiRect;` that was shadowing the `pub use types::*;` re-export (effectively suppressing `KatatuiRect` from the crate's public API), and removed the unused `terminal: *mut KatatuiTerminal` field on `KatatuiFrame` (set in `katatui_terminal_begin_draw` but never read).

## Decisions

2026-05-08T23:57-0700 Single-runner cross-compile chosen over multi-runner per-target publishing. The multi-runner path requires declaring all targets on the root-metadata-publishing runner, which then drags in `commonizeCInterop` across every target, which needs cinterop output for each — and `nativeMain` imports the commonized cinterop API, so disabling commonization breaks the build. Single-runner is simpler in Gradle (no per-target task disabling, no `commonizeCInterop` workarounds) at the cost of installing cross-compile linkers in CI.

2026-05-08T23:57-0700 Cross-compile linkers via `CARGO_TARGET_*_LINKER` env vars in publish.yml, not `katatui-ffi/.cargo/config.toml`. The Linux CI job uses `gcc-aarch64-linux-gnu` (apt) which provides `aarch64-linux-gnu-gcc`; the Mac messense brew package provides `aarch64-unknown-linux-gnu-gcc`. Putting Mac names in config.toml would break the Linux job. Env-var overrides scope to the Mac publish runner only.

2026-05-08T23:57-0700 Conditional target declaration (host-or-crossCompile) instead of always-declare-all + per-host disabling. Always-declare ran `commonizeCInterop` on every host, which fails on the Linux/Windows CI builds for the same reason as above. Conditional declaration keeps each per-platform CI job seeing only its native targets — same as before this PR.

## Issues

**Multi-runner attempt failed on `commonizeCInterop`.** First implementation declared all five targets on every host and disabled task chains for foreign targets. Cinterop and compile tasks were correctly skipped, but `commonizeCInterop` (project-level, not target-level) fired and demanded outputs from every declared target's cinterop. Couldn't disable the commonization task without losing the commonized API in `nativeMain`. Pivoted to the cross-compile-on-Mac approach.

**`crossCompile=true` not verifiable locally.** Mac dev machine doesn't have the cross-compile linkers installed. Default-mode `:katatui:publishToMavenLocal` was verified — produces a Mac-only artifact, same as before, so the host-conditional path is intact. CI is the integration test for the cross-compile path.

## Commits

11c7507 — fix: cross-compile all KMP targets on Mac for complete root metadata
HEAD — chore: clean up two cargo warnings (shadowed re-export, unused field)
