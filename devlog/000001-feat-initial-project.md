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
- [x] Pull Request created: [PR #1](https://github.com/hyeons-lab/katatui/pull/1)

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
aa4986f — feat: widget showcase sample app with tabs, sparkline, gauges, barchart, table
2faec07 — chore: update devlog
0de71ba — test: add test suite for codegen and katatui modules
7003cfd — test: tighten OptIn assertion and use Reset short name
3555458 — feat: add Scrollbar, Chart, Canvas, Logo, and Mascot widgets
HEAD — chore: update devlog

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

## What Changed (session 4 — PR review fixes)

2026-02-26T20:56-0800 katatui-ffi/src/types.rs — Added fg_r/g/b/fg_index/bg_r/g/b/bg_index payload fields to KatatuiStyle; added color_from_katatui() helper; From<KatatuiStyle> now uses payload for Rgb/Indexed colors (previously returned Reset for both)
2026-02-26T20:56-0800 katatui-ffi/src/lib.rs — Removed katatui_terminal_init (was a no-op stub; ratatui::init() already enables raw mode + alt screen on Terminal::new)
2026-02-26T20:56-0800 katatui-ffi/src/widgets/sparkline.rs — Fixed application order: style first (base), bar_style second (overrides); added comment noting ratatui-widgets 0.3 has no separate bar_style() method
2026-02-26T20:56-0800 katatui/src/nativeInterop/cinterop/katatui.h — Added 8 uint8_t payload fields to KatatuiStyle struct; removed katatui_terminal_init declaration
2026-02-26T20:56-0800 katatui/src/nativeMain/kotlin/…/StyleNative.kt — Rewrote toCValue() with payload-aware Rgb/Indexed handling; deleted public Color.toCValue(); renamed fallback to private toColorEnum()
2026-02-26T20:56-0800 katatui/src/nativeMain/kotlin/…/Terminal.kt — Removed katatui_terminal_init call and import
2026-02-26T20:56-0800 katatui/src/nativeMain/kotlin/…/BlockExt.kt — Replaced broken read/write style property (getter returned dummy Style()) with write-only setStyle() function
2026-02-26T20:56-0800 katatui/src/nativeMain/kotlin/…/Event.kt — Added pollMillis(timeoutMillis: Long) overload for Swift callers that cannot construct Duration
2026-02-26T20:56-0800 katatui/src/nativeMain/kotlin/…/Rect.kt — Added early-return guard in inner(): returns Rect(x, y, 0, 0) when width < 2 or height < 2 to prevent UShort coordinate wraparound
2026-02-26T20:56-0800 codegen/src/main/kotlin/…/HeaderParser.kt — Removed two debug println() calls from widgetGroups()
2026-02-26T20:56-0800 sample-app/src/…/main.kt — Changed block.style = Style(...) → block.setStyle(Style(...))
2026-02-26T20:56-0800 sample-app/build.gradle.kts — Replaced byteArrayOf(...) embedding with Base64.decode(...) to reduce generated source size; added import java.util.Base64
2026-02-26T20:56-0800 sample-app-swift/Sources/…/DSL.swift — Event.poll now calls EventKt.pollMillis; readKey simplified (SKIE bridges Char? → Character? directly, no cast needed)
2026-02-26T20:56-0800 sample-app-swift/Package.swift — Replaced hardcoded aarch64 lib path with #if arch(arm64) conditional at package scope

## Decisions (session 4)

2026-02-26T20:56-0800 Style property → setStyle() — BlockExt previously exposed var Block.style with a dummy getter returning Style(); a write-only function is honest and simpler
2026-02-26T20:56-0800 bar_style order fix without bar_style() — ratatui-widgets 0.3.0 has no Sparkline::bar_style() method; fixed by applying base style first and bar_style second (so bar_style wins when both are set); noted in code comment
2026-02-26T20:56-0800 Base64 via java.util.Base64 import — fully-qualified java.util.Base64 in Gradle Kotlin DSL fails with "Unresolved reference 'util'"; explicit import java.util.Base64 resolves it

## Issues (session 4)

**Sparkline.bar_style() does not exist in ratatui 0.30:** Plan said to use Sparkline::bar_style(); ratatui-widgets 0.3.0 (used by ratatui 0.30) has no such method. Fixed by correcting application order to use .style() for both fields.
**java.util.Base64 fails as fully-qualified in Gradle Kotlin DSL:** `java.util.Base64.getEncoder()` resolves as "Unresolved reference 'util'" in a Gradle Kotlin DSL script even though it works on JVM. Fixed by adding `import java.util.Base64` at the top of build.gradle.kts.

## What Changed (session 5 — self-review fixes)

2026-02-26T21:44-0800 katatui-ffi/src/types.rs — From<KatatuiColor> Rgb/Indexed arms changed to unreachable!(); eliminates the latent footgun where .into() on Rgb/Indexed silently returned Reset
2026-02-26T21:44-0800 katatui-ffi/src/widgets/sparkline.rs — bar_style now properly implemented: when set, each data point is wrapped in SparklineBar::style(bar_style) so per-bar colouring works correctly; widget-level style() remains independent
2026-02-26T21:44-0800 katatui/src/nativeMain/kotlin/…/StyleNative.kt — else branches now use already-bound c instead of re-accessing this@toCValue.fg/bg; toColorEnum() Rgb/Indexed arms changed to error() calls to make contract explicit and fail-fast

## Decisions (session 5)

2026-02-26T21:44-0800 SparklineBar for bar_style — ratatui-widgets 0.3 exposes SparklineBar::style() for per-bar styling; wrapping each u64 in SparklineBar::from(v).style(bar_style) achieves the correct semantics without any API surface change
2026-02-26T21:44-0800 unreachable!() not panic!() in From<KatatuiColor> — communicates developer intent more clearly than a generic panic; the message names color_from_katatui as the correct alternative

## What Changed (session 6 — tests)

2026-02-26T22:06-0800 codegen/build.gradle.kts — added testImplementation(libs.kotlin.test)
2026-02-26T22:06-0800 codegen/src/test/.../CFunctionTest.kt — new; tests FunctionRole classification (_new/free/set_/add_/render_/split/other) and setterProperty extraction
2026-02-26T22:06-0800 codegen/src/test/.../HeaderParserTest.kt — new; integration tests for parse() (opaque types, body structs, enums, functions+params) and widgetGroups() (multi-word names, excluded prefixes, state function exclusion)
2026-02-26T22:06-0800 codegen/src/test/.../WrapperEmitterTest.kt — new; verifies generated Kotlin source contains class declaration, close(), invoke factory, simple setters, adder methods; asserts complex types (KatatuiStyle) are excluded
2026-02-26T22:06-0800 katatui/src/commonTest/.../widgets/StyleTest.kt — new; Style defaults, RESET constant, modifier storage, data class equality, copy
2026-02-26T22:06-0800 katatui/src/commonTest/.../widgets/ColorTest.kt — new; Rgb/Indexed field storage and equality; named singleton type checks
2026-02-26T22:06-0800 katatui/src/commonTest/.../widgets/BordersTest.kt — new; bitmask values, plus operator, idempotent combine, none identity
2026-02-26T22:06-0800 katatui/src/commonTest/.../widgets/ConstraintTest.kt — new; all five factory methods, Int→UShort conversion, zero value
2026-02-26T22:06-0800 katatui/src/nativeTest/.../RectTest.kt — new; inner() edge cases: zero/1/2/3 width-height, normal shrink; guards against UShort wraparound; origin preserved in guard path
2026-02-26T22:06-0800 katatui/src/nativeTest/.../widgets/StyleNativeTest.kt — new; Style.toCValue() for Rgb fg/bg payload fields, Indexed fg/bg index field, named color mapping, all five modifiers, default Style

## Decisions (session 6)

2026-02-26T22:06-0800 nativeTest for Rect and StyleNative — Rect and StyleNative are in nativeMain (use cinterop types); tests that inspect CValue struct fields must be in nativeTest where cinterop is available
2026-02-26T22:06-0800 Kotlin/Native backtick names: no special chars — Native backend rejects `()` and `,` in backtick-quoted test method names; renamed offending test in BordersTest

## Issues (session 6)

**Kotlin/Native rejects special chars in backtick names:** Backtick test names with `()` and `,` fail with "Name contains illegal characters". Fixed by using plain alphanumeric + space in all native test method names.

## What Changed (session 7 — test suite fixes)

2026-02-26T23:37-0800 codegen/src/test/.../WrapperEmitterTest.kt — added withTempDir helper (deletes temp dirs in finally); refactored emitBlock() to use withTempDir; replaced WrapperEmitterTest().run { … } with inline withTempDir { … } (bare run on outer this); added two new tests: `generated file carries OptIn annotation` (checks @file:OptIn) and `generated file imports used FFI functions`
2026-02-26T23:37-0800 katatui/src/nativeTest/.../widgets/StyleNativeTest.kt — added Reset and White named-color tests (boundary coverage for 16-arm when); removed redundant false-case assertion from bold test (covered by default-style test)

## Decisions (session 7)

2026-02-26T23:37-0800 @file:OptIn not @OptIn — KotlinPoet FileSpec.addAnnotation emits @file:OptIn(…); test assertion updated to check "@file:OptIn" to match actual output
2026-02-26T23:37-0800 withTempDir vs @TempDir — @TempDir requires junit-jupiter-api not in scope; withTempDir helper with try/finally is the correct lightweight fix

## Issues (session 7)

**@OptIn test failed on first run:** Plan claimed `@OptIn` would be in generated text; KotlinPoet renders file-level annotations as `@file:OptIn`, which does not contain the substring `@OptIn`. Fixed by checking `"@file:OptIn"` instead.

## What Changed (session 8 — test assertion fixes)

2026-02-27T00:45-0800 codegen/src/test/.../WrapperEmitterTest.kt — tightened OptIn assertion: `"ExperimentalForeignApi"` → `"ExperimentalForeignApi::class"` so it verifies the annotation and not just the import
2026-02-27T00:45-0800 katatui/src/nativeTest/.../widgets/StyleNativeTest.kt — replaced FQCN `com.hyeonslab.katatui.cinterop.Reset` with short `Reset` in default-style test (Reset is in scope via import added in session 7)

## What Changed (session 9 — Scrollbar, Chart, Canvas, Logo, Mascot)

2026-02-27T14:23-0800 katatui-ffi/src/types.rs — added KatatuiMarker enum (MarkerDot…MarkerQuadrant, prefixed to avoid C namespace collision); added From<KatatuiMarker>; made color_from_katatui pub(crate)
2026-02-27T14:23-0800 katatui-ffi/src/widgets/scrollbar.rs — new; KatatuiScrollbar (orientation, thumb/track/begin/end symbols/styles); KatatuiScrollbarState wrapping ratatui ScrollbarState; symbols stored as pre-leaked &'static str
2026-02-27T14:23-0800 katatui-ffi/src/widgets/chart.rs — new; KatatuiChart with begin/commit dataset pattern; data Vec leaked once at commit time to &'static [(f64,f64)]; AxisBuilder for x/y axes; build_chart() called every frame at zero allocation cost
2026-02-27T14:23-0800 katatui-ffi/src/widgets/canvas.rs — new; KatatuiCanvas with buffered CanvasCmd enum (Circle/Line/Rectangle/Points); commands replayed in 'static closure at render time
2026-02-27T14:23-0800 katatui-ffi/src/widgets/logo.rs — new; KatatuiLogo (Tiny/Small size); KatatuiMascot with KatatuiMascotEyeColor (EyeDefault/EyeRed, prefixed to avoid conflict with KatatuiColor::Red)
2026-02-27T14:23-0800 katatui-ffi/src/widgets/mod.rs — added pub mod canvas/chart/logo/scrollbar + pub use entries
2026-02-27T14:23-0800 katatui-ffi/src/lib.rs — added imports and render functions for Scrollbar (stateful), Chart, Canvas, Logo, Mascot
2026-02-27T14:23-0800 codegen/src/main/kotlin/.../HeaderParser.kt — added "scrollbar_state" to excluded set
2026-02-27T14:23-0800 codegen/src/main/kotlin/.../WrapperEmitter.kt — updated isComplexType to exclude non-pointer KatatuiXxx types; added double→Double and "Double"→"0.0" mappings
2026-02-27T14:23-0800 katatui/src/nativeMain/kotlin/.../ScrollbarState.kt — new hand-written state class; contentLength/position/viewportContentLength properties
2026-02-27T14:23-0800 katatui/src/nativeMain/kotlin/.../ScrollbarExt.kt — ScrollbarOrientation enum + setOrientation(); setThumbStyle/setTrackStyle/setBeginStyle/setEndStyle; imports toCValue explicitly
2026-02-27T14:23-0800 katatui/src/nativeMain/kotlin/.../ChartExt.kt — GraphType + ChartMarker enums; datasetPoint/setDatasetGraphType/setDatasetMarker/setDatasetStyle/commitDataset/xBounds/yBounds/setXStyle/setYStyle/setStyle; uses import aliases for Marker* cinterop constants
2026-02-27T14:23-0800 katatui/src/nativeMain/kotlin/.../CanvasExt.kt — CanvasMarker enum; setMarker/xBounds/yBounds/circle/line/rectangle/beginPoints/point/commitPoints; uses import aliases (MarkerDot as Dot, etc.)
2026-02-27T14:23-0800 katatui/src/nativeMain/kotlin/.../LogoExt.kt — LogoSize + MascotEyeColor enums; setSize() for Logo; setEyeColor() using EyeDefault/EyeRed import aliases
2026-02-27T14:23-0800 katatui/src/nativeMain/kotlin/.../Frame.kt — 5 new render() overloads and DSL extension functions for scrollbar/chart/canvas/logo/mascot

## Decisions (session 9)

2026-02-27T14:23-0800 Box::leak() for Scrollbar symbols — Scrollbar<'a> symbols are &'a str; pre-leaking once per set-call gives a &'static str that build_scrollbar() can use every frame at zero allocation cost
2026-02-27T14:23-0800 Dataset data leaked once at commit — Dataset<'a> takes &'a [(f64,f64)]; leaking the Vec at katatui_chart_commit_dataset() yields &'static [(f64,f64)] so build_chart() is allocation-free per frame
2026-02-27T14:23-0800 Canvas buffered commands — Canvas<'a,F> is closure-generic; buffering draw commands as CanvasCmd and inlining the Canvas::default().paint(move|ctx|{…}) in the render function avoids the 'static + generic closure lifetime problem
2026-02-27T14:23-0800 C enum variant prefixes for disambiguation — C enum values are global constants; KatatuiMarker::Bar and KatatuiGraphType::Bar (and KatatuiColor::Red vs KatatuiMascotEyeColor::Red) caused cinterop parse errors; fixed with Marker*/Eye* prefixes in Rust; Kotlin extension files use import aliases to keep code readable

## Issues (session 9)

**C global enum namespace collisions:** KatatuiMarker::Bar conflicted with KatatuiGraphType::Bar, and KatatuiMascotEyeColor::Red with KatatuiColor::Red in katatui.h. Import aliases can't fix C-level redefinition errors. Fixed by prefixing KatatuiMarker variants (MarkerDot…MarkerQuadrant) and KatatuiMascotEyeColor variants (EyeDefault, EyeRed); used import aliases in Kotlin extension files to restore readability.
**internal Style.toCValue() not auto-imported:** The extension function is internal to com.hyeonslab.katatui.widgets; callers in com.hyeonslab.katatui must add `import com.hyeonslab.katatui.widgets.toCValue` explicitly. Pattern established in BlockExt.kt but not followed in the new files until compile error was observed.

## What Changed (session 10 — consistent C enum prefixing)

2026-02-27T15:05-0800 katatui-ffi/cbindgen.toml — replaced `rename_variants = "ScreamingSnakeCase"` (no-op) with `prefix_with_name = true`; all C enum variants now namespaced as `KatatuiXxx_Variant`
2026-02-27T15:05-0800 katatui-ffi/build.rs — added `cargo:rerun-if-changed=cbindgen.toml`; explicitly loads cbindgen.toml via `Config::from_file()` and passes it via `.with_config()`; removed `.with_language()` since language is already set in the toml
2026-02-27T15:05-0800 katatui-ffi/src/types.rs — reverted KatatuiMarker variants: MarkerDot/MarkerBlock/… → Dot/Block/…; updated From<KatatuiMarker> match arms; removed workaround doc comment
2026-02-27T15:05-0800 katatui-ffi/src/widgets/logo.rs — reverted KatatuiMascotEyeColor variants: EyeDefault/EyeRed → Default/Red; updated From impl and constructor default; removed workaround doc comment
2026-02-27T15:05-0800 katatui-ffi/src/widgets/canvas.rs — KatatuiMarker::MarkerBraille → KatatuiMarker::Braille
2026-02-27T15:05-0800 katatui-ffi/src/widgets/chart.rs — KatatuiMarker::MarkerDot (×2) → KatatuiMarker::Dot
2026-02-27T15:05-0800 katatui/src/nativeInterop/cinterop/katatui.h — regenerated; all enum variants now carry type prefix (e.g. KatatuiMarker_Dot, KatatuiColor_Reset)
2026-02-27T15:05-0800 katatui/src/nativeMain/kotlin/.../StyleNative.kt — replaced bare cinterop imports with KatatuiColor_Xxx imports (unaliased); updated toColorEnum() and toCValue() bodies to use full names
2026-02-27T15:05-0800 katatui/src/nativeMain/kotlin/.../Layout.kt — replaced aliased imports with KatatuiDirection_Xxx/KatatuiConstraintKind_Xxx; extracted toCDirection() and kept toCKind() as internal fun; Layout object moved to top of file per naming convention
2026-02-27T15:05-0800 katatui/src/nativeMain/kotlin/.../CanvasExt.kt — KatatuiMarker_Xxx imports (unaliased); extracted CanvasMarker.toCMarker(); Canvas extensions moved to top of file
2026-02-27T15:05-0800 katatui/src/nativeMain/kotlin/.../ChartExt.kt — KatatuiGraphType_Xxx/KatatuiMarker_Xxx imports (unaliased); extracted GraphType.toCGraphType() and ChartMarker.toCMarker(); Chart extensions moved to top of file
2026-02-27T15:05-0800 katatui/src/nativeMain/kotlin/.../LogoExt.kt — KatatuiLogoSize_Xxx/KatatuiMascotEyeColor_Xxx imports (unaliased); extracted LogoSize.toCLogoSize() and MascotEyeColor.toCEyeColor(); Logo/Mascot extensions moved to top of file
2026-02-27T15:05-0800 katatui/src/nativeMain/kotlin/.../ScrollbarExt.kt — KatatuiScrollbarOrientation_Xxx imports (unaliased); extracted ScrollbarOrientation.toCOrientation(); Scrollbar extensions moved to top of file
2026-02-27T15:05-0800 katatui/src/nativeTest/kotlin/.../widgets/StyleNativeTest.kt — updated imports to KatatuiColor_Xxx (unaliased); updated assertEquals call-sites to use full names
2026-02-27T15:05-0800 katatui/src/nativeTest/kotlin/.../EnumMappingTest.kt — new; 35 tests covering all .toCXxx() conversion functions (Direction, Constraint, CanvasMarker, GraphType, ChartMarker, LogoSize, MascotEyeColor, ScrollbarOrientation)

## Decisions (session 10)

2026-02-27T15:05-0800 prefix_with_name requires explicit Config loading — cbindgen's Builder::with_crate() does call Config::from_root(), but the toml settings were silently ignored because the builder chain was not actually applying them (confirmed: old rename_variants was also a no-op). Fix: load config explicitly with Config::from_file() and Builder::with_config(); add cargo:rerun-if-changed=cbindgen.toml
2026-02-27T15:05-0800 Unaliased imports for C enum values — using full KatatuiXxx_Variant names at call sites (no `as Alias`) makes the enum origin explicit and avoids maintaining a mapping between short aliases and generated names; conversion functions (.toCXxx()) encapsulate the mapping in one place
2026-02-27T15:05-0800 File naming convention — for entity-named files (Layout.kt), the primary entity (Layout object) is defined first, with helper extensions below; for extension files (*Ext.kt), the extensions on the namesake type come first, supporting enums and converters below

## Issues (session 10)

**cbindgen.toml settings silently ignored by build.rs:** The Builder API's with_crate() was supposed to auto-load cbindgen.toml via Config::from_root(), but the settings (including the pre-existing rename_variants = "ScreamingSnakeCase") were never applied. Root cause unclear; fixed definitively by calling Config::from_file() explicitly and passing via with_config().

## What Changed (session 11 — review fixes)

2026-02-27T15:41-0800 katatui-ffi/build.rs — removed `cargo:rerun-if-changed=cbindgen.toml`; adding any rerun-if-changed line switches Cargo from "re-run on any package file change" to "re-run only on listed files", which would have stopped header regeneration when Rust source files changed
2026-02-27T15:41-0800 katatui/src/nativeMain/kotlin/.../StyleNative.kt — added @file:OptIn(ExperimentalForeignApi::class) at file level; removed per-function @OptIn from toCValue() and toColorEnum() for consistency with all other ext files
2026-02-27T15:41-0800 katatui/src/nativeMain/kotlin/.../Marker.kt — new file; consolidated CanvasMarker and ChartMarker (identical enums mapping to the same KatatuiMarker C type) into a single Marker enum with internal fun Marker.toCMarker()
2026-02-27T15:41-0800 katatui/src/nativeMain/kotlin/.../CanvasExt.kt — removed CanvasMarker enum and its toCMarker(); setMarker() parameter type changed to shared Marker; removed KatatuiMarker_* imports (moved to Marker.kt)
2026-02-27T15:41-0800 katatui/src/nativeMain/kotlin/.../ChartExt.kt — removed ChartMarker enum and its toCMarker(); setDatasetMarker() parameter type changed to shared Marker; removed KatatuiMarker_* imports (moved to Marker.kt)
2026-02-27T15:41-0800 katatui/src/nativeTest/kotlin/.../EnumMappingTest.kt — merged CanvasMarker+ChartMarker test sections (12 tests) into single Marker section (6 tests); 35 → 29 tests total

## Decisions (session 11)

2026-02-27T15:41-0800 Removed rerun-if-changed for cbindgen.toml — Cargo's default (re-run on any package file change) is correct here; adding a partial list would break header regeneration when Rust source changes; explicit Config::from_file() in build.rs is sufficient to ensure the toml is always loaded
2026-02-27T15:41-0800 Consolidated Marker enum — CanvasMarker and ChartMarker both mapped identically to KatatuiMarker; a single Marker enum in Marker.kt removes the duplication and lets callers use canvas.setMarker(Marker.Dot) and chart.setDatasetMarker(Marker.Dot) interchangeably

## Issues (session 11)

**cargo:rerun-if-changed regression from session 10:** build.rs added `println!("cargo:rerun-if-changed=cbindgen.toml")` to document the config dependency. This implicitly restricted Cargo's re-run trigger to cbindgen.toml only, silently preventing header regeneration on Rust source edits. Fixed by removing the line entirely.

## What Changed (session 12 — kotest assertions)

2026-02-27T16:25-0800 codegen/build.gradle.kts — added `testImplementation(libs.kotest.assertions.core)` (was missing; katatui already had it)
2026-02-27T16:25-0800 codegen/src/test/.../CFunctionTest.kt — replaced assertEquals/assertNull with shouldBe/shouldBe(null)
2026-02-27T16:25-0800 codegen/src/test/.../HeaderParserTest.kt — replaced assertEquals/assertTrue/assertFalse/assertNotNull with shouldBe/shouldContain/shouldNotContain/checkNotNull; replaced assertFalse(x == null) with shouldBe(null)
2026-02-27T16:25-0800 codegen/src/test/.../WrapperEmitterTest.kt — replaced assertTrue/assertFalse with shouldContain/shouldNotContain
2026-02-27T16:25-0800 katatui/src/commonTest/.../StyleTest.kt — replaced assertEquals/assertTrue/assertFalse with shouldBe
2026-02-27T16:25-0800 katatui/src/commonTest/.../ColorTest.kt — replaced assertEquals/assertNotEquals/assertIs with shouldBe/shouldNotBe/shouldBeInstanceOf
2026-02-27T16:25-0800 katatui/src/commonTest/.../BordersTest.kt — replaced assertEquals with shouldBe
2026-02-27T16:25-0800 katatui/src/commonTest/.../ConstraintTest.kt — replaced assertEquals/assertIs with shouldBe/shouldBeInstanceOf
2026-02-27T16:25-0800 katatui/src/nativeTest/.../RectTest.kt — replaced assertEquals with shouldBe
2026-02-27T16:25-0800 katatui/src/nativeTest/.../widgets/StyleNativeTest.kt — replaced assertEquals/assertTrue/assertFalse with shouldBe
2026-02-27T16:25-0800 katatui/src/nativeTest/.../EnumMappingTest.kt — replaced assertEquals with shouldBe

## Decisions (session 12)

2026-02-27T16:25-0800 shouldBe(null) instead of shouldBeNull() — kotest 6.1.3's shouldBeNull() has a different import path than expected; shouldBe(null) uses the already-imported shouldBe function and is unambiguous
2026-02-27T16:25-0800 checkNotNull() for null extraction in tests — shouldNotBeNull() failed to infer the type parameter 'T' in certain chains (find().shouldNotBeNull()); stdlib checkNotNull() is unambiguous and gives the compiler a clear non-null type

## Issues (session 12)

**shouldBeNull/shouldNotBeNull unresolved:** `import io.kotest.matchers.shouldBeNull` was unresolved in kotest 6.1.3. Fixed by using `shouldBe(null)` / `shouldNotBe(null)` instead, which requires only the existing `shouldBe` import.
**shouldNotBeNull() type inference failure:** Calling `.shouldNotBeNull()` on a chained `List.find()` result caused "Cannot infer type for type parameter 'V'" errors on subsequent uses of the returned value. Replaced with `checkNotNull()` which the compiler always handles correctly.

## What Changed (session 13)

2026-02-27T17:06-0800 sample-app/src/nativeMain/kotlin/.../main.kt — expanded sample app from 3 to 7 tabs (Dashboard, Chart, Canvas, Scrollbar, Branding, Widgets, Image); added renderChart/renderCanvas/renderScrollbar/renderBranding render functions; SCROLL_LINES constant; ScrollbarState lifecycle in main(); WIDGET_ROWS expanded to 15 rows; key bindings 1-7 and updated coerceAtMost for ◄/►
2026-02-27T17:06-0800 devlog/plans/000001-03-expand-sample-app.md — plan file for this session

## Decisions (session 13)

2026-02-27T17:06-0800 One tab per new widget — Chart, Canvas, Scrollbar each get their own tab to clearly isolate the widget demo; Logo and Mascot share a Branding tab since they're thematically paired
2026-02-27T17:06-0800 tick-driven animation, no extra state — Chart/Canvas/Scrollbar all derive their animated values from the existing `tick` counter; only ScrollbarState requires a new object (it holds C-side cursor state that must persist across frames)

## Commits

3555458 — feat: add Scrollbar, Chart, Canvas, Logo, and Mascot widgets
0f0440b — refactor: use cbindgen prefix_with_name for consistent C enum namespacing
ac16d1b — refactor: consolidate Marker enums and fix review issues
bf11cb1 — test: remove redundant null assertion and unused import in HeaderParserTest
7dbdbcc — test: migrate all assertions to kotest
4342e07 — feat: expand sample app to showcase all 15 widgets across 7 tabs
HEAD — fix: always link katatui Rust FFI release lib regardless of Kotlin binary type

## Issues (session 13 cont.)

**`./gradlew build` failed — `ld: library 'katatui_ffi' not found` on macosX64:** The `katatui/build.gradle.kts` `binaries.all` block used `if (optimized) "release" else "debug"` to select the Rust library path. Test binaries are non-optimized, so they looked for the `debug` build. The cargo task only builds `--release`. For `macosArm64` this was masked because `buildKatatuiFfiHeader` incidentally produced a debug arm64 lib. For `macosX64` no debug lib was ever created. Fixed by always linking against `release` — the Rust optimization level is independent of the Kotlin binary type.

## What Changed (session 14 — PR review fixes)

2026-02-27T18:41-0800 katatui-ffi/src/widgets/chart.rs — KatatuiDataset.data changed from &'static [(f64,f64)] to Vec<(f64,f64)> (owned); removed Box::leak from commit_dataset; commit_dataset now uses std::mem::take directly; added uncommitted-data warning in katatui_chart_free; removed build_chart (no longer viable with &'a slice API); added #[derive(Clone)] to KatatuiDataset and AxisBuilder; made x_axis/y_axis pub(crate) and AxisBuilder fields pub(crate)
2026-02-27T18:41-0800 katatui-ffi/src/widgets/scrollbar.rs — changed four symbol fields from Option<&'static str> to Option<String>; set_*_symbol now assigns owned String directly (no Box::leak); removed build_scrollbar (replaced by inline build in lib.rs); made symbol fields pub(crate)
2026-02-27T18:41-0800 katatui-ffi/src/widgets/canvas.rs — katatui_canvas_clear now resets current_points_color to Color::Reset; katatui_canvas_commit_points guards against empty batch (early return)
2026-02-27T18:41-0800 katatui-ffi/src/lib.rs — removed build_scrollbar import; katatui_frame_render_scrollbar now clones all symbol/style fields into locals and builds Scrollbar<'_> inside the closure using as_deref(); removed build_chart import, replaced with build_axis; katatui_frame_render_chart now clones datasets/axes/style into closure and builds Chart<'_> inline (Dataset::data takes &'a [(f64,f64)] not Cow)
2026-02-27T18:41-0800 katatui/src/nativeMain/kotlin/.../ScrollbarState.kt — added require(value in 0..65535) to all three property setters
2026-02-27T18:41-0800 katatui/src/nativeMain/kotlin/.../CanvasExt.kt — circle/line/rectangle/beginPoints param changed from Style to Color; internally wraps Style(fg = color).toCValue(); added Canvas.clear() extension calling katatui_canvas_clear
2026-02-27T18:41-0800 katatui/src/nativeMain/kotlin/.../Frame.kt — scrollbar() params reordered: state first, area second (consistent with list())
2026-02-27T18:41-0800 katatui/build.gradle.kts — added comment to linkerOpts explaining always-release linking
2026-02-27T18:41-0800 sample-app/src/.../main.kt — canvas call sites changed from Style(fg = Color.X) to Color.X; scrollbar() call updated to new param order; auto-scroll replaced with manual ↑/↓ (scrollOffset var); KEY_UP/KEY_DOWN imports added; help text updated with ↑/↓ hint
2026-02-27T18:41-0800 katatui/src/nativeTest/.../ScrollbarStateTest.kt — new; 12 tests (valid/upper-bound/negative/65536) × 3 properties
2026-02-27T18:41-0800 codegen/src/test/.../HeaderParserTest.kt — added KatatuiScrollbarState typedef + state fns to sampleHeader; added `widgetGroups excludes scrollbar_state` test
2026-02-27T18:41-0800 codegen/src/test/.../WrapperEmitterTest.kt — added graphTypeSetter CFunction (KatatuiGraphType param); added `setter with complex Katatui enum param is excluded` test

## Decisions (session 14)

2026-02-27T18:41-0800 Build chart inside closure — Dataset::data takes &'a [(f64,f64)] not Into<Cow>; build_chart returning Chart<'static> was incompatible with owned Vec data; cloning datasets into closure and building Chart<'_> locally (same as canvas pattern) is correct
2026-02-27T18:41-0800 Scrollbar built inline with Scrollbar<'_> — Scrollbar::new returns Scrollbar<'static>; after applying &str symbols from owned Strings, the type becomes Scrollbar<'closer>; using explicit `let mut widget: Scrollbar<'_>` + as_deref() resolves the type inference issue
2026-02-27T18:41-0800 pub(crate) for axis/symbol fields — lib.rs is in the same crate as the widget modules; pub(crate) gives the render functions access to clone fields without exposing them in the C API

## Issues (session 14)

**Dataset::data takes &'a not Cow:** Plan claimed ratatui 0.30's Dataset::data accepted Into<Cow>; actual signature is const fn data(self, data: &'a [(f64,f64)]) -> Self. Resolved by restructuring render to build Chart inside the closure.
**Scrollbar type inference with as_str():** Calling widget.thumb_symbol(sym.as_str()) caused E0282 ("cannot infer type"); root cause is lifetime narrowing from Scrollbar<'static> to Scrollbar<'shorter>. Fixed with as_deref() on Option<String> and explicit Scrollbar<'_> annotation.
**Private symbol fields in scrollbar.rs:** lib.rs (same crate) needs access to symbol fields for cloning; fixed by changing private to pub(crate).

## What Changed (session 15 — Katatui Code TUI tab)

2026-02-27T19:17-0800 devlog/plans/000001-04-cc-tab.md — plan for Katatui Code 8th tab
2026-02-27T19:17-0800 katatui-ffi/src/lib.rs — added `KeyCode::Backspace => 0x08` and `KeyCode::Tab => 0x09` to katatui_event_read_key_code match
2026-02-27T19:17-0800 katatui/src/nativeMain/kotlin/.../Event.kt — added KEY_BACKSPACE = '\b' and KEY_TAB = '\t' constants
2026-02-27T19:17-0800 sample-app/src/.../sample/KatatuiCode.kt — new; ChatMessage/Sender domain model; KatatuiCodeState (messages, input, cursorPos, scrollOffset, selectedSuggestion, computed suggestions/isCommandMode); KatatuiCodeIntent sealed interface (9 intents); pure reduce() function; KatatuiCodeStore wrapping state+dispatch; KatatuiCodeEnv data class; readEnv() reading HOME/PWD; readGitBranch() via fopen
2026-02-27T19:17-0800 sample-app/src/.../sample/renderKatatuiCode.kt — new; Frame.renderKatatuiCodeTab() with 4-region layout; renderMessages() with logo/text split + scrollbar; renderSuggestions()/renderInput()/renderStatusBar() helpers; flattenMessages/formattedSuggestions/inputWithCursor private helpers; LOGO_HEIGHT = 6 constant
2026-02-27T19:17-0800 sample-app/src/.../sample/main.kt — added "Katatui Code" to TAB_NAMES; KatatuiCodeStore + readEnv() init; '8' key binding; updated KEY_LEFT/RIGHT to dispatch CursorLeft/Right on tab 7; added KEY_UP/DOWN ScrollUp/Down dispatch; KEY_BACKSPACE/TAB/ENTER/ESC dispatch; else branch for TypeChar on printable chars; added tab 7 render case

## Decisions (session 15)

2026-02-27T19:17-0800 Private helper functions for renderKatatuiCodeTab — split into renderMessages/renderSuggestions/renderInput/renderStatusBar to keep each function focused; detekt prefers smaller functions
2026-02-27T19:17-0800 Unicode escapes for special chars in string literals — using \u276F (❯), \u25C6 (◆), \u2502 (│), \u2191 (↑), \u2193 (↓), \u2190 (←), \u2192 (→) avoids source encoding issues
2026-02-27T19:17-0800 ScrollbarState().use per-frame for CC tab — CC tab owns its scrollbar state; create+close per frame avoids adding it to the main lifecycle; .use handles AutoCloseable cleanup
2026-02-27T19:17-0800 when (val key = readKey()) — binding the key to a val in the when expression allows the else branch to reference the value for the TypeChar dispatch; key is Char? but null cannot fall in the ' '..'~' range so key!! is not needed (null check in else branch)

## Commits

4342e07 — feat: expand sample app to showcase all 15 widgets across 7 tabs
27675bf — fix: always link katatui Rust FFI release lib regardless of Kotlin binary type
3c6b74c — fix: address all PR review issues
HEAD — feat: add Katatui Code TUI tab (8th tab, MVI architecture)

## What Changed (session 16 — vertical suggestion menu)

2026-02-27T19:52-0800 sample-app/src/.../sample/KatatuiCode.kt — added `PrevSuggestion` intent to sealed interface; added reducer arm: wraps index backward with `(selectedSuggestion - 1 + size) % size`
2026-02-27T19:52-0800 sample-app/src/.../sample/renderKatatuiCode.kt — reordered layout: messages(Fill(1)), input(Length(3)), suggestions(Length(suggHeight)), status(Length(1)); `suggHeight` = `suggestions.size` when `isCommandMode && suggestions.isNotEmpty()`, else 1; `formattedSuggestions` now renders vertical list with `▶` (U+25B6) on selected item and `joinToString("\n")`; hint text updated to `/ commands` (drop TAB mention)
2026-02-27T19:52-0800 sample-app/src/.../sample/main.kt — KEY_UP dispatch: added `isCommandMode` branch → `PrevSuggestion` before the ScrollUp fallback; KEY_DOWN dispatch: added `isCommandMode` branch → `NextSuggestion` before the ScrollDown fallback; removed KEY_TAB branch entirely; removed unused `KEY_TAB` import

## Decisions (session 16)

2026-02-27T19:52-0800 Suggestions below input — vertical list is more scannable than inline horizontal chips; placing it below the input box mirrors conventional autocomplete UI patterns (IDEs, shell completions)
2026-02-27T19:52-0800 Dynamic layout height for suggestions — `Length(suggestions.size)` collapses to `Length(1)` when no suggestions, preserving layout stability (no layout re-flow visible to user when suggestions disappear)
2026-02-27T19:52-0800 ↑/↓ gated on isCommandMode — outside command mode ↑/↓ continue to scroll message history; inside command mode they navigate suggestions; consistent with how most TUI apps disambiguate context-sensitive keys

## Commits

HEAD — feat: vertical suggestion menu for Katatui Code tab (↑/↓ navigation)

## What Changed (session 17 — katatui logo)

2026-02-27T20:21-0800 katatui-ffi/src/widgets/logo.rs — replaced `build_logo` return type from `RatatuiLogo` to `Text<'static>`; added `KATATUI_LOGO_TINY` and `KATATUI_LOGO_SMALL` constants spelling "katatui" with block characters in the same style as the ratatui logo; only the first letter differs (`r` → `k`): tiny uses `▌▞`/`▌▚` (left bar + diagonal slash/backslash), small uses `█▌▞▝`/`█▌▚▗` (heavy bar + left half + diagonal + quarter tip)

## Decisions (session 17)

2026-02-27T20:21-0800 Return `Text<'static>` from `build_logo` — `RatatuiLogo` is just `Text::raw(static_str)` internally; returning `Text<'static>` directly is simpler and removes the dependency on ratatui's logo widget; `Text` implements `Widget` so the lib.rs render call is unchanged
2026-02-27T20:21-0800 Only replace the `r` glyph — all other letters in "ratatui" are identical to "katatui"; reusing the existing block-character designs for a, t, u, i preserves the visual style; k is designed as `▌▞`/`▌▚` (tiny: left-half + forward/backslash diagonal) and `█▌▞▝`/`█▌▚▗` (small: full + left-half + diagonal + quarter tip)

## Commits

HEAD — feat: replace ratatui logo with katatui block-character logo

## What Changed (session 18 — MVI refactor: AppStore)

2026-02-27T21:12-0800 sample-app/src/.../sample/App.kt — new; TAB_NAMES/WAVE/SCROLL_LINES constants (moved from main.kt); AppState data class (running, activeTab, tick, history, scrollOffset, codeState; cpuPct/memPct computed properties); AppIntent sealed interface (Quit, Tick, SelectTab, TabLeft, TabRight, ScrollUp, ScrollDown, Code, KeyPress); reduce() top-level function; private reduceKey() helper centralising all key→intent translation; AppStore class (state + dispatch)
2026-02-27T21:12-0800 sample-app/src/.../sample/KatatuiCode.kt — removed KatatuiCodeStore class; state ownership moved to AppState.codeState
2026-02-27T21:12-0800 sample-app/src/.../sample/main.kt — removed TAB_NAMES/WAVE/SCROLL_LINES constants and local vars (activeTab, tick, scrollOffset, history, store); removed KEY_* imports (moved to App.kt); replaced while(true)+break with while(appStore.state.running); main loop now: Tick dispatch → snapshot state → draw → KeyPress dispatch; zero conditional logic in event loop

## Decisions (session 18)

2026-02-27T21:12-0800 KeyPress captures raw Char? and reduceKey does all routing — removes all conditional dispatch logic from main.kt; key-to-intent translation is in one testable place; main loop is three lines: tick, draw, forward key
2026-02-27T21:12-0800 AppStore composes KatatuiCodeState — codeState becomes a field of AppState; Code(intent) arm delegates to the existing KatatuiCodeState reducer; KatatuiCodeStore removed as redundant wrapper

## Commits

HEAD — refactor: full MVI architecture for sample-app (AppStore, AppState, AppIntent)

## What Changed (session 19 — Picker FFI + image tab lifecycle)

2026-02-28T00:00-0800 katatui-ffi/src/widgets/image.rs — added `KatatuiPicker` opaque struct (pub(crate) inner field keeps cbindgen opaque); `katatui_picker_new()` calls `Picker::from_query_stdio()` on whichever thread calls it (must be main); `katatui_picker_free()`; `katatui_image_state_from_bytes_with_picker(data, len, picker)` — image decode path using existing picker
2026-02-28T00:00-0800 katatui/src/nativeInterop/cinterop/katatui.h — rebuilt via `:katatui:buildKatatuiFfiHeader`; added `KatatuiPicker` opaque typedef and three new function declarations
2026-02-28T00:00-0800 katatui/build/generated-sources/katatui/.../Picker.kt — codegen auto-detected `KatatuiPicker` struct and generated `Picker` class (extends KatatuiWidget, ptr, close(), companion invoke)
2026-02-28T00:00-0800 katatui/src/nativeMain/.../ImageState.kt — added `fromBytesWithPicker(bytes, picker)` factory calling `katatui_image_state_from_bytes_with_picker`
2026-02-28T00:00-0800 gradle/libs.versions.toml — re-added `coroutines = "1.10.1"` and `kotlinx-coroutines-core` library entry (removed in earlier synchronous attempt, restored with coroutines approach)
2026-02-28T00:00-0800 sample-app/build.gradle.kts — re-added `implementation(libs.kotlinx.coroutines.core)` to nativeMain dependencies
2026-02-28T00:00-0800 sample-app/src/.../main.kt — `val picker = Picker()` created on main thread before loop; `scope.async { ImageState.fromBytesWithPicker(LEAP_DARK_PNG, picker) }` offloads decode to background; added `var previousTab = -1`; added image lifecycle cleanup: when `previousTab == 6 && state.activeTab != 6`, cancel imageFuture + close imageState + null both; `renderImageTab` has `loading` param restored; `picker.close()` in cleanup; `@OptIn(ExperimentalCoroutinesApi::class)` for `Deferred.getCompleted()`

## Decisions (session 19)

2026-02-28T00:00-0800 Split picker creation from image decode — `Picker::from_query_stdio()` queries the terminal (stdin/stdout) to detect the best image protocol; if run on a background thread it races with `readKey()` on the main thread, corrupting key events AND causing halfblocks fallback (pixelated image). Solution: `katatui_picker_new` runs on the main thread; `katatui_image_state_from_bytes_with_picker` runs on the background thread. Passing `picker` (just a raw pointer) across threads is safe here because main thread does not use picker after construction.
2026-02-28T00:00-0800 pub(crate) inner field in KatatuiPicker — makes cbindgen emit opaque `typedef struct KatatuiPicker KatatuiPicker;` without exposing the Picker type in the C header; cinterop then exposes it as `cnames.structs.KatatuiPicker`
2026-02-28T00:00-0800 Delete hand-written Picker.kt — codegen auto-detected `KatatuiPicker` and generated an equivalent class; keeping both caused `Redeclaration: class Picker` compile error; the generated version has all needed members
2026-02-28T00:00-0800 Free ImageState on tab leave — ImageState holds GPU/terminal memory for the image protocol; freeing on leave and recreating on next visit avoids holding that memory indefinitely while the user navigates other tabs

## Issues (session 19)

**Hand-written Picker.kt conflicted with codegen:** Created `katatui/src/.../Picker.kt` manually before realising codegen would auto-generate it from the new `KatatuiPicker` struct in the header. Both declared `class Picker : KatatuiWidget` → compile error `Redeclaration`. Fixed by deleting the hand-written file.
**ExperimentalCoroutinesApi opt-in missing:** `Deferred.getCompleted()` requires `@OptIn(ExperimentalCoroutinesApi::class)`. Added to `main()`.

## What Changed (session 20 — KatatuiCode scroll + logo in paragraph)

2026-02-28T00:00-0800 sample-app/src/.../sample/KatatuiCode.kt — redefined `scrollOffset` semantics: 0 = pinned to latest (bottom), N = scrolled up N rows from bottom; `ScrollUp` → `scrollOffset + 1`, `ScrollDown` → `maxOf(0, scrollOffset - 1)`, `Accept` → `scrollOffset = 0`
2026-02-28T00:00-0800 sample-app/src/.../sample/renderKatatuiCode.kt — replaced `logo()` widget + separate paragraph approach with unified `LOGO_LINES` constant (literal block-character strings from KATATUI_LOGO_SMALL); `renderMessages` now: `val allLines = LOGO_LINES + textLines`; single scroll loop + single `paragraph` covers both logo and messages; `clampedOffset` recalculated as `maxOf(0, maxOffset - state.scrollOffset)` to convert bottom-distance to top-anchored offset; removed `logo`, `setSize`, `LogoSize` imports (no longer needed)

## Decisions (session 20)

2026-02-28T00:00-0800 scrollOffset as distance from bottom — eliminates Int.MAX_VALUE sentinel; 0 naturally means "show latest" (correct chat default); ScrollUp/ScrollDown are simple +1/-1; viewport-clamping is done at render time, not in the reducer
2026-02-28T00:00-0800 Hardcode LOGO_LINES instead of logo() widget — the `logo()` widget always renders from its own row 0; when partially visible it clips from the bottom (shrinks), not from the top (slides off). To make the logo truly scroll as content, it must be text lines prepended to the paragraph's line list

## What Changed (session 21 — logo text fix)

2026-02-28T00:00-0800 sample-app/src/.../sample/renderKatatuiCode.kt — fixed unicode escape errors in LOGO_LINES (▘ and ▌ were both incorrectly `\u2588` (█)); replaced all unicode escapes with literal characters; reverted attempted 'k' gap fill (`██▞▞`/`██▚▚`) — looked worse visually; restored original `█▌▞▝`/`█▌▚▗`

## Commits

e0c5c1a — feat: Katatui Code tab, MVI app architecture, Picker FFI image fix, katatui logo

## What Changed (session 22 — PR review fixes)

2026-02-28T07:17-0800 katatui-ffi/src/widgets/chart.rs — replaced `eprintln!` warning in `katatui_chart_free` with auto-commit: if `current_data` is non-empty when the chart is freed, the pending dataset is committed rather than silently dropped; removes invisible stderr noise while preventing data loss
2026-02-28T07:17-0800 katatui-ffi/src/lib.rs — added `// SAFETY:` comment on the `render_stateful_widget` call (scrollbar state lifetime invariant: caller must not free `state` between `begin_draw` and `end_draw`; Kotlin wrapper guarantees `ScrollbarState` outlives the draw closure)
2026-02-28T07:17-0800 katatui/src/nativeMain/.../BlockExt.kt — moved `@OptIn(ExperimentalForeignApi::class)` from function to file level (`@file:OptIn`) for consistency with all other `*Ext.kt` files
2026-02-28T07:17-0800 katatui/src/nativeMain/.../ScrollbarState.kt — moved `@OptIn(ExperimentalForeignApi::class)` from class to file level (`@file:OptIn`) for consistency
2026-02-28T07:17-0800 katatui/src/nativeMain/.../Layout.kt — added comment on `maxRects = constraints.size + 1` explaining this is a C API requirement documented in `layout.rs`

## Decisions (session 22)

2026-02-28T07:17-0800 Auto-commit on free instead of removing eprintln! — removing the warning entirely would make the data-loss silent with no fix; auto-committing converts the bug into correct behavior (pending dataset is preserved); consistent with the "be liberal in what you accept" principle for resource destructors
2026-02-28T07:17-0800 `@file:OptIn` over per-declaration `@OptIn` — all other `*Ext.kt` files use file-level opt-in; this avoids having to annotate every new function added later and is the idiomatic Kotlin convention for files that are uniformly experimental

## Commits

4e2de2a — fix: address PR review comments (chart, scrollbar, OptIn, layout)

## What Changed (session 23 — event-driven render loop)

2026-02-28T09:09-0800 katatui-ffi/src/lib.rs — added `use std::time::Duration`; added `katatui_event_read_extended(timeout_ms: u64) -> u32`: blocks via `event::poll(timeout)`, returns 256 on timeout (Tick), key code on key press, 0 on other events
2026-02-28T09:09-0800 katatui/src/nativeInterop/cinterop/katatui.h — rebuilt via `:katatui:buildKatatuiFfiHeader`; added `katatui_event_read_extended(uint64_t timeout_ms)` declaration
2026-02-28T09:09-0800 katatui/src/nativeMain/kotlin/.../Event.kt — added import for `katatui_event_read_extended`; added `TerminalEvent` sealed interface (Tick/Key/Other); added `readEvent(timeoutMs: Long = 100L)` function mapping the u32 return to TerminalEvent
2026-02-28T09:09-0800 sample-app/src/.../main.kt — replaced `poll` + `readKey` imports with `TerminalEvent` + `readEvent`; replaced tick-first loop with event-driven loop using `when (val ev = readEvent())`: Tick advances app state + manages image lifecycle, Key dispatches KeyPress, Other skips rendering; `shouldRender` boolean eliminates duplicate draw block; draw is called once with a fresh `appStore.state` snapshot

## Decisions (session 23)

2026-02-28T09:09-0800 poll(timeout)→Tick instead of push_event — the plan called for a Rust background thread injecting Resize(u16::MAX,u16::MAX) via `event::push_event`; `push_event` does not exist in crossterm 0.28 or 0.29 (confirmed by cargo build error and registry source search). The `event::poll(timeout_ms)` approach achieves identical semantics: the OS blocks the thread until a key arrives or the timeout elapses; zero CPU when idle; key presses return instantly; no background thread or synthetic event needed.
2026-02-28T09:09-0800 No KatatuiTickSource struct — the plan's TickSource was only needed to own the background tick thread. With the poll-based approach there is no background thread; the tick interval is a parameter to `katatui_event_read_extended`. No new opaque C struct, no codegen-generated class.
2026-02-28T09:09-0800 shouldRender boolean pattern — eliminates duplicate draw blocks (plan showed Tick and Key each with their own draw call). Using `when` as an expression returning Bool and a single `if (shouldRender) draw { … }` block keeps the render code in one place and avoids `continue` inside a `try` (which would incorrectly fire `finally`).
2026-02-28T09:09-0800 Image lifecycle remains Tick-only — previousTab tracking and image free/null only runs in the Tick branch (mirrors original behaviour); key events dispatch + render without touching the lifecycle

## Issues (session 23)

**`event::push_event` not in crossterm 0.28/0.29:** Plan assumed `event::push_event` exists in crossterm 0.28. Cargo build failed with `E0425: cannot find function 'push_event' in module 'event'`. Confirmed absent in both 0.28.1 and 0.29.0 in the local registry. Resolved by using `event::poll(timeout)` returning 256 on timeout instead.

## Commits

3d7dd0d — feat: event-driven render loop (blocking poll, immediate key feedback)

## What Changed (session 24 — remove polling event API)

2026-02-28T10:17-0800 katatui-ffi/src/lib.rs — deleted `katatui_event_poll` and `katatui_event_read_key_code` functions; both are dead code now that the event-driven `katatui_event_read_extended` path is the only consumer; `use std::time::Duration` and `use crossterm::event::KeyEventKind` kept (still used by `katatui_event_read_extended`)
2026-02-28T10:17-0800 katatui/src/nativeInterop/cinterop/katatui.h — rebuilt via `:katatui:buildKatatuiFfiHeader`; declarations for `katatui_event_poll` and `katatui_event_read_key_code` removed
2026-02-28T10:17-0800 katatui/src/nativeMain/kotlin/.../Event.kt — removed imports for `katatui_event_poll`, `katatui_event_read_key_code`, `kotlin.time.Duration`, `kotlin.time.Duration.Companion.milliseconds`; removed `poll()`, `pollMillis()`, `readKey()` functions; kept `KEY_*` constants, `TerminalEvent`, and `readEvent()`
2026-02-28T10:17-0800 sample-app-swift/Sources/.../DSL.swift — replaced `Event.poll(timeoutMillis:)` + `Event.readKey()` with single `Event.readEvent(timeoutMs:)` delegating to `EventKt.readEvent`
2026-02-28T10:17-0800 sample-app-swift/Sources/.../main.swift — replaced `while true { tick++; draw; if poll { if readKey == "q" break } }` with `mainLoop: while true { switch Event.readEvent() { case .tick: tick++; draw; case .key: if key?.character == "q" break mainLoop; case .other: break } }`

## Decisions (session 24)

2026-02-28T10:17-0800 Remove poll/readKey, keep KEY_* constants — `poll` and `readKey` are dead code (Kotlin sample already uses `readEvent`; Swift sample was the only remaining caller). KEY_* constants are still imported in `App.kt` and are part of the public API for key comparison, so they stay.
2026-02-28T10:17-0800 Swift switch on TerminalEvent — mirrors the Kotlin sample's `when (readEvent())` pattern; SKIE bridges Kotlin sealed interface → Swift enum automatically; `key?.character` extracts Swift `Character` from the SKIE-bridged `KotlinChar?`

## Commits

379d53e — refactor: remove deprecated poll/readKey API, migrate Swift sample to readEvent

## What Changed (session 25 — review fixes)

2026-02-28T12:15-0800 katatui-ffi/src/lib.rs — fixed stale doc comment on `katatui_event_read_extended`: replaced "same mapping as katatui_event_read_key_code" (deleted function) with inline key-code table
2026-02-28T12:15-0800 sample-app-swift/Sources/.../main.swift — aligned Swift event loop with Kotlin pattern: extracted `shouldRender` bool; `.tick` and `.key` both set it true; draw block moved outside switch; previously `.key` never triggered a redraw

## Decisions (session 25)

2026-02-28T12:15-0800 shouldRender pattern in Swift — mirrors Kotlin's `when (ev) { Tick -> …; Key -> …; Other -> false }` exactly; draw block is in one place; `break mainLoop` inside `.key` still works because Swift `break <label>` targets the named while loop, not the switch

## Commits

HEAD — fix: stale doc comment and missing key-event redraw in Swift sample
