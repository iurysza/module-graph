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
    sortedProjects: List<Project>,
    dependencies: MutableList<Pair<Project, Project>>,
    theme: Theme,
): String {
    // Create a list of module paths by splitting project paths by ":" and filtering out blank parts
    val modules = sortedProjects.map { project ->
        project.path
            .split(":")
            .filter { it.isNotBlank() }
    }

    // Create a list of distinct group names
    val groups = createGroups(modules)

    // Generate the Mermaid subgraph for each group
    val subgraphs = groups.joinToString("\n") { group ->
        createSubgraph(group, modules)
    }

    // Generate Mermaid arrows for each dependency between projects
    val arrows = dependencies.joinToString("\n") { (source, target) ->
        val sourceName = source.path.split(":").last { it.isNotBlank() }
        val targetName = target.path.split(":").last { it.isNotBlank() }
        "  $sourceName --> $targetName"
    }

    // Combine subgraphs and arrows to create the final Mermaid graph
    val mermaidConfig = """
      %%{
        init: {
          'theme': '${theme.value}'
        }
      }%%
    """.trimIndent()
    return "${mermaidConfig}\n\ngraph LR\n$subgraphs\n$arrows"
}

// Create a list of distinct group names based on the module paths
fun createGroups(modules: List<List<String>>): List<String> {
    return modules.filter { it.size > 1 }.map { it[0] }.distinct()
}

// Generate a Mermaid subgraph for the specified group and list of modules
fun createSubgraph(group: String, modules: List<List<String>>): String {
    // Extract module names for the current group
    val moduleNames = modules
        .filter { it.getOrNull(0) == group }
        .map { if (it.size > 1) it[1] else it[0] }
        .joinToString("\n    ") { moduleName -> moduleName }

    // Create the subgraph string for the current group with its module names
    return "  subgraph $group\n    $moduleNames\n  end"
}

fun appendMermaidGraphToReadme(
    mermaidGraph: String,
    readMeSection: String,
    readmeFile: File,
    logger: Logger,
) {
    val readmeLines: MutableList<String> = readmeFile.readLines().toMutableList()

    val modulesIndex = readmeLines.indexOfFirst { it.startsWith(readMeSection) }

    if (modulesIndex != -1) {
        val endIndex = readmeLines.drop(modulesIndex + 1).indexOfFirst { it.startsWith("#") }
        val actualEndIndex =
            if (endIndex != -1) endIndex + modulesIndex + 1 else readmeLines.size

        readmeLines.subList(modulesIndex + 1, actualEndIndex).clear()
        readmeLines.add(modulesIndex + 1, "```mermaid\n$mermaidGraph\n```")

        readmeFile.writeText(readmeLines.joinToString("\n"))
        logger.debug("Module graph added to ${readmeFile.path} under the $readMeSection section")
    } else {
        logger.debug("The $readMeSection section was not found in ${readmeFile.path}")
    }
}

fun Project.parseProjectStructure(): Pair<MutableList<Pair<Project, Project>>, List<Project>> {
    val projects = mutableSetOf<Project>()
    val dependencies = mutableListOf<Pair<Project, Project>>()

    project.allprojects.forEach { project ->
        project.configurations.forEach { config ->
            config.dependencies.withType(ProjectDependency::class.java)
                .map { it.dependencyProject }
                .forEach { dependency ->
                    if (dependency != project) {
                        projects.add(project)
                        projects.add(dependency)
                        dependencies.add(project to dependency)
                    }
                }
        }
    }

    val sortedProjects = projects.sortedBy { it.path }.distinct()
    return Pair(dependencies, sortedProjects)
}
