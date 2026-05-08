## Thinking

The user wants to publish Katatui to the local Maven repository so it can be consumed as a regular dependency by other Kotlin projects. The repo already declares `GROUP=com.hyeons-lab` and `VERSION_NAME=0.1.0-SNAPSHOT` in `gradle.properties`, but no module applies the `maven-publish` Gradle plugin, so `publishToMavenLocal` is not registered.

Kotlin Multiplatform integrates with `maven-publish` automatically: when the plugin is applied, the KMP plugin creates one publication per target (`macosArm64`, `macosX64`, …) plus a "root" publication that carries the common metadata KLib. Consumers depend on the root coordinate and Gradle resolves the per-target artifact via the Gradle module metadata.

Scope decisions:
- Apply `maven-publish` only to the `katatui` library module. `:codegen` is an internal build-time tool (used only by `:katatui`'s `generateKotlinWrappers` task) and `:sample-app` is an executable demo — neither belongs on a Maven coordinate.
- Read `group` and `version` from the existing `GROUP`/`VERSION_NAME` properties so the source of truth stays in `gradle.properties`.
- Don't add POM metadata (license, SCM, developers, etc.) yet. The user's ask is "publish to maven local" — POM enrichment is needed for a real Maven Central publish, not for local consumption. Defer until there's a publish-to-central plan.
- Don't add a sources/javadoc jar config. KMP's default publication already wires sources for the metadata jar; full javadoc requires Dokka and is out of scope.

Per-host caveat: the existing build only enables Rust/Kotlin native targets that match the host OS (`isMac` → `macosArm64` + `macosX64`, etc.). So `publishToMavenLocal` on macOS publishes only the macOS artifacts. That's expected for local development; cross-host publishing is a CI concern.

Rust toolchain prerequisite: cargo needs the `x86_64-apple-darwin` target installed for `macosX64`. On Apple Silicon dev boxes, `rustup target add x86_64-apple-darwin` is required once. This isn't something to wire into Gradle — it's a one-shot env setup, documented (or auto-detected) elsewhere if needed.

## Plan

1. Edit `katatui/build.gradle.kts`:
   - Add `` `maven-publish` `` to the `plugins {}` block.
   - Set `group` from `providers.gradleProperty("GROUP")`.
   - Set `version` from `providers.gradleProperty("VERSION_NAME")`.
2. Verify with `./gradlew :katatui:publishToMavenLocal` and inspect `~/.m2/repository/com/hyeons-lab/`. Expect:
   - `katatui/0.1.0-SNAPSHOT/` (root + metadata)
   - `katatui-macosarm64/0.1.0-SNAPSHOT/`
   - `katatui-macosx64/0.1.0-SNAPSHOT/`
3. Update devlog and open PR.
