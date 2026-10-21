# Agent map

This repository is the Gradle plugin `dev.iurysouza.modulegraph`. It generates Mermaid architecture graphs from real module dependencies. Plugin source lives in `plugin-build/modulegraph/`. The `sample/` tree is the fixture used to exercise graphs.

## Docs

How-it-works docs belong in `ai-artifacts/` and must stay current when behavior or structure changes. Start at `ai-artifacts/_index.md`. That file points at `README.md` and `docs/`. Grow architecture notes under `ai-artifacts/` as you learn the graph pipeline.

## JDK

Gradle 9.8.1 runs only on JDK 17 or newer. Pre Merge Checks and Publish Plugin to Portal install Zulu 17 for the Gradle daemon and Zulu 8 as a toolchain. The plugin still sets `jvmToolchain(8)` and `JavaVersion.VERSION_1_8`, so the published classes stay on Java 8. TestKit tests run on JDK 17 because that worker is what starts Gradle.

Point `JAVA_HOME` at JDK 17 or newer, and install a JDK 8 that Gradle can detect. Configuration fails when the Java 8 toolchain is missing. Validate Gradle Wrapper still installs Zulu 8. That job checks the wrapper jar and does not start Gradle.

This wrapper bump does not declare a higher minimum Gradle version for plugin users. The published classes stay on Java 8, and the plugin descriptors do not set a Gradle requirement.

## Commands

Primary gate, from `.github/workflows/pre-merge.yaml`:

```sh
./gradlew preMerge --continue
```

`preMerge` in the root `build.gradle.kts` runs `:sample:check`, included-build `:modulegraph:check`, and included-build `:modulegraph:validatePlugins`.

Other tasks in this tree. Run the ones you need. They are not a sequence:

```sh
./gradlew reformatAll
./gradlew createModuleGraph
./gradlew :sample:check
./gradlew :plugin-build:modulegraph:check
./gradlew :plugin-build:modulegraph:test
./gradlew :plugin-build:modulegraph:build
./gradlew :plugin-build:modulegraph:validateVersionProperties
./gradlew :plugin-build:modulegraph:validatePlugins
./gradlew :plugin-build:modulegraph:publishToMavenLocal
```

`check` on `:plugin-build:modulegraph` includes tests, ktlint, detekt, and `validateVersionProperties`. Root `check` skips the included plugin build. Use `preMerge` for the full gate.

`createModuleGraph` writes Mermaid into the sample READMEs. `reformatAll` runs ktlintFormat on the root and on `plugin-build`.

To publish to the Plugin Portal, with `GRADLE_PUBLISH_KEY` and `GRADLE_PUBLISH_SECRET` set:

```sh
./gradlew --project-dir plugin-build setupPluginUploadFromEnvironment publishPlugins
```

That command is the Publish Plugin to Portal workflow step. Do not run it from a change PR.

## Closed loop

1. Research: read `README.md`, `ai-artifacts/`, `docs/`, and `plugin-build/modulegraph/`.
2. Change the plugin, the sample, or the docs.
3. Run `./gradlew preMerge --continue`. That task is the local equivalent of Pre Merge Checks.

If `preMerge` fails, fix the failure and run it again before you push.

## Git commits

Never include Cursor (or any Cursor agent/bot) as git author, committer, or in a Co-authored-by / similar trailer.
