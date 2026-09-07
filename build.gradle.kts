import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.ModuleType
import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.Theme
import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.versionCheck)
}

subprojects {
    apply {
        plugin(rootProject.libs.plugins.detekt.get().pluginId)
        plugin(rootProject.libs.plugins.ktlint.get().pluginId)
    }

    ktlint {
        debug.set(false)
        verbose.set(true)
        android.set(false)
        outputToConsole.set(true)
        ignoreFailures.set(false)
        enableExperimentalRules.set(true)
        filter {
            exclude("**/generated/**")
            include("**/kotlin/**")
        }
    }

    detekt {
        config.from(rootProject.files("config/detekt/detekt.yml"))
    }
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        html.outputLocation.set(file("build/reports/detekt.html"))
    }
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        candidate.version.isNonStable()
    }
}

fun String.isNonStable() = "^[0-9,.v-]+(-r)?$".toRegex().matches(this).not()

tasks.register("clean", Delete::class.java) {
    delete(layout.buildDirectory)
}

tasks.register("reformatAll") {
    description = "Reformat all the Kotlin Code"
    group = "Verification"

    dependsOn("ktlintFormat")
    dependsOn(gradle.includedBuild("plugin-build").task(":modulegraph:ktlintFormat"))
}

tasks.register("preMerge") {
    description = "Runs all the tests/verification tasks on both top level and included build."
    group = "Verification"

    dependsOn(":sample:check")
    dependsOn(gradle.includedBuild("plugin-build").task(":modulegraph:check"))
    dependsOn(gradle.includedBuild("plugin-build").task(":modulegraph:validatePlugins"))
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

moduleGraphConfig {
    heading.set("# Primary Graph")
    readmePath.set("./sample/README.md")
    showFullPath.set(false)
    orientation.set(Orientation.TOP_TO_BOTTOM)
    linkText.set(LinkText.NONE)
    setStyleByModuleType.set(true)
    theme.set(
        Theme.BASE(
            themeVariables = mapOf(
                "primaryTextColor" to "#F6F8FAff",
                "primaryColor" to "#5a4f7c",
                "primaryBorderColor" to "#5a4f7c",
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
    graph(
        readmePath = "./sample/README.md",
        heading = "# Graph with root: gama",
    ) {
        nestingEnabled = true
        rootModulesRegex = ".*gama.*"
    }
    graph(
        readmePath = "./sample/SomeOtherReadme.md",
        heading = "# Graph",
    ) {
        nestingEnabled = false
        rootModulesRegex = ".*zeta.*"
    }
}
