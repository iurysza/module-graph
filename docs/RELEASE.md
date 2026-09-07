# Release process

Releases are automated with [release-please](https://github.com/googleapis/release-please), same pattern as `visual-artifact-renderer` and `clockwork`.

`plugin-build/gradle.properties` (`VERSION`) is the source of truth. README install snippets are kept in sync via release-please markers. Merging a release PR bumps the version, updates `CHANGELOG.md`, creates a `v*` tag, and publishes a GitHub Release. The `Publish Plugin to Portal` workflow then uploads to the Gradle Plugin Portal.



## VERSION markers (important)

`plugin-build/gradle.properties` is a Java `.properties` file. A mid-line `#` is **not** a comment; it becomes part of the value. That is how Portal publish once failed on:

```properties
VERSION=0.14.0 # x-release-please-version
```

**Always** use block markers on their own lines:

```properties
# x-release-please-start-version
VERSION=0.14.1
# x-release-please-end
```

Never put `x-release-please-version` (or any `#…`) on the `VERSION=` line.

`validateVersionProperties` (part of `check` / `preMerge`) fails the build if the VERSION line is dirty. `sanitizePluginVersion` also strips a trailing `#…` if one slips through at publish time, then rejects anything still invalid.

## Setup (one-time)

Create a fine-grained PAT with `contents:write` and `pull-requests:write` on this repo (or reuse the one used for `visual-artifact-renderer` / `clockwork`), then add it as the repository secret `RELEASE_PLEASE_TOKEN`.

release-please needs a PAT because the default `GITHUB_TOKEN` cannot trigger the publish workflow after it creates a tag or release.

## Cut a release

1. Merge changes to `main` with Conventional Commits (`feat:`, `fix:`, `feat!:` / `BREAKING CHANGE:`, etc.).
2. Wait for the release PR from the `Release Please` workflow (or run it via `workflow_dispatch`).
3. Merge the release PR. release-please tags and opens the GitHub Release.
4. Confirm `Publish Plugin to Portal` succeeds for that tag.

## Manual override

The legacy `version-bumper.main.kts` script still works for emergencies:

```bash
kotlin version-bumper.main.kts MINOR true
```

Prefer release-please for normal cuts so the changelog stays consistent.
