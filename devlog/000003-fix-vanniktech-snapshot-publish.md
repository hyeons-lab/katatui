## Agent

Claude Code (claude-opus-4-7) @ katatui branch fix/vanniktech-snapshot-publish

## Intent

The first run of the publish workflow on `main` failed with "Snapshots are not supported when publishing through the central portal." Fix the publish path so `0.1.0-SNAPSHOT` artifacts route to the Central Portal snapshot repo, and pin CI runner versions while we're in there.

## What Changed

2026-05-08T19:37-0700 gradle/libs.versions.toml — bumped `maven-publish` 0.30.0 → 0.36.0; snapshot routing to `central.sonatype.com/repository/maven-snapshots/` was added in 0.31.0.
2026-05-08T19:37-0700 katatui/build.gradle.kts — replaced `publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)` with `publishToMavenCentral()`; the `SonatypeHost` enum was removed in 0.36 and the no-arg form targets Central Portal by default. Did not pass `automaticRelease = true` (per user preference — keeps manual release confirmation step).
2026-05-08T19:37-0700 .github/workflows/ci.yml + .github/workflows/publish.yml — pinned runners: `macos-15` → `macos-26`, `ubuntu-latest` → `ubuntu-24.04`, `windows-latest` → `windows-2025`. Also updated the `if: matrix.os == 'ubuntu-latest'` guard in publish.yml to match.

## Decisions

2026-05-08T19:37-0700 Bump to 0.36.0 (latest) instead of the minimum-needed 0.31.0 — user preference. Project already meets all 0.36 prerequisites (Kotlin 2.3.10 ≥ 2.2, Gradle 9.3.1 ≥ 9.0, JDK 21 ≥ 17), and we don't call `configure(...)` manually so the new javadoc jar default doesn't affect us.

2026-05-08T19:37-0700 No `automaticRelease = true` — user explicitly opted out. Snapshots auto-publish regardless; for non-snapshot releases this preserves a manual confirmation step at the Central Portal UI.

2026-05-08T19:37-0700 Pin runners to explicit version labels rather than `-latest` aliases — silent re-pointing of `-latest` (e.g., `windows-latest` flipping from 2022 → 2025) has historically broken builds elsewhere; explicit pins make upgrades a deliberate, reviewable change.

## Issues

**Stale-branch push on the prior PR.** PR #3 was already squash-merged when I pushed `bd064b0` (the original vanniktech-bump commit) to `feat/initial-project-v2`. The push went to a now-orphaned branch and never reached `main`. Recovered by creating this fresh worktree from `origin/main`, cherry-picking `bd064b0` (excluding its modifications to the merged branch's devlog), and opening a new PR.

**Central Portal namespace prerequisite.** Even with vanniktech routing snapshots correctly, the `com.hyeonslab` namespace must have snapshots enabled at central.sonatype.com. Not a code fix — flagged in the PR body for the user to verify before re-running the publish workflow.

## Commits

HEAD — fix: bump vanniktech to 0.36 for snapshot publishing + pin CI runners
