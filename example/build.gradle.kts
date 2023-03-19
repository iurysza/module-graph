plugins {
    java
    id("dev.iurysouza.modulegraph")
}

moduleGraphConfig {
    heading.set("### Dependency Diagram")
    readmePath.set("$projectDir/README.md")
    theme.set(dev.iurysouza.modulegraph.Theme.NEUTRAL)
}

dependencies {
    implementation(project(":groupFolder:example2"))
}
