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
    pattern.set(""".*zeta.*""")
    showFullPath.set(false)
    orientation.set(Orientation.TOP_TO_BOTTOM)
    linkText.set(LinkText.NONE)
    theme.set(
        Theme.BASE(
            themeVariables = mapOf(
                "primaryTextColor" to "#F6F8FAff", // All text colors
                "primaryColor" to "#5a4f7c", // Node color
                "primaryBorderColor" to "#5a4f7c", // Node border color
                "tertiaryColor" to "#40375c", // Container box background
                "lineColor" to "#f5a623",
                "fontSize" to "12px",
            ),
            focusColor = "#F5A622"
        )
    )
    excludeConfigurationNames.set(listOf("testImplementation"))
}

task("check") {
    dependsOn(tasks.detekt)
    dependsOn(tasks.ktlintCheck)
}
