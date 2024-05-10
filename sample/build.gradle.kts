import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.ModuleType
import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.Theme

plugins {
    id("dev.iurysouza.modulegraph")
}

// Generate graphs for these configs with: `./gradlew createModuleGraph`
moduleGraphConfig {
    heading.set("# Module Graph")
    readmePath.set("./README.md")

    // optional configs
    // focusedModulesRegex.set(""".*gama.*""")
    showFullPath.set(false)
    orientation.set(Orientation.TOP_TO_BOTTOM)
    linkText.set(LinkText.NONE)
    setStyleByModuleType.set(true)
    theme.set(
        Theme.BASE(
            themeVariables = mapOf(
                // Text
                "primaryTextColor" to "#F6F8FAff",
                // Node
                "primaryColor" to "#5a4f7c",
                // Node border
                "primaryBorderColor" to "#5a4f7c",
                // Container box background
                "tertiaryColor" to "#40375c",
                "lineColor" to "#f5a623",
                "fontSize" to "12px",
            ),
            focusColor = "#F5A622",
            moduleTypes = listOf(
                ModuleType.Kotlin("#2C4162"),
            ),
        ),
    )
    excludedConfigurationsRegex.set(""".*test.*""")

    // You can choose to exclude certain modules
    // excludedModulesRegex.set(".*alpha.*")

    // You can choose to only include modules that are reachable from certain root modules
    // rootModulesRegex.set(".*gama.*")

    // Add other graph
    graph(
        readmePath = "./README.md",
        heading = "# My second graph",
    ) {
        rootModulesRegex = ".*gama.*"
    }

    // Add other graph
    graph(
        readmePath = "./otherReadme.md",
        heading = "# Graph",
    ) {
        rootModulesRegex = ".*zeta.*"
    }
}

task("check") {
    dependsOn(tasks.detekt)
    dependsOn(tasks.ktlintCheck)
}
