# Release process

Releases are automated with [release-please](https://github.com/googleapis/release-please), same pattern as `visual-artifact-renderer` and `clockwork`.

`plugin-build/gradle.properties` (`VERSION`) is the source of truth. README install snippets are kept in sync via release-please markers. Merging a release PR bumps the version, updates `CHANGELOG.md`, creates a `v*` tag, and publishes a GitHub Release. The `Publish Plugin to Portal` workflow then uploads to the Gradle Plugin Portal.

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
