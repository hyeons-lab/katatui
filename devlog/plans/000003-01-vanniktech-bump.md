## Thinking

The first run of the publish workflow on `main` (run 25588889433) failed with:

> Snapshots are not supported when publishing through the central portal.

vanniktech-maven-publish 0.30.0 routes every version (including `*-SNAPSHOT`) to the production Central Portal endpoint, which rejects snapshots. Snapshot routing to the dedicated repo at `central.sonatype.com/repository/maven-snapshots/` was added in vanniktech 0.31.0.

Two paths considered:
1. Bump vanniktech to a version that supports snapshot routing.
2. Drop `-SNAPSHOT` from `gradle.properties` and cut a real `0.1.0` release.

User chose option 1 with the latest version (0.36.0).

0.36.0 has a few breaking changes vs 0.30.0; relevant ones for this project:
- `SonatypeHost` enum was removed from the public `publishToMavenCentral(...)` signature. The no-arg form now targets Central Portal by default.
- Defaults around javadoc jar emission shifted, but only when `configure(...)` is called manually — not our case (we use the plugin block + `mavenPublishing { ... }`).
- Min Kotlin 2.2, Gradle 9, JDK 17 — already met (Kotlin 2.3.10, Gradle 9.3.1, JDK 21).

Also opportunistically pinning runner labels to explicit versions (`macos-26`, `ubuntu-24.04`, `windows-2025`) so future re-pointing of `-latest` aliases doesn't silently change CI behavior.

## Plan

1. Bump `gradle/libs.versions.toml` → `maven-publish = "0.36.0"`.
2. Update `katatui/build.gradle.kts`: replace `publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)` with `publishToMavenCentral()` (no args, no `automaticRelease` — manual release confirmation step preserved per user preference).
3. Pin runners in `.github/workflows/ci.yml` and `.github/workflows/publish.yml`: `macos-15` → `macos-26`, `ubuntu-latest` → `ubuntu-24.04`, `windows-latest` → `windows-2025`. Update the `if: matrix.os == 'ubuntu-latest'` guard in publish.yml accordingly.
4. Verify locally: `./gradlew :katatui:tasks --group=publishing` and `./gradlew ktfmtCheck detekt`.
5. Commit + open PR.

Note: Central Portal snapshot publishing also requires snapshots to be enabled for the `com.hyeonslab` namespace at central.sonatype.com — not a code change, flagging in the PR body.
