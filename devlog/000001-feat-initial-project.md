## Agent

Claude Code (claude-sonnet-4-6) @ katatui branch feat/initial-project

## Intent

Create the initial Katatui project: a Kotlin Multiplatform Native library that wraps Rust's ratatui TUI library. Includes build-logic convention plugins, Rust FFI crate, KMP cinterop wrappers, KotlinPoet codegen, and a counter sample app.

## Progress

- [x] Git repository initialized
- [x] Worktree created: `feat/initial-project`
- [x] Devlog + plan files created
- [x] Root project scaffolding (Gradle, .gitignore, README, AGENTS.md)
- [x] Build-logic convention plugin
- [x] Rust FFI crate (katatui-ffi)
- [x] KMP library module (katatui)
- [x] Codegen module
- [x] Sample app
- [x] End-to-end build verified

## What Changed

2026-02-25T20:58-0800 devlog/CONVENTIONS.md — devlog conventions (copied from prism pattern)
2026-02-25T20:58-0800 devlog/plans/000001-01-initial-project.md — initial project plan
2026-02-25T22:45-0800 All project files — full initial implementation (see commit aa170a2)

**Codegen fixes (session 2):**
2026-02-25T22:45-0800 codegen/WrapperEmitter.kt — removed memScoped/toCString/cstr; cinterop auto-converts String↔const char*; changed val→const val for BASE_PACKAGE/CINTEROP_PACKAGE; refactored setter+adder loops to mapNotNull+forEach to fix detekt LoopWithTooManyJumpStatements; added constructor params to invoke factory
2026-02-25T22:45-0800 codegen/HeaderParser.kt — added kotlinNameOverrides map; List→ListWidget to avoid stdlib collision
2026-02-25T22:45-0800 katatui-ffi/src/widgets/layout.rs — changed katatui_layout_split to return u32 count directly (dropped out_count pointer param); cleaner FFI surface
2026-02-25T22:45-0800 katatui/src/nativeInterop/cinterop/katatui.h — regenerated after layout.rs change
2026-02-25T22:45-0800 katatui/src/nativeMain/kotlin/…/Layout.kt — rewrote split() using new return-value API; allocArray<KatatuiRect>; import kotlinx.cinterop.get; @file:OptIn
2026-02-25T22:45-0800 katatui/src/nativeMain/kotlin/…/Terminal.kt — wrapped draw() and close() in try/finally for exception safety
2026-02-25T23:43-0800 katatui/src/nativeMain/kotlin/…/Terminal.kt — moved init(), restore(), draw() to extension functions; ptr changed private→internal; @OptIn moved to @file: level
2026-02-25T23:43-0800 sample-app/src/…/main.kt — added explicit imports for Terminal extension functions (draw, init)
2026-02-25T22:45-0800 katatui/src/nativeMain/kotlin/…/Frame.kt — updated render(widget: ListWidget, …) after rename
2026-02-25T22:45-0800 katatui/build.gradle.kts — moved generated output to build/generated-sources/ (avoids ktfmt scanning); added proper codegenClasspath Configuration for config-cache; wired cinterop→cargo build dependencies
2026-02-25T22:45-0800 sample-app/build.gradle.kts — added rustTriples + linkerOpts (KMP native doesn't propagate lib linkerOpts to executable consumers)
2026-02-25T22:45-0800 sample-app/src/…/main.kt — fixed draw lambda (Frame.() -> Unit requires implicit receiver, not named parameter)
2026-02-25T22:45-0800 .gitignore — removed old generated/ entry (now covered by build/ exclusion)

## Decisions

2026-02-25T20:58-0800 Mirrored prism build conventions — same build-logic pattern, version catalog, ktfmt+detekt quality plugin; ensures consistency and familiarity

2026-02-25T20:58-0800 begin/end draw pattern for Frame FFI — ratatui's `Frame<'_>` is lifetime-bound; split into `katatui_terminal_begin_draw` / `katatui_terminal_end_draw` with a `drawing: bool` guard on the terminal struct; avoids unsafe lifetime crossing the FFI boundary

2026-02-25T20:58-0800 Gradle 9.3.1 (vs prism's 9.2.0) — plan specifies 9.3.1; build-logic mirrors prism pattern exactly otherwise

2026-02-25T20:58-0800 katatui-ffi is Cargo workspace, NOT Gradle subproject — FFI crate is purely Rust; Gradle invokes cargo via Exec tasks, not as a Gradle subproject

2026-02-25T20:58-0800 codegen as JVM module — KotlinPoet is a JVM library; codegen runs on JVM, outputs Kotlin source that gets compiled by nativeMain

2026-02-25T22:45-0800 ListWidget instead of List — generated class name 'List' shadowed kotlin.collections.List; added kotlinNameOverrides map in HeaderParser to allow per-group overrides without changing the C API

2026-02-25T22:45-0800 katatui_layout_split returns u32 — out-pointer approach required complex UIntVar mechanics in Kotlin/Native; returning count directly is a cleaner C API and simpler Kotlin call site

2026-02-25T22:45-0800 Generated sources in build/ not src/ — generated files in src/ are scanned by ktfmt/detekt; moving to build/generated-sources/ follows Gradle convention and removes need for exclusion rules in quality tools

2026-02-25T23:43-0800 Extension functions in separate package require explicit imports — call sites are syntactically identical (terminal.init()), but when extension functions are defined in com.hyeonslab.katatui and the call site is in com.hyeonslab.katatui.sample, the extension functions must be explicitly imported (import com.hyeonslab.katatui.draw, etc.)

## Issues

**String passing (const char * ↔ String):** Initially used toCString()/memScoped, then .cstr — both wrong. Kotlin/Native cinterop auto-converts const char* to String? at the boundary; just pass String directly. The import toCString was not even resolvable in 2.3.10.

**katatui_layout_split out-pointer:** Original Rust API used `*mut u32` for the count output. Kotlin/Native requires allocating a UIntVar, getting its ptr, and indexing — collision with MatchGroupCollection.get(String) when import kotlinx.cinterop.get is missing. Resolved by redesigning the Rust function to return u32 directly.

**import kotlinx.cinterop.get required for CPointer<T>[i]:** The array indexing operator on CPointer is not auto-imported. Without it, Kotlin resolves [0] to MatchGroupCollection.get.

**Frame.() -> Unit vs Function2:** draw { frame -> … } creates a value parameter lambda, not a receiver lambda. Must use bare { … } block with implicit `this`.

**linkerOpts not propagated:** Kotlin/Native does not forward a library's linkerOpts to consuming executables. Must duplicate -L/-lkatatui_ffi in sample-app/build.gradle.kts.

**Stale release lib:** After Rust source change and debug rebuild only, the release .a file retained the old layout_split symbol. Resolved by cargo build --release.

**Generated files in src/ scanned by ktfmt:** Moved generated output to build/generated-sources/ following Gradle convention. ktfmt and detekt skip build/ directories by default.

## Commits

3435a90 — fix: complete Kotlin wrappers — codegen, layout FFI, exception safety
aa170a2 — feat: initial Katatui project scaffold
61386c3 — chore: add devlog scaffolding for feat/initial-project
17e649d — chore: update devlog
4a77070 — fix: use ratatui::try_init() to propagate terminal errors as null
7f412fa — feat: Katatui sealed interface; generated widgets implement Katatui
befb93e — refactor: rename Katatui sealed interface to KatatuiWidget
e2db0f4 — refactor: move Terminal non-lifecycle methods to extension functions
e09c5ed — feat: add Clear, Gauge, LineGauge, Sparkline, BarChart, Tabs, Table widgets
HEAD — feat: widget showcase sample app with tabs, sparkline, gauges, barchart, table

## What Changed (session 3)

2026-02-26T07:08-0800 devlog/plans/000001-02-add-widgets.md — plan for adding 7 remaining ratatui widgets
2026-02-26T07:08-0800 codegen/HeaderParser.kt — rewrote widgetGroups() to derive snake_case prefixes from opaqueTypes registry, enabling correct multi-word group detection (line_gauge, bar_chart); added list_state to excluded set
2026-02-26T07:08-0800 codegen/WrapperEmitter.kt — added uint64_t → ULong mapping in cTypeToKotlin; added "ULong" → "0uL" in defaultValueFor
2026-02-26T07:08-0800 katatui-ffi/src/widgets/clear.rs — KatatuiClear FFI wrapper
2026-02-26T07:08-0800 katatui-ffi/src/widgets/gauge.rs — KatatuiGauge FFI wrapper (percent/label/style/gauge_style)
2026-02-26T07:08-0800 katatui-ffi/src/widgets/line_gauge.rs — KatatuiLineGauge FFI wrapper (percent/style via filled_style)
2026-02-26T07:08-0800 katatui-ffi/src/widgets/sparkline.rs — KatatuiSparkline FFI wrapper (data Vec<u64>/max/style/bar_style)
2026-02-26T07:08-0800 katatui-ffi/src/widgets/bar_chart.rs — KatatuiBarChart FFI wrapper; _bar not _add_bar to bypass codegen
2026-02-26T07:08-0800 katatui-ffi/src/widgets/tabs.rs — KatatuiTabs FFI wrapper (titles/selected/style)
2026-02-26T07:08-0800 katatui-ffi/src/widgets/table.rs — KatatuiTable FFI wrapper (headers/rows/current_row/widths/style)
2026-02-26T07:08-0800 katatui-ffi/src/widgets/mod.rs — added 7 new pub mod + pub use entries
2026-02-26T07:08-0800 katatui-ffi/src/lib.rs — added 7 katatui_frame_render_* functions
2026-02-26T07:08-0800 katatui/src/.../Frame.kt — 7 new render() overloads
2026-02-26T07:08-0800 katatui/src/.../Layout.kt — toCKind() private → internal
2026-02-26T07:08-0800 katatui/src/.../BarChart.kt — new file, bar() extension
2026-02-26T07:08-0800 katatui/src/.../Table.kt — new file, nextRow() and addWidth() extensions

## Decisions (session 3)

2026-02-26T07:08-0800 widgetGroups() uses opaqueTypes registry — deriving prefixes from registered C type names (KatatuiLineGauge → line_gauge) is robust against multi-word names; the old substringBefore("_") broke on line_gauge
2026-02-26T07:08-0800 BarChart::new(bars) instead of BarGroup — ratatui 0.30 BarChart::new() directly takes Vec<Bar<'a>>; simpler than the BarGroup indirection
2026-02-26T07:08-0800 LineGauge.line_style maps to filled_style — ratatui 0.30 has no single "line_style" setter; filled_style(Style) controls the filled bar styling
2026-02-26T07:08-0800 Sparkline.data uses iter().copied() — data() accepts IntoIterator<Item: Into<SparklineBar>>; u64 implements Into<SparklineBar> natively
2026-02-26T07:08-0800 katatui_bar_chart_bar not _add_bar — avoids codegen Adder detection; hand-written BarChart.kt extension calls it directly

## Issues (session 3)

**Kotlin Native cache stale after new FFI symbols:** Cache from before the new symbols caused undefined symbol linker errors. Fixed by cleaning build outputs and relinking with -Pkotlin.native.cacheKind.macosArm64=none to repopulate.

**Table.kt KatatuiConstraint wrong package:** Initial import used cnames.structs; KatatuiConstraint is #[repr(C)] struct so lives in com.hyeonslab.katatui.cinterop. Fixed import.

**LoopWithTooManyJumpStatements in HeaderParser.kt:** Refactored loop had three jump statements. Fixed by replacing the Elvis continue (?:continue) with explicit null check + nested if block.

## Next Steps

- Replace `HEAD` hash after commit; push branch, update PR
- Swift wrapper using SKIE
