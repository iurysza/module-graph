import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.ModuleType
import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.Theme

plugins {
    id("dev.iurysouza.modulegraph")
}

// Sample showing usage of `moduleGraphConfig` from Groovy.
// Enable this file by removing the `.x` extension, if present.
// Generate graphs for these configs with: `./gradlew createModuleGraph`
moduleGraphConfig {
    /* Setup primary graph */

    /* Primary graph - required config */
    readmePath = "./README.md"
    heading = "# Primary Graph"

    /* Primary graph - optional config */
    showFullPath = false
    orientation = Orientation.TOP_TO_BOTTOM
    linkText = LinkText.NONE
    setStyleByModuleType = true
    excludedConfigurationsRegex.set(""".*test.*""")
    // focusedModulesRegex.set(""".*gama.*""")
    // excludedModulesRegex.set(".*alpha.*")
    // rootModulesRegex.set(".*gama.*")
    // strictMode.set(true)


    /* Primary graph - optional theme */
    def customTheme = new Theme.BASE()
    customTheme.themeVariables = [
        // Text
        "primaryTextColor"  : "#F6F8FAff",
        // Node
        "primaryColor"      : "#5a4f7c",
        // Node border
        "primaryBorderColor": "#5a4f7c",
        // Container box background
        "tertiaryColor"     : "#40375c",
        "lineColor"         : "#f5a623",
        "fontSize"          : "12px",
    ]
    customTheme.focusColor = "#F5A622"
    customTheme.moduleTypes = [
        new ModuleType.Kotlin("#2C4162"),
    ]
    theme = customTheme

    /* Setup additional graphs */
    graph(
        "./README.md",
        "# Graph with root: gama",
    ) {
        it.rootModulesRegex = ".*gama.*"
    }
    graph(
        "./SomeOtherReadme.md",
        "# Graph",
    ) {
        it.rootModulesRegex = ".*zeta.*"
    }
}

task("check") {
    dependsOn(tasks.detekt)
    dependsOn(tasks.ktlintCheck)
}
