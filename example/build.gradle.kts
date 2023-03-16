plugins {
    java
    id("dev.iurysouza.modulegraph")
}

moduleGraphConfig {
    heading.set("### Dependency Diagram")
    readmeFile.set(file("$projectDir/README.md"))
}

dependencies {
    implementation(project(":example2"))
}
