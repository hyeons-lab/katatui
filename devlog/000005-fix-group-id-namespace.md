## Agent

Claude Code (claude-opus-4-7) @ katatui branch fix/group-id-namespace

## Intent

The publish workflow has been 403'ing on snapshot upload despite valid credentials and snapshots enabled. Root cause: the Maven `GROUP` was `com.hyeonslab` but the registered Central Portal namespace is `com.hyeons-lab` (with hyphen, matching the GitHub org). Align them.

## What Changed

2026-05-08T20:54-0700 gradle.properties — `GROUP=com.hyeonslab` → `GROUP=com.hyeons-lab`. The artifact path becomes `com/hyeons-lab/katatui/0.1.0-SNAPSHOT/...`, which falls under the namespace the publishing account actually owns.

## Decisions

2026-05-08T20:54-0700 Did not rename Kotlin packages — `com.hyeonslab.katatui` stays. Hyphens aren't valid in Java identifiers, and the groupId vs. package name decoupling is normal in Maven (e.g., `org.jetbrains.kotlin` group publishes packages like `kotlin.test`, etc.). Renaming packages would touch every source file for no functional gain.

## Issues

**Three rounds of token regeneration didn't help** because the namespace mismatch was the real blocker. A 403 (vs 401) on a snapshot upload always implies "authenticated but unauthorized for this resource" — token regeneration only addresses 401-class problems. Lesson for future debugging: when a snapshot upload 403s with valid credentials, check the artifact path against the registered namespace before regenerating tokens.

Verified locally with `:katatui:publishToMavenLocal` — artifacts now land under `~/.m2/repository/com/hyeons-lab/katatui/...`.

## Commits

HEAD — fix: align Maven GROUP with registered namespace com.hyeons-lab
