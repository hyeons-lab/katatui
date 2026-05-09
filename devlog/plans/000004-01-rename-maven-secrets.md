## Thinking

The signing secrets in `publish.yml` already follow the `ORG_GRADLE_PROJECT_*` naming pattern (`ORG_GRADLE_PROJECT_SIGNINGINMEMORYKEY`, `ORG_GRADLE_PROJECT_SIGNINGINMEMORYKEYPASSWORD`), but the maven central credential secrets were named `MAVEN_CENTRAL_USERNAME` / `MAVEN_CENTRAL_PASSWORD`. Inconsistent.

User asked to standardize on `ORG_GRADLE_PROJECT_*` names. The new secrets `ORG_GRADLE_PROJECT_MAVENCENTRALUSERNAME` / `ORG_GRADLE_PROJECT_MAVENCENTRALPASSWORD` already exist in repo settings (created via `gh secret set` ahead of this PR with the latest credentials).

This PR only swaps the references in `publish.yml`. The old `MAVEN_CENTRAL_*` secrets stay until merge so the workflow doesn't break in the meantime, then they get deleted.

## Plan

1. Update `.github/workflows/publish.yml` env block: `secrets.MAVEN_CENTRAL_USERNAME` → `secrets.ORG_GRADLE_PROJECT_MAVENCENTRALUSERNAME`; same for the password.
2. Commit + open PR.
3. After merge: delete the old `MAVEN_CENTRAL_USERNAME` and `MAVEN_CENTRAL_PASSWORD` secrets via `gh secret delete`.
