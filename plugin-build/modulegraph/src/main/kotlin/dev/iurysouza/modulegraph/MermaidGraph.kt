package dev.iurysouza.modulegraph

import java.io.File
import java.io.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.logging.Logger

/**
 * Generates a Mermaid graph for module dependencies.
 * - Groups: Folders containing modules
 * - Subgraphs: Grouped modules in the graph
 * - Source: Module with dependencies
 * - Target: Dependent module
 * - Digraph: Represents the dependencies between two modules: source -> target
 */
internal fun buildMermaidGraph(
    theme: Theme,
    orientation: Orientation,
    linkText: LinkText,
    dependencies: MutableMap<String, List<Dependency>>,
): String {
    val projectPaths = dependencies
        .entries
        .asSequence()
        .flatMap { (source, target) -> target.map { it.targetProjectPath }.plus(source) }
        .sorted()

    val mostMeaningfulGroups: List<String> = projectPaths
        .map { it.split(":").takeLast(2).take(1) }
        .distinct()
        .flatten()
        .toList()

    val projectNames = projectPaths
        .map { listOf(it.split(":").takeLast(2)) }
        .distinct()
        .flatten()
        .toList()

    val subgraphs = mostMeaningfulGroups.joinToString("\n") { group ->
        createSubgraph(group, projectNames)
    }

    val digraph = dependencies.filterKeys { it != ":" }.flatMap { entry ->
        val sourceName = entry.key.split(":").last { it.isNotBlank() }
        entry.value.mapNotNull { target ->
            val targetName = target.targetProjectPath.split(":").last { it.isNotBlank() }
            if (sourceName != targetName) {
                val link = when (linkText) {
                    LinkText.CONFIGURATION -> "-- ${target.configName} -->"
                    LinkText.NONE -> "-->"
                }
                "  $sourceName $link $targetName"
            } else {
                null
            }
        }
    }.joinToString(separator = "\n")

    return "${createConfig(theme)}\n\ngraph ${orientation.value}\n$subgraphs\n$digraph"
}

private fun createConfig(theme: Theme): String = """
%%{
  init: {
    'theme': '${theme.name}'${if (theme is Theme.BASE) ",\n\t'themeVariables': ${Json.encodeToString(theme.themeVariables).trimIndent()}" else ""}
  }
}%%
""".trimIndent()

// Generate a Mermaid subgraph for the specified group and list of modules
fun createSubgraph(group: String, modules: List<List<String>>): String {
    // Extract module names for the current group
    val moduleNames = modules.asSequence()
        .map { it.takeLast(2) } // group by the most relevant part of the path
        .filter { it.all { it.length > 2 } }
        .filter { it.contains(group) }
        .map { it.last() } // take the actual module name
        .joinToString("\n    ") { moduleName -> moduleName }

    if (moduleNames.isBlank()) {
        return ""
    }
    return "  subgraph $group\n    $moduleNames\n  end"
}

internal fun appendMermaidGraphToReadme(
    mermaidGraph: String,
    readMeSection: String,
    readmeFile: File,
    logger: Logger,
) {
    if (!readmeFile.exists()) {
        readmeFile.createNewFile()
        logger.warn(
            """
                The specified README file was not found.
                A new file has been created at: ${readmeFile.path}
            """.trimMargin()
        )
    }
    val readmeLines = readmeFile.readLines().toMutableList()
    val sectionStartIndex = findPredefinedSection(readmeLines, readMeSection)
    val sectionEndIndex = findNextSectionStart(readmeLines, sectionStartIndex)

    readmeLines.subList(sectionStartIndex + 1, sectionEndIndex).clear()
    if (sectionStartIndex == -1) {
        readmeLines.add(0, "$readMeSection\n\n```mermaid\n$mermaidGraph\n```")
    } else {
        readmeLines.add(sectionStartIndex + 1, "\n```mermaid\n$mermaidGraph\n```")
    }
    readmeFile.writeText(readmeLines.joinToString("\n"))
    logger.debug("Module graph added to ${readmeFile.path} under the $readMeSection section")
}

private fun findPredefinedSection(readmeLines: List<String>, section: String): Int {
    return readmeLines.indexOfFirst { it.startsWith(section) }
}

private fun findNextSectionStart(readmeLines: List<String>, startIndex: Int): Int {
    return readmeLines.drop(startIndex + 1).indexOfFirst { it.startsWith("#") }.let {
        if (it != -1) it + startIndex + 1 else readmeLines.size
    }
}

/**
 * Represents a dependency on a project.
 * Contains the name of the configuration to which the dependency belongs.
 */
internal data class Dependency(
    val targetProjectPath: String,
    val configName: String,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 46465844
    }
}

internal fun Project.parseProjectStructure(): HashMap<String, List<Dependency>> {
    println("Parsing project structure...")
    val dependencies = hashMapOf<String, List<Dependency>>()
    project.allprojects.forEach { sourceProject ->
        sourceProject.configurations.forEach { config ->
            config.dependencies.withType(ProjectDependency::class.java)
                .map { it.dependencyProject }
                .forEach { targetProject ->
                    dependencies[sourceProject.path] =
                        dependencies.getOrDefault(sourceProject.path, emptyList())
                            .plus(Dependency(targetProject.path, config.name))
                }
        }
    }
    return dependencies
}
