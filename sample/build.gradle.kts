import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.Theme

plugins {
    id("dev.iurysouza.modulegraph")
}

moduleGraphConfig {
    heading.set("# Module Graph")
    readmePath.set("./README.md")
    theme.set(
        Theme.BASE(
            mapOf(
                "primaryTextColor" to "#fff", // All text colors
                "primaryColor" to "#5a4f7c", // Box colors
                "primaryBorderColor" to "#5a4f7c", // Box border color
                "lineColor" to "#f5a623", // Line color
                "tertiaryColor" to "#40375c", // Container box background
                "fontSize" to "11px"
            )
        )
    )
    orientation.set(Orientation.TOP_TO_BOTTOM)
}

task("check") {
    dependsOn(tasks.detekt)
    dependsOn(tasks.ktlintCheck)
}
