## Agent

Claude Code (claude-opus-4-7) @ katatui branch chore/rename-maven-secrets

## Intent

The signing secrets in `publish.yml` follow the `ORG_GRADLE_PROJECT_*` naming pattern, but the maven central credential secrets were named `MAVEN_CENTRAL_USERNAME` / `MAVEN_CENTRAL_PASSWORD`. Standardize on `ORG_GRADLE_PROJECT_*` for consistency.

## What Changed

2026-05-08T20:43-0700 .github/workflows/publish.yml — swapped `secrets.MAVEN_CENTRAL_USERNAME` / `secrets.MAVEN_CENTRAL_PASSWORD` references for the new `secrets.ORG_GRADLE_PROJECT_MAVENCENTRALUSERNAME` / `secrets.ORG_GRADLE_PROJECT_MAVENCENTRALPASSWORD`. The new secrets were created in the repo via `gh secret set` ahead of this PR.

## Decisions

2026-05-08T20:43-0700 Old `MAVEN_CENTRAL_*` secrets stay until merge — deleting them now would break the publish workflow on `main` if it was triggered between push and merge. Deletion happens after merge as a manual `gh secret delete` step.

## Issues

None — straightforward rename.

## Commits

HEAD — chore: rename maven central secrets to ORG_GRADLE_PROJECT_* convention
