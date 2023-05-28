import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.Theme

plugins {
    id("dev.iurysouza.modulegraph")
}

moduleGraphConfig {
    heading.set("# Module Graph")
    readmePath.set("./README.md")

    // optional configs
    showFullPath.set(true)
    orientation.set(Orientation.TOP_TO_BOTTOM)
    linkText.set(LinkText.NONE)
    theme.set(
        Theme.BASE(
            themeVariables = mapOf(
                "primaryTextColor" to "#F6F8FAff", // All text colors
                "primaryColor" to "#5a4f7c", // Box colors
                "primaryBorderColor" to "#5a4f7c", // Box border color
                "lineColor" to "#f5a623", // Line color
                "tertiaryColor" to "#40375c", // Container box background
                "fontSize" to "11px"
            )
        )
    )
}

task("check") {
    dependsOn(tasks.detekt)
    dependsOn(tasks.ktlintCheck)
}
