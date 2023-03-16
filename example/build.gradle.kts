plugins {
    java
    id("dev.iurysouza.modulegraph")
}

moduleGraphConfig {
    heading.set("### Dependency Diagram")
    readmePath.set("$projectDir/README.md")
}

dependencies {
    implementation(project(":example2"))
}
