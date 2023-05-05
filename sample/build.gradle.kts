import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.Theme

plugins {
    id("dev.iurysouza.modulegraph")
}

moduleGraphConfig {
    heading.set("# Module Graph")
    readmePath.set("./README.md")
    theme.set(Theme.DARK)
    orientation.set(Orientation.TOP_TO_BOTTOM)
}

task("check") {
    dependsOn(tasks.detekt)
    dependsOn(tasks.ktlintCheck)
}
