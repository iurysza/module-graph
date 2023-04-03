plugins {
    id("dev.iurysouza.modulegraph")
}

moduleGraphConfig {
    heading.set("# Dependency Diagram")
    readmePath.set("./README.md")
    theme.set(dev.iurysouza.modulegraph.Theme.NEUTRAL)
}

task("check") {
    dependsOn(tasks.detekt)
    dependsOn(tasks.ktlintCheck)
}
