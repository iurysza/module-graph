plugins {
    java
    id("dev.iurysouza.modulegraph.plugin")
}

moduleGraphConfig {
    heading.set("### Dependency Diagram")
    readmeFile.set(File(projectDir, "README.md"))
}

dependencies {
    implementation(project(":example2"))
}
