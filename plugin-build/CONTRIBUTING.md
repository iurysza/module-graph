This is the source for the Module Graph Gradle plugin.

# Formatting
The code style of this project is enforced with Ktlint. Run it with:
`./gradlew :plugin-build:modulegraph:ktlintTestSourceSetCheck`

# Test
This project has extensive unit tests. Run them locally with:
`./gradlew :plugin:modulegraph:test`

# Sample project
Check out the sample project to test out the plugin. Create graphs with:
`./gradlew createModuleGraph`
