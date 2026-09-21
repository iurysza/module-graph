# Agent map

This repository is the Gradle plugin `dev.iurysouza.modulegraph`. It generates Mermaid architecture graphs from real module dependencies. Plugin source lives in `plugin-build/modulegraph/`. The `sample/` tree is the fixture used to exercise graphs.

## Docs

How-it-works docs belong in `ai-artifacts/` and must stay current when behavior or structure changes. Start at `ai-artifacts/_index.md`. That file points at `README.md` and `docs/`. Grow architecture notes under `ai-artifacts/` as you learn the graph pipeline.

## JDK 8

Use JDK 8. Pre Merge Checks, Publish Plugin to Portal, and Validate Gradle Wrapper all install Zulu 8. The plugin sets `jvmToolchain(8)` and `JavaVersion.VERSION_1_8`. Gradle configuration fails if no Java 8 toolchain is present. Point `JAVA_HOME` at JDK 8, or install JDK 8 where Gradle can detect it.

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
