package dev.iurysouza.modulegraph

import java.io.File
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.logging.Logger

/**
 * Generates a Mermaid graph for module dependencies.
 * - Groups: Folders containing modules
 * - Subgraphs: Grouped modules in the graph
 * - Source: Module with dependencies
 * - Target: Dependent module
 * - Arrows: Represent dependencies between modules
 */

fun buildMermaidGraph(
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

    var arrows = ""

    dependencies.forEach { (source, targets) ->
        if (source == ":") return@forEach
        targets.forEach { target ->
            val sourceName = source.split(":").last { it.isNotBlank() }
            val targetName = target.targetProjectPath.split(":").last { it.isNotBlank() }
            if (sourceName != targetName) {
                val link = when (linkText) {
                    LinkText.CONFIGURATION -> "-- ${target.configName} -->"
                    LinkText.NONE -> "-->"
                }
                arrows += "  $sourceName $link $targetName\n"
            }
        }
    }

    val mermaidConfig = """
      %%{
        init: {
          'theme': '${theme.value}'
        }
      }%%
    """.trimIndent()
    val graphOrientation = orientation.value
    return "${mermaidConfig}\n\ngraph $graphOrientation\n$subgraphs\n$arrows"
}

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

fun appendMermaidGraphToReadme(
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

fun Project.parseProjectStructure(): HashMap<String, List<Dependency>> {
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
