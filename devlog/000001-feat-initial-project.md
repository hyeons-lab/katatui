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

## Next Steps

- Push and update PR #1
