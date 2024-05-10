import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.ModuleType
import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.Theme

plugins {
    id("dev.iurysouza.modulegraph")
}

// Sample showing usage of `moduleGraphConfig` from Kotlin
// Enable this file by removing the `.x` extension, if present.
// Generate graphs for these configs with: `./gradlew createModuleGraph`
moduleGraphConfig {
    /* Setup primary graph */

    /* Primary graph - required config */
    heading.set("# Primary Graph")
    readmePath.set("./README.md")

    /* Primary graph - optional config */
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
    // focusedModulesRegex.set(""".*gama.*""")
    // excludedModulesRegex.set(".*alpha.*")
    // rootModulesRegex.set(".*gama.*")

    /* Setup additional graphs */
    graph(
        readmePath = "./README.md",
        heading = "# Graph with root: gama",
    ) {
        rootModulesRegex = ".*gama.*"
    }
    graph(
        readmePath = "./SomeOtherReadme.md",
        heading = "# Graph",
    ) {
        rootModulesRegex = ".*zeta.*"
    }
}

task("check") {
    dependsOn(tasks.detekt)
    dependsOn(tasks.ktlintCheck)
}
