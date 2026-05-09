## Thinking

PR #6 fixed the namespace path (com.hyeons-lab) and unblocked uploads to the snapshot repo, but a new failure surfaced: the published snapshot only listed *one* platform's variants, not all five. Each runner's `publishAllPublicationsToMavenCentral` writes its own root `katatui-0.1.0-SNAPSHOT.module` describing only the targets that runner had configured (e.g. on Mac: `macosArm64`, `macosX64`). With three runners PUTing the same path, the root metadata is whoever-wrote-last; the other variants get clobbered.

User identified this by inspecting `katatui-0.1.0-SNAPSHOT.module`. Verified locally — the Mac mavenLocal output's root metadata listed only Mac variants, no Linux/Windows.

Two viable fixes:
1. **Multi-runner with per-target publishes + designated metadata publisher.** Each runner publishes only its target-specific publications; one runner publishes the kotlinMultiplatform (root) publication. Requires declaring all targets on the metadata-publishing runner so the root .module lists them all. Tried this — broke on `commonizeCInterop`, which needs cinterop output for every declared target. Disabling cinterop commonization isn't viable: `nativeMain` imports the commonized cinterop API.
2. **Single-runner cross-compile on Mac.** Mac runner installs Rust toolchains *and* cross-compile linkers for Linux + Windows, builds everything natively, and publishes everything in one `publishAllPublicationsToMavenCentral` invocation. One root metadata write, complete variant list, no overwrite.

Going with #2. Simpler Gradle config (no per-target task disabling), more CI work but the work is pretty mechanical (`brew install` the right packages).

## Plan

1. **`katatui/build.gradle.kts`**
   - Add a `crossCompile` property gate (read from `-PcrossCompile=true`).
   - In the `kotlin {}` block, declare targets based on host *or* `crossCompile`. Without the flag, only the host's native targets are declared (preserves existing per-platform CI builds and local dev). With the flag, all five are declared so the root .module lists every variant.
   - Cargo task `isEnabled = isNativeTarget || crossCompile` so the Mac publish runner builds Rust for every target.
   - Fix the `binaries.framework {}` guard: was `if (isMac)` (host check), now `if (this.name.startsWith("macos"))` (target check). The previous guard was harmless when only Mac targets were declared on Mac; with all five declared, it tried to add a framework to Linux/Windows targets (frameworks are Apple-only).

2. **`.github/workflows/publish.yml`**
   - Drop the OS matrix. One job, `runs-on: macos-26`.
   - Install all five Rust targets via `dtolnay/rust-toolchain@stable`.
   - `brew install` the cross-compile linkers: `messense/macos-cross-toolchains/x86_64-unknown-linux-gnu`, `aarch64-unknown-linux-gnu`, and `mingw-w64`.
   - Set `CARGO_TARGET_*_LINKER` env vars to override `katatui-ffi/.cargo/config.toml` (which is keyed for Ubuntu's `aarch64-linux-gnu-gcc` from the existing per-platform Linux CI job — the Mac brew packages use different binary names).
   - Run `./gradlew :katatui:publishAllPublicationsToMavenCentral -PcrossCompile=true`.

3. **No `katatui-ffi/.cargo/config.toml` change.** Tempting to add cross-compile linkers there too, but the binary names differ between the Linux apt package (`aarch64-linux-gnu-gcc`) and the Mac brew package (`aarch64-unknown-linux-gnu-gcc`); putting Mac names in config.toml would break the Linux CI job. Env-var overrides on the Mac publish runner are cleaner.

4. **Verified locally.** Default-mode `:katatui:publishToMavenLocal` (no flag) still produces a Mac-only artifact — same behavior as before. `crossCompile=true` build can't be verified without the linkers installed; CI will be the integration test.
