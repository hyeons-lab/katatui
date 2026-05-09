## Thinking

Publish workflow keeps 403'ing on snapshot upload to `central.sonatype.com/repository/maven-snapshots/com/hyeonslab/...` despite the user having a valid Central Portal user token and snapshots enabled on their namespace.

Root cause discovered: the registered Central Portal namespace is `com.hyeons-lab` (with a hyphen, matching the GitHub org `hyeons-lab`), but `gradle.properties` declared `GROUP=com.hyeonslab` (no hyphen). Sonatype rejected the upload because the artifact's groupId fell under a namespace the account doesn't own.

Maven groupIds allow hyphens (different rules than Java/Kotlin packages, which can't contain them). So the fix is just `GROUP=com.hyeons-lab` in `gradle.properties`. Kotlin packages stay `com.hyeonslab.katatui` since hyphens aren't valid in Java identifiers.

## Plan

1. Update `gradle.properties`: `GROUP=com.hyeonslab` → `GROUP=com.hyeons-lab`.
2. Verify locally with `./gradlew :katatui:publishToMavenLocal` and check `~/.m2/repository/com/hyeons-lab/` contains the artifacts.
3. Commit + open PR.
4. After merge: re-trigger publish workflow; expect success.

Did NOT touch Kotlin packages — those remain `com.hyeonslab.katatui` (hyphens not valid in Java identifiers, and renaming would touch every file). The Maven groupId vs. Kotlin package name decoupling is normal.
