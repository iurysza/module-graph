This is the source for the Module Graph Gradle plugin.

# Workflow

The recommended workflow is to navigate to the root and run:

- `./gradlew reformatAll` - for auto-fixing formatting issues
- `./gradlew preMerge` - for validating everything (this runs ktlint, detekt and tests)

# Formatting

The code style of this project is enforced with Detekt. Run it with:
`./gradlew :plugin-build:modulegraph:detekt`

Also check Ktlint:
`./gradlew :plugin-build:modulegraph:ktlintMainSourceSetCheck`
`./gradlew :plugin-build:modulegraph:ktlintTestSourceSetCheck`

# Test

This project has extensive unit tests. Run them locally with:
`./gradlew :plugin:modulegraph:test`

# Sample project

Check out the sample project to test out the plugin. Create graphs with:
`./gradlew createModuleGraph`

# Local publish

You can test a local release of this plugin on another local project by publishing the plugin
locally.

Run:
`./gradlew :plugin-build:modulegraph:publishToMavenLocal`

Then in the **consumer** project, make sure `mavenLocal` is a configured repository.
In `settings.gradle`:

```
pluginManagement {
    repositories {
        mavenLocal()
        ...
    }
}
```

Give `mavenLocal` priority over all other repositories by putting in first in the list.
This ensures the plugin will use your local publish over official releases.
