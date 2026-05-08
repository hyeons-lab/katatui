## Agent

Claude Code (claude-opus-4-7) @ katatui branch feat/maven-publish

## Intent

Enable `publishToMavenLocal` for the `:katatui` library module so the KMP artifacts can be consumed locally as a regular Maven dependency.

## Progress

- [x] Worktree created: `feat/maven-publish`
- [x] Devlog + plan files created
- [x] `maven-publish` plugin applied to `:katatui`
- [x] `group` / `version` wired from `GROUP` / `VERSION_NAME` properties
- [x] `publishToMavenLocal` verified — artifacts present under `~/.m2/repository/com/hyeons-lab/`
- [ ] Pull Request opened

## What Changed

2026-05-08T14:51-0700 katatui/build.gradle.kts — applied `` `maven-publish` `` plugin and set `group`/`version` from `GROUP`/`VERSION_NAME` Gradle properties so KMP's default publications (root + per-target) are wired without duplicating coordinates.
2026-05-08T14:51-0700 devlog/plans/000002-01-maven-publish.md — initial plan.
2026-05-08T14:51-0700 devlog/000002-feat-maven-publish.md — branch devlog.

## Decisions

2026-05-08T14:51-0700 Apply `maven-publish` only to `:katatui` — `:codegen` is an internal build-time tool used by `:katatui`'s codegen task and `:sample-app` is an executable demo; neither belongs on a Maven coordinate.
2026-05-08T14:51-0700 Read coordinates from `gradle.properties` rather than hardcoding in the build script — keeps the source of truth in one place and matches how `GROUP`/`VERSION_NAME` were already declared.
2026-05-08T14:51-0700 Skip POM metadata (license, SCM, developers, signing) — user asked for local publish only; that metadata is only required for Maven Central and is better added alongside a real central-publish plan.

## Issues

2026-05-08T14:51-0700 First `:katatui:publishToMavenLocal` run failed at `buildKatatuiFfi_macosX64` with `error[E0463]: can't find crate for 'core' / 'std'` — the `x86_64-apple-darwin` Rust target wasn't installed on this Apple Silicon dev box. Resolved with `rustup target add x86_64-apple-darwin`; subsequent run published cleanly. This is a one-shot toolchain prerequisite, not something to wire into Gradle.

## Verification

After the rustup target install:

```
$ ls ~/.m2/repository/com/hyeons-lab/
katatui
katatui-macosarm64
katatui-macosx64
```

All three at `0.1.0-SNAPSHOT`. The `katatui` root publication carries KMP metadata; consumers in another KMP project depend on `com.hyeons-lab:katatui:0.1.0-SNAPSHOT` and Gradle resolves the per-target artifact via Gradle module metadata.

## Commits

HEAD — feat: publish :katatui to maven local

## Next Steps

- For Maven Central publishing, add POM metadata (license, SCM, developers) and signing (e.g. `signing` plugin + GPG key in CI).
- Cross-host publishing: macOS-only host publishes only macOS artifacts. CI on a Linux runner publishes the linux targets, Windows for mingw — the canonical Central release is then a fan-in of all hosts (com.vanniktech.maven.publish or similar handles this).
