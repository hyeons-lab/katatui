# Katatui

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A Kotlin Multiplatform TUI library — idiomatic Kotlin bindings for
[ratatui](https://ratatui.rs), the battle-tested Rust terminal-UI framework.

## Why Katatui?

Katatui wraps ratatui's rendering engine with a Kotlin-native DSL.
You get ratatui's performance and widget library, expressed in idiomatic Kotlin:
builder DSLs, sealed classes, extension functions, and `AutoCloseable` lifecycle —
no unsafe Rust required.

## Quickstart

### Gradle

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        nativeMain.dependencies {
            implementation("com.hyeons-lab:katatui:0.1.0")
        }
    }
}
```

### Hello World

**Kotlin (Katatui):**
```kotlin
import com.hyeonslab.katatui.*
import com.hyeonslab.katatui.widgets.*

fun main() {
    Terminal().use { terminal ->
        terminal.init()
        terminal.draw { frame ->
            frame.render(
                Paragraph("Hello from Katatui!"),
                frame.size,
            )
        }
        katatui_event_poll(0u)  // wait for any key
    }
}
```

For comparison, the equivalent Rust program:
```rust
use ratatui::{DefaultTerminal, Frame};

fn main() {
    let terminal = ratatui::init();
    terminal.draw(|f| f.render_widget("Hello from ratatui!", f.area())).unwrap();
    crossterm::event::read().unwrap();
    ratatui::restore();
}
```

## What Makes Katatui Idiomatic Kotlin

| Ratatui (Rust) | Katatui (Kotlin) |
|----------------|-----------------|
| Builder chaining: `Block::default().title("T").borders(Borders::ALL)` | Lambda DSL: `Block { title = "T"; borders = Borders.ALL }` |
| Owned rendering: widget consumed on `render_widget()` | Reference rendering: widget is `AutoCloseable`, reusable within a frame |
| `Constraint::Length(3)` enum variant | `Constraint.Length(3)` sealed class subtype |
| `Layout::vertical().constraints([...]).split(area)` | `Layout.vertical(Constraint.Length(3), Constraint.Fill(1)).split(area)` |
| `terminal.draw(\|frame\| { ... })` closure | `terminal.draw { frame -> ... }` trailing lambda |
| Explicit `ratatui::restore()` call | Automatic cleanup via `Terminal : AutoCloseable` |
| Match on `crossterm::event::KeyCode` | Match on `KeyCode` sealed class (Kotlin) |

## Widgets

Block · Paragraph · List · Layout · (more coming)

## Differences from ratatui

- **No async required** — Katatui's event loop is synchronous; use coroutines if needed
- **Memory managed** — All opaque handles are `AutoCloseable`; use `use { }` or explicit `close()`
- **Generated wrappers** — Widget bindings are auto-generated from ratatui's C API headers;
  new ratatui widgets can be added by regenerating with `./gradlew :katatui:generateKotlinWrappers`
- **Platform support** — macOS ARM64/x64, Linux x64/ARM64, Windows x64

## Building from Source

Requires: Rust 1.80+ · Cargo · JDK 17+

```bash
git clone https://github.com/hyeons-lab/katatui
cd katatui
cargo build --release --target aarch64-apple-darwin  # or your host triple
./gradlew :katatui:compileKotlinMacosArm64
./gradlew :sample-app:runDebugExecutableMacosArm64
```

## Acknowledgements

Katatui is a thin Kotlin wrapper around [ratatui](https://ratatui.rs) by
[@joshka](https://github.com/joshka) and the ratatui contributors.
All TUI rendering is performed by ratatui's engine; this library provides
only the Kotlin FFI layer and idiomatic DSL wrappers.

## License

MIT
